package com.daniel_araujo.lissaner.android.ui.dev

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.android.ui.RecButtonView

class DevRecButtonActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_rec_button)

        val button = findViewById<RecButtonView>(R.id.dev_rec_button);

        button.setOnClickListener {
            it.isActivated = !it.isActivated;
        }
    }
}