package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.RecordingManager
import com.daniel_araujo.lissaner.android.AutoServiceBind
import com.daniel_araujo.lissaner.android.RecordingService
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.single.BasePermissionListener


class MainFragment : Fragment() {
    /**
     * We only have a service so that we can record with the app closed.
     */
    var recordingService: AutoServiceBind<RecordingService>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.settings_button).also {
             it.setOnClickListener {
                findNavController().navigate(R.id.action_global_settingsFragment)
            }
        }

        view.findViewById<Button>(R.id.files_button).also {
            it.setOnClickListener {
                findNavController().navigate(R.id.action_recordFragment_to_filesFragment)
            }
        }

        view.findViewById<Button>(R.id.about_button).also {
            it.setOnClickListener {
                findNavController().navigate(R.id.action_recordFragment_to_aboutFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        recordingService = AutoServiceBind(
            RecordingService::class,
            requireActivity().application
        )

        recordingService?.run {
            it.onRecordStart = {
                update(it.recording)
            }
            it.onRecordStop = {
                update(it.recording)
            }
            update(it.recording)
        }
    }

    override fun onPause() {
        super.onPause()

        recordingService?.run {
            it.onRecordStart = null
            it.onRecordStop = null
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

    private fun update(recording: RecordingManager) {
        val ft = childFragmentManager.beginTransaction()
        if (recording.isRecording()) {
            ft.replace(R.id.content, RecordingFragment())
        } else {
            ft.replace(R.id.content, DeactivatedFragment())
        }
        ft.commit()
    }
}