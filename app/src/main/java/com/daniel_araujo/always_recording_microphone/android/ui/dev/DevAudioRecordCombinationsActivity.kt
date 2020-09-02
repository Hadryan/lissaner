package com.daniel_araujo.always_recording_microphone.android.ui.dev

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import com.daniel_araujo.always_recording_microphone.R

class DevAudioRecordCombinationsActivity : AppCompatActivity() {
    class ListItemWithId<T>(val id: T, private val text: String = id.toString()) {
        override fun toString(): String {
            return text
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dev_audio_record_combinations)

        setupSource()
        setupSampleRate()
        setupChannel()
        setupEncoding()
    }

    private fun setupSource() {
        val spinner = findViewById<Spinner>(R.id.source_spinner)

        val list = ArrayList<ListItemWithId<Int>>()

        list.addAll(arrayOf(
            ListItemWithId(
                MediaRecorder.AudioSource.DEFAULT,
                MediaRecorder.AudioSource::DEFAULT.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.MIC,
                MediaRecorder.AudioSource::MIC.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.VOICE_UPLINK,
                MediaRecorder.AudioSource::VOICE_UPLINK.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.VOICE_DOWNLINK,
                MediaRecorder.AudioSource::VOICE_DOWNLINK.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.VOICE_CALL,
                MediaRecorder.AudioSource::VOICE_CALL.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.CAMCORDER,
                MediaRecorder.AudioSource::CAMCORDER.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                MediaRecorder.AudioSource::VOICE_RECOGNITION.name
            ),
            ListItemWithId(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                MediaRecorder.AudioSource::VOICE_COMMUNICATION.name
            )
        ))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.addAll(arrayOf(
                ListItemWithId(
                    MediaRecorder.AudioSource.UNPROCESSED,
                    MediaRecorder.AudioSource::UNPROCESSED.name
                )
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.addAll(arrayOf(
                ListItemWithId(
                    MediaRecorder.AudioSource.VOICE_PERFORMANCE,
                    MediaRecorder.AudioSource::VOICE_PERFORMANCE.name
                )
            ))
        }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                validate()
            }
        })
    }

    private fun setupSampleRate() {
        val spinner = findViewById<Spinner>(R.id.sample_rate_spinner)

        val available = arrayOf(
            ListItemWithId(
                AudioFormat.SAMPLE_RATE_UNSPECIFIED,
                AudioFormat::SAMPLE_RATE_UNSPECIFIED.name
            ),
            ListItemWithId(
                8000
            ),
            ListItemWithId(
                11025
            ),
            ListItemWithId(
                16000
            ),
            ListItemWithId(
                22050
            ),
            ListItemWithId(
                32000
            ),
            ListItemWithId(
                37800
            ),
            ListItemWithId(
                44100
            ),
            ListItemWithId(
                48000
            ),
            ListItemWithId(
                88200
            ),
            ListItemWithId(
                96000
            )
        )

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, available)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                validate()
            }
        })
    }

    private fun setupChannel() {
        val spinner = findViewById<Spinner>(R.id.channel_spinner)

        val available = arrayOf(
            ListItemWithId(
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat::CHANNEL_IN_MONO.name
            ),
            ListItemWithId(
                AudioFormat.CHANNEL_IN_STEREO,
                AudioFormat::CHANNEL_IN_STEREO.name
            )
        )

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, available )

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                validate()
            }
        })
    }

    private fun setupEncoding() {
        val spinner = findViewById<Spinner>(R.id.encoding_spinner)

        val list = ArrayList<ListItemWithId<Int>>()

        list.addAll(arrayOf(
            ListItemWithId(
                AudioFormat.ENCODING_DEFAULT,
                AudioFormat::ENCODING_DEFAULT.name
            ),
            ListItemWithId(
                AudioFormat.ENCODING_PCM_16BIT,
                AudioFormat::ENCODING_PCM_16BIT.name
            ),
            ListItemWithId(
                AudioFormat.ENCODING_PCM_8BIT,
                AudioFormat::ENCODING_PCM_8BIT.name
            ),
            ListItemWithId(
                AudioFormat.ENCODING_PCM_FLOAT,
                AudioFormat::ENCODING_PCM_FLOAT.name
            ),
            ListItemWithId(
                AudioFormat.ENCODING_AC3,
                AudioFormat::ENCODING_AC3.name
            ),
            ListItemWithId(
                AudioFormat.ENCODING_E_AC3,
                AudioFormat::ENCODING_E_AC3.name
            )
        ))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            list.addAll(arrayOf(
                ListItemWithId(
                    AudioFormat.ENCODING_DTS,
                    AudioFormat::ENCODING_DTS.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_DTS_HD,
                    AudioFormat::ENCODING_DTS_HD.name
                )
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            list.addAll(arrayOf(
                ListItemWithId(
                    AudioFormat.ENCODING_MP3,
                    AudioFormat::ENCODING_MP3.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AAC_LC,
                    AudioFormat::ENCODING_AAC_LC.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AAC_HE_V1,
                    AudioFormat::ENCODING_AAC_HE_V1.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AAC_HE_V2,
                    AudioFormat::ENCODING_AAC_HE_V2.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_IEC61937,
                    AudioFormat::ENCODING_IEC61937.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_DOLBY_TRUEHD,
                    AudioFormat::ENCODING_DOLBY_TRUEHD.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AAC_ELD,
                    AudioFormat::ENCODING_AAC_ELD.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AAC_XHE,
                    AudioFormat::ENCODING_AAC_XHE.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_AC4,
                    AudioFormat::ENCODING_AC4.name
                ),
                ListItemWithId(
                    AudioFormat.ENCODING_E_AC3_JOC,
                    AudioFormat::ENCODING_E_AC3_JOC.name
                )
            ))
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            list.addAll(arrayOf(
                ListItemWithId(
                    AudioFormat.ENCODING_DOLBY_MAT,
                    AudioFormat::ENCODING_DOLBY_MAT.name
                )
            ))
        }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)

        spinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not implemented")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                validate()
            }
        })
    }

    private fun validate() {
        val selectedSource = findViewById<Spinner>(R.id.source_spinner).selectedItem as ListItemWithId<Int>
        val selectedChannel = findViewById<Spinner>(R.id.channel_spinner).selectedItem as ListItemWithId<Int>
        val selectedEncoding = findViewById<Spinner>(R.id.encoding_spinner).selectedItem as ListItemWithId<Int>
        val selectedSampleRate = findViewById<Spinner>(R.id.sample_rate_spinner).selectedItem as ListItemWithId<Int>

        val result = findViewById<TextView>(R.id.result)

        try {
            val format = AudioFormat.Builder()
                .setEncoding(selectedEncoding.id)
                .setSampleRate(selectedSampleRate.id)
                .setChannelMask(selectedChannel.id)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val recorder = AudioRecord.Builder()
                    .setAudioSource(selectedSource.id)
                    .setAudioFormat(format)
                    .setBufferSizeInBytes(1 * 1000 * 1000)
                    .build()

                recorder.release()
            } else {
                throw Exception("The builder is not supported on this version of Android.");
            }

            result.setText("Accepted.")
            result.setBackgroundColor(65280)
        } catch (e: Exception) {
            result.setText(e.toString())
            result.setBackgroundColor(16711680)
        }

    }
}