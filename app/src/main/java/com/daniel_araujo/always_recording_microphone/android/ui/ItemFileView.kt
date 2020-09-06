package com.daniel_araujo.always_recording_microphone.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.use
import com.daniel_araujo.always_recording_microphone.DateFormatUtils
import com.daniel_araujo.always_recording_microphone.R

/**
 * TODO: document your custom view class.
 */
class ItemFileView : FrameLayout {
    /**
     * The name of the file.
     */
    var fileName: String? = "filename.wav"
        set(value) {
            field = value
            updateFilename()
        }

    /**
     * The timestamp of the file.
     */
    var fileTimestamp: Long? = null
        set(value) {
            field = value
            updateFileTimestamp()
        }

    /**
     * The size of the file in bytes
     */
    var fileSize: Long? = null
        set(value) {
            field = value
            updateFileSize()
        }

    /**
     * Duration in seconds.
     */
    var duration: Int? = null
        set(value) {
            field = value
            updateDuration()
        }

    /**
     * Called when the user requests to permanently delete the file.
     */
    var onDeleteListener: (() -> Unit)? = null

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

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        inflate(context, R.layout.view_item_file, this)

        // Load attributes
        context.obtainStyledAttributes(attrs, R.styleable.ItemFileView, defStyle, 0).use {
            fileName = it.getString(R.styleable.ItemFileView_fileName)

            if (it.hasValue(R.styleable.ItemFileView_fileTimestamp)) {
                fileTimestamp = it.getInteger(R.styleable.ItemFileView_fileTimestamp, 0).toLong()
            } else {
                fileTimestamp = null
            }

            if (it.hasValue(R.styleable.ItemFileView_fileSize)) {
                fileSize = it.getInteger(R.styleable.ItemFileView_fileSize, 0).toLong()
            } else {
                fileSize = null
            }

            if (it.hasValue(R.styleable.ItemFileView_duration)) {
                duration = it.getInteger(R.styleable.ItemFileView_duration, 0)
            } else {
                duration = null
            }
        }

        findViewById<ImageButton>(R.id.more).setOnClickListener(OnClickListener {
            showPopup(it)
        })
    }

    fun updateFilename() {
        if (fileName != null) {
            findViewById<TextView>(R.id.file_name).text = fileName
        } else {
            findViewById<TextView>(R.id.file_name).text = "?"
        }
    }

    fun updateFileTimestamp() {
        if (fileTimestamp != null) {
            findViewById<TextView>(R.id.file_timestamp).text = DateFormatUtils.localeDateAndTime(fileTimestamp!!)
        } else {
            findViewById<TextView>(R.id.file_timestamp).text = ""
        }
    }

    fun updateFileSize() {
        if (fileSize != null) {
            findViewById<TextView>(R.id.file_size).text = android.text.format.Formatter.formatShortFileSize(context, fileSize!!)
        } else {
            findViewById<TextView>(R.id.file_size).text = ""
        }
    }

    fun updateDuration() {
        if (duration != null) {
            findViewById<TextView>(R.id.duration).text = DateFormatUtils.mmss(duration!!)
        } else {
            findViewById<TextView>(R.id.duration).text = ""
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(context, view)
        popup.inflate(R.menu.item_file_more_menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.delete -> {
                    onDeleteListener?.invoke()
                }
            }

            false
        })

        popup.show()
    }
}
