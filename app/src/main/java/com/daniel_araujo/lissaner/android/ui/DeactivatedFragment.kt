package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.android.AutoServiceBind
import com.daniel_araujo.lissaner.android.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener

class DeactivatedFragment : Fragment() {
    /**
     * We only have a service so that we can record with the app closed.
     */
    var recordingService: AutoServiceBind<RecordingService>? = null

    /**
     * The button that starts and stops recording.
     */
    lateinit var buttonRecord: RecButtonView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deactivated, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonRecord = view.findViewById<RecButtonView>(R.id.button_activate).also {
            it.setOnClickListener {
                startRecording()
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
                    }
                }
            })
            .check();
    }

    override fun onResume() {
        super.onResume()

        recordingService = AutoServiceBind(
            RecordingService::class,
            requireActivity().application
        )
    }
}