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
     * The button that starts and stops recording.
     */
    lateinit var buttonRecord: RecButtonView

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

        buttonRecord = view.findViewById<RecButtonView>(R.id.button_activate).also {
            it.setOnClickListener {
                toggleRecording()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        update()
    }

    private fun toggleRecording() {
        if (!ourActivity.ourApplication.recording.isRecording()) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun startRecording() {
        Log.v(javaClass.simpleName, "startRecording")

        Dexter.withContext(activity)
            .withPermission(android.Manifest.permission.RECORD_AUDIO)
            .withListener(object : BasePermissionListener() {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    try {
                        ourActivity.ourApplication.recording.startRecording()
                        update()
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
            })
            .check();
    }

    private fun stopRecording() {
        Log.v(javaClass.simpleName, "stopRecording")

        ourActivity.ourApplication.recording.stopRecording()
        ourActivity.ourApplication.recording.discardRecording()
        update()
    }

    private fun update() {
        buttonRecord.isActivated = ourActivity.ourApplication.recording.isRecording()

        val ft = childFragmentManager.beginTransaction()

        if (buttonRecord.isActivated) {
            ft.replace(R.id.content, RecordingFragment())
        } else {
            ft.replace(R.id.content, DeactivatedFragment())
        }

        ft.commit()
    }
}