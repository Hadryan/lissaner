package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.R

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

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewDetachedFromWindow(v: View?) {
            }

            override fun onViewAttachedToWindow(v: View?) {
                if (ourActivity.ourApplication.initialized) {
                    Log.w(
                        "SplashFragment",
                        "Did not expect this to run when already initialized. Something else must have called initialize"
                    )
                }

                initialize()
                move()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        Log.i(
            "SplashFragment",
            "Application initialization status: " + ourActivity.ourApplication.initialized
        )

        if (ourActivity.ourApplication.initialized) {
            Log.w("SplashFragment", "Already initialized.")
            // Already loaded.
            move()
        }
    }

    /**
     * App initialization code. This code is run while an animated loading screen is up.
     */
    private fun initialize() {
        ourActivity.ourApplication.initialize()
    }

    /**
     * Ends splash screen.
     */
    private fun move() {
        Log.d("SplashFragment", "Move")
        findNavController().navigate(R.id.enter_app)
    }
}