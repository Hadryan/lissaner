package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.ByteFormatUtils
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.android.Application
import com.daniel_araujo.lissaner.android.AutoServiceBind
import com.daniel_araujo.lissaner.android.PreferenceUtils
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
    var recordingService: AutoServiceBind<RecordingService>? = null

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
     * Shows available storage space.
     */
    lateinit var storageInfo: TextView

    /**
     * The button that starts and stops recording.
     */
    lateinit var settingsButton: Button

    /**
     * The timer that queries memory info.
     */
    var systemStatusTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        accumulatedTime = view.findViewById<TextCounter>(R.id.accumulated_time)
        memoryInfo = view.findViewById<TextView>(R.id.memory_info)
        storageInfo = view.findViewById<TextView>(R.id.storage_info)

        controls = view.findViewById<LinearLayout>(R.id.controls)
        // They are supposed to be invisible by default but I leave them visible so I can see them
        // in the designer.
        controls.visibility = View.GONE

        labelRecord = view.findViewById<TextView>(R.id.label_record)

        buttonRecord = view.findViewById<RecButtonView>(R.id.button_record).also {
            it.setOnClickListener {
                toggleRecording()
            }
        }

        view.findViewById<Button>(R.id.button_save).also {
            it.setOnClickListener {
                saveRecording()
            }
        }

        view.findViewById<Button>(R.id.button_cancel).also {
            it.setOnClickListener {
                cancelRecording()
            }
        }

        settingsButton = view.findViewById<Button>(R.id.settings_button).also {
             it.setOnClickListener {
                findNavController().navigate(R.id.action_global_settingsFragment)
            }
        }
    }

    private fun toggleRecording() {
        recordingService?.run {
            if (!it.recording.isRecording()) {
                startRecording()
            } else {
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        Log.v(javaClass.simpleName, "startRecording")

        Dexter.withContext(activity)
            .withPermission(android.Manifest.permission.RECORD_AUDIO)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    recordingService?.run {
                        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

                        if (it.recording.accumulated() == 0L) {
                            it.recording.sampleRate = PreferenceUtils.getIntOrFail(
                                preferences,
                                Application.PREFERENCE_SAMPLES_PER_SECOND
                            )
                            it.recording.bitsPerSample = PreferenceUtils.getIntOrFail(
                                preferences,
                                Application.PREFERENCE_BITS_PER_SAMPLE
                            )
                        }

                        try {
                            it.recording.startRecording()
                        } catch (e: OutOfMemoryError) {
                            // We may have some left to show an error message.
                            AlertDialog.Builder(context!!)
                                .setTitle("Unable to record")
                                .setMessage("Out of memory.")
                                .setNeutralButton(android.R.string.ok) { dialog, _ -> dialog.cancel() }
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                        }

                        updateControls(it.recording)
                    }
                }
            })
            .check();
    }

    private fun stopRecording() {
        Log.v(javaClass.simpleName, "stopRecording")

        recordingService?.run {
            it.recording.stopRecording()
            updateControls(it.recording)
        }
    }

    private fun saveRecording() {
        Log.v(javaClass.simpleName, "saveRecording")

        recordingService?.run { service ->
            try {
                val name = System.currentTimeMillis().toString() + ".wav"
                val stream = ourActivity.ourApplication.recordingFiles.create(name);

                Log.d(javaClass.simpleName, "Creating ${name}")

                stream.use {
                    service.recording.saveRecording(stream)
                }
            } catch (e: IOException) {
                Log.e(javaClass.simpleName, "File write failed.", e)
            }
        }
    }

    private fun cancelRecording() {
        Log.v(javaClass.simpleName, "cancelRecording")

        recordingService?.run {
            it.recording.stopRecording()
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

        recordingService = AutoServiceBind(
            RecordingService::class,
            activity!!.application
        )

        recordingService?.run {
            it.recording.onAccumulateListener = {
                accumulatedTime.time = it.recording.accumulated()

                updateControls(it.recording)
            }

            // Synchronize recording button state.
            buttonRecord.isActivated = it.recording.isRecording();

            // Synchronize accumulated time.
            accumulatedTime.time = it.recording.accumulated()

            updateControls(it.recording)
        }

        systemStatusTimer = Timer("SystemStatusTimer")
        systemStatusTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Memory.
                run {
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

                // Storage.
                run {
                    val stat = StatFs(requireContext().getExternalFilesDir(null)?.path)

                    val bytesAvailable = stat.availableBytes
                    val bytesTotal = stat.totalBytes

                    activity!!.runOnUiThread {
                        storageInfo.text = ByteFormatUtils.shortSize(context!!, bytesAvailable) +
                                '/' +
                                ByteFormatUtils.shortSize(context!!, bytesTotal)
                    }
                }
            }
        }, 0, 5000)
    }

    override fun onPause() {
        super.onPause()

        recordingService?.run {
            it.recording.onAccumulateListener = null
        }

        systemStatusTimer!!.cancel()
        systemStatusTimer = null
    }
}