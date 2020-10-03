package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.android.Application
import com.daniel_araujo.lissaner.android.AudioRecordUtils
import com.daniel_araujo.lissaner.android.PreferenceUtils
import com.daniel_araujo.lissaner.android.SpinnerUtils
import com.shawnlin.numberpicker.NumberPicker

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SplashFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.addOnAttachStateChangeListener(object: View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
            }

            override fun onViewAttachedToWindow(v: View?) {
                if (ourActivity.ourApplication.splash) {
                    Log.e(javaClass.simpleName, "Did not expect this to run when already initialized.")
                    return
                }

                initialize()
                move()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        if (ourActivity.ourApplication.splash) {
            Log.w(javaClass.simpleName, "Already initialized.")
            // Already loaded.
            move()
        }
    }

    /**
     * App initialization code. This code is run while an animated loading screen is up.
     */
    private fun initialize() {
        Log.i(javaClass.simpleName, "Initializing.")

        val preferences = ourActivity.ourApplication.getDefaultSharedPreferences()

        with(preferences.edit()) {
            if (!PreferenceUtils.hasLong(preferences, Application.PREFERENCE_KEEP)) {
                putLong(Application.PREFERENCE_KEEP, 30 * 60 * 1000)
            }

            if (!PreferenceUtils.hasInt(preferences, Application.PREFERENCE_SAMPLES_PER_SECOND)) {
                putInt(Application.PREFERENCE_SAMPLES_PER_SECOND, 44100)
            }

            if (!PreferenceUtils.hasInt(preferences, Application.PREFERENCE_BITS_PER_SAMPLE)) {
                putInt(Application.PREFERENCE_BITS_PER_SAMPLE, 16)
            }

            commit()
        }

        ourActivity.ourApplication.splash = true
    }

    /**
     * Ends splash screen.
     */
    private fun move() {
        findNavController().navigate(R.id.enter_app)
    }
}