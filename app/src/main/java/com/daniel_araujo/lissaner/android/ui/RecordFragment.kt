package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.daniel_araujo.lissaner.ByteFormatUtils
import com.daniel_araujo.lissaner.android.AutoServiceBind
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.android.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener
import java.io.IOException
import java.util.*

class RecordFragment : Fragment() {
    /**
     * We only have a service so that we can record with the app closed.
     */
    lateinit var recordingService: AutoServiceBind<RecordingService>

    /**
     * The button that starts and stops recording.
     */
    lateinit var buttonRecord: RecButtonView

    /**
     * Complements recording button.
     */
    lateinit var labelRecord: TextView

    /**
     * The container that holds recording controls.
     */
    lateinit var controls: View

    /**
     * A view that shows how many seconds have been recorded.
     */
    lateinit var accumulatedTime: TextCounter

    /**
     * Shows available memory.
     */
    lateinit var memoryInfo: TextView

    /**
     * The timer that queries memory info.
     */
    var memoryTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordingService =
            AutoServiceBind(
                RecordingService::class,
                activity!!
            )

        recordingService.onConnectListener = { service ->
            service.onAccumulateListener = {
                accumulatedTime.time = service.recording.accumulated()

                updateControls(service.recording)
            }
        }

        accumulatedTime = view.findViewById<TextCounter>(R.id.accumulated_time)
        memoryInfo = view.findViewById<TextView>(R.id.memory_info)

        controls = view.findViewById<LinearLayout>(R.id.controls)
        // They are supposed to be invisible by default but I leave them visible so I can see them
        // in the designer.
        controls.visibility = View.GONE

        labelRecord = view.findViewById<TextView>(R.id.label_record)

        buttonRecord = view.findViewById<RecButtonView>(R.id.button_record).also {
            it.setOnClickListener {
                toggleRecordingService()
            }
        }

        view.findViewById<Button>(R.id.button_save).also {
            it.setOnClickListener {
                saveRecordingService()
            }
        }

        view.findViewById<Button>(R.id.button_discard).also {
            it.setOnClickListener {
                discardRecording()
            }
        }

        recordingService.run {
            // Synchronize recording button state.
            buttonRecord.isActivated = it.recording.isRecording();

            // Synchronize accumulated time.
            accumulatedTime.time = it.recording.accumulated()

            updateControls(it.recording)
        }
    }

    private fun toggleRecordingService() {
        recordingService.run {
            if (!it.recording.isRecording()) {
                startRecordingService()
            } else {
                stopRecordingService()
            }
        }
    }

    private fun startRecordingService() {
        Log.v(javaClass.simpleName, "startRecordingService")

        Dexter.withContext(activity)
            .withPermission(android.Manifest.permission.RECORD_AUDIO)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    recordingService.run {
                        it.recording.startRecording()
                        updateControls(it.recording)
                    }
                }
            })
            .check();
    }

    private fun stopRecordingService() {
        Log.v(javaClass.simpleName, "stopRecordingService")

        recordingService.run {
            it.recording.stopRecording()
            updateControls(it.recording)
        }
    }

    private fun saveRecordingService() {
        Log.v(javaClass.simpleName, "saveRecordingService")

        recordingService.run { service ->
            try {
                val name = System.currentTimeMillis().toString() + ".wav"
                val stream = ourActivity.ourApplication.recordingFiles.create(name);

                Log.d(javaClass.simpleName, "Creating ${name}")

                stream.use { stream ->
                    service.recording.saveRecording(stream)
                }
            } catch (e: IOException) {
                Log.e("Exception", "File write failed.", e)
            }
        }
    }

    private fun discardRecording() {
        Log.v(javaClass.simpleName, "saveRecordingService")

        recordingService.run {
            it.recording.discardRecording()
        }
    }

    private fun updateControls(recording: RecordingManager) {
        buttonRecord.isActivated = recording.isRecording()

        controls.visibility = if (recording.accumulated() > 0) View.VISIBLE else View.GONE

        labelRecord.text = if (recording.isRecording()) {
            context!!.getText(R.string.record_stop)
        } else {
            if (recording.accumulated() > 0) {
                context!!.getText(R.string.record_continue)
            } else {
                context!!.getText(R.string.record_start)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        memoryTimer = Timer("MemoryTimer")
        memoryTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val runtime = Runtime.getRuntime()

                val total = runtime.maxMemory()
                val used = runtime.totalMemory() - runtime.freeMemory()

                activity!!.runOnUiThread {
                    memoryInfo.text =
                        ByteFormatUtils.shortSize(context!!, used) +
                                '/' +
                                ByteFormatUtils.shortSize(context!!, total)
                }
            }
        }, 0, 5000)
    }

    override fun onPause() {
        super.onPause()

        memoryTimer!!.cancel()
        memoryTimer = null
    }
}