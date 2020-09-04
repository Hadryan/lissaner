package com.daniel_araujo.always_recording_microphone.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.daniel_araujo.always_recording_microphone.R


class FilesFragment : Fragment() {
    inner class FileListAdapter : BaseAdapter {
        private val files: List<String>

        constructor(files: List<String>) {
            this.files = files.sorted()
        }

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View? {
            var view = if (convertView != null) convertView as ItemFileView else ItemFileView(context!!)
            var fileName = getItem(position) as String

            view.fileName = fileName
            view.fileTimestamp = ourActivity.ourApplication.recordingFiles.timestamp(fileName)

            return view
        }

        override fun getItem(position: Int): Any {
            return files[position]
        }

        override fun getItemId(position: Int): Long {
            return files[position].hashCode().toLong()
        }

        override fun getCount(): Int {
            return files.size
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_files, container, false)
    }

    override fun onResume() {
        super.onResume()

        // Updates list.
        view?.findViewById<ListView>(R.id.file_list)?.adapter = FileListAdapter(ourActivity.ourApplication.recordingFiles.list())
    }
}