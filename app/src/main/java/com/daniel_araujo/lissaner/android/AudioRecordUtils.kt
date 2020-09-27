package com.daniel_araujo.lissaner.android

import android.media.AudioFormat
import android.media.AudioRecord

object AudioRecordUtils {
    /**
     * Queries the hardware for supported sample rates.
     */
    fun supportedSampleRates(): List<Int> {
        // List of sample rates that we would like to list.
        val validSampleRates = intArrayOf(
            8000, 11025, 16000, 22050, 32000, 37800,
            44056, 44100, 47250, 48000, 50000, 50400,
            64000, 88200, 96000, 176400, 192000, 352800
        )

        return validSampleRates.filter {
            val result = AudioRecord.getMinBufferSize(
                it,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT
            )

            // Good.
            result > 0
        }
    }

    /**
     * Queries the hardware for supported number of bits per sample for PCM.
     */
    fun supportedPcmBits(): List<Int> {
        // List of sample rates that we would like to list.
        val bits = ArrayList<Int>();

        // 8 Bits.
        run {
            val result = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_8BIT
            )

            if (result > 0) {
                bits.add(8)
            }
        }

        // 16 Bits.
        run {
            val result = AudioRecord.getMinBufferSize(
                44100,
                AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT
            )

            if (result > 0) {
                bits.add(16)
            }
        }

        return bits;
    }
}