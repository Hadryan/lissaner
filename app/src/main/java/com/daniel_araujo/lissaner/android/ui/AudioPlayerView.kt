package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.Toast
import com.daniel_araujo.lissaner.R
import com.daniel_araujo.lissaner.android.LongClickRepeatTouchListener
import java.io.FileDescriptor

class AudioPlayerView : FrameLayout {
    private open class State {
    }

    /**
     * In this state the player is not interactive. The view may not be attached to a window.
     */
    private inner class StateDead : State() {
    }

    /**
     * The player is waiting for a file to be loaded.
     */
    private inner class StateIdle : State() {
    }

    /**
     * A file is being loaded.
     */
    private inner class StateLoading : State() {
    }

    /**
     * A file has been loaded to the player. The user can play it.
     */
    private inner class StateLoaded : State() {
        var playingState: State = StateLoadedPaused()
    }

    /**
     * The file is loaded but is not being played.
     */
    private inner class StateLoadedPaused : State() {
    }

    /**
     * The file is loaded and is being played.
     */
    private inner class StateLoadedPlaying : State() {
        val seekBarProgressUpdater: Runnable = object : Runnable {
            override fun run() {
                if (state !is StateLoaded) {
                    return;
                }

                val loadedState = state as StateLoaded

                if (loadedState.playingState !is StateLoadedPlaying) {
                    return;
                }

                syncSeekBarProgress()

                handler.removeCallbacks(this)
                handler.postDelayed(this, PLAYING_SEEK_BAR_UPDATE_INTERVAL)
            }
        }
    }
    
    private val PLAYING_SEEK_BAR_UPDATE_INTERVAL: Long = 1000

    private val FF_VALUE: Int = 5000

    /**
     * The media player object. It belongs to this view so the view is responsible for destroying
     * it.
     */
    private lateinit var mediaPlayer: MediaPlayer

    /**
     * The current state of the player.
     */
    private var state: State = StateDead()

    /**
     * The play and pause button.
     */
    private lateinit var playButton: ImageButton

    /**
     * The rewind button.
     */
    private lateinit var rewindButton: ImageButton

    /**
     * The fast forward button.
     */
    private lateinit var fastforwardButton: ImageButton

    /**
     * The seek bar.
     */
    private lateinit var seekBar: SeekBar

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    /**
     * Loads file and sets up player play it.
     */
    fun load(fd: FileDescriptor) {
        if (state is StateLoaded) {
            mediaPlayer.reset()
        }

        state = StateIdle()

        mediaPlayer.setDataSource(fd)
        mediaPlayer.prepareAsync()

        state = StateLoading()

        updateControls()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        mediaPlayer = MediaPlayer()

        mediaPlayer.setOnPreparedListener { _ ->
            state = StateLoaded()

            updateControls()
        }

        mediaPlayer.setOnErrorListener { _: MediaPlayer, what: Int, extra: Int ->
            if (state is StateLoading) {
                Log.e(javaClass.simpleName, "Failed to load. Error code: $what (extra: $extra)")

                Toast.makeText(context, context.getString(R.string.audio_player_error_loading), Toast.LENGTH_SHORT).show()

                state = StateIdle()
                return@setOnErrorListener true
            } else {
                Log.e(javaClass.simpleName, "Media player error code: $what (extra: $extra)")
                return@setOnErrorListener false
            }
        }

        mediaPlayer.setOnCompletionListener {
            if (state !is StateLoaded) {
                return@setOnCompletionListener
            }

            val loadedState = state as StateLoaded

            loadedState.playingState = StateLoadedPaused()

            updateControls()
        }

        state = StateIdle()

        updateControls()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        state = StateDead()

        mediaPlayer.release()

        // No need to update UI because it is not visible.
    }

    @Suppress("UNUSED_PARAMETER")
    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_audio_player, this)

        playButton = findViewById<ImageButton>(R.id.play_button)
        rewindButton = findViewById<ImageButton>(R.id.rewind_button)
        fastforwardButton = findViewById<ImageButton>(R.id.fastforward_button)
        seekBar = findViewById<SeekBar>(R.id.seek_bar)

        playButton.setOnClickListener {
            togglePlay()
        }

        rewindButton.setOnTouchListener(LongClickRepeatTouchListener())
        rewindButton.setOnClickListener {
            rewind()
        }
        rewindButton.setOnLongClickListener {
            rewind()
            return@setOnLongClickListener true
        }

        fastforwardButton.setOnTouchListener(LongClickRepeatTouchListener())
        fastforwardButton.setOnClickListener {
            fastforward()
        }
        fastforwardButton.setOnLongClickListener {
            fastforward()
            return@setOnLongClickListener true
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (state !is StateLoaded) {
                    return;
                }

                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun updateControls() {
        if (state is StateLoaded) {
            playButton.isEnabled = true
            rewindButton.isEnabled = true
            fastforwardButton.isEnabled = true

            val loadedState = state as StateLoaded

            if (loadedState.playingState is StateLoadedPlaying) {
                playButton.setImageResource(R.drawable.ic_audio_pause)
            } else {
                playButton.setImageResource(R.drawable.ic_audio_play)
            }

            seekBar.isEnabled = true
            seekBar.max = mediaPlayer.duration
            syncSeekBarProgress()
        } else {
            playButton.isEnabled = false
            playButton.setImageResource(R.drawable.ic_audio_play)
            rewindButton.isEnabled = false
            fastforwardButton.isEnabled = false

            seekBar.isEnabled = false
            seekBar.max = 0
            seekBar.progress = 0
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun syncSeekBarProgress() {
        seekBar.progress = mediaPlayer.currentPosition
    }

    private fun togglePlay() {
        if (state !is StateLoaded) {
            return
        }

        val loadedState = state as StateLoaded

        if (loadedState.playingState is StateLoadedPaused) {
            mediaPlayer.start()

            val playingState = StateLoadedPlaying()

            loadedState.playingState = playingState

            playingState.seekBarProgressUpdater.run()
        } else {
            mediaPlayer.pause()

            loadedState.playingState = StateLoadedPaused()
        }

        updateControls()
    }

    private fun rewind() {
        if (state !is StateLoaded) {
            return
        }

        mediaPlayer.seekTo(mediaPlayer.currentPosition - FF_VALUE)

        syncSeekBarProgress()
    }

    private fun fastforward() {
        if (state !is StateLoaded) {
            return
        }

        mediaPlayer.seekTo(mediaPlayer.currentPosition + FF_VALUE)

        syncSeekBarProgress()
    }
}
