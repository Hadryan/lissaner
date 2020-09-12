package com.daniel_araujo.lissaner.android.ui

import android.content.Context
import android.util.AttributeSet
import io.noties.markwon.Markwon

class MarkdownView : androidx.appcompat.widget.AppCompatTextView {
    var markwon: Markwon? = null

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
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (markwon == null) {
            markwon = Markwon.create(context)
        }

        val markdown = markwon!!.toMarkdown(text as String);
        super.setText(markdown, BufferType.SPANNABLE);
    }
}
