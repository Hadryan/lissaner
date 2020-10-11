package com.daniel_araujo.lissaner.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import com.daniel_araujo.lissaner.R
import java.util.ArrayList


class FilesFragment : Fragment() {
    /**
     * A reference to the file list view. Will be initialized when view is constructed.
     */
    private lateinit var fileList: ListView

    /**
     * A reference to the audio player view. Will be initialized when view is constructed.
     */
    private lateinit var audioPlayer: AudioPlayerView

    inner class FileListAdapter : BaseAdapter {
        val files: ArrayList<String>

        constructor(files: List<String>) {
            this.files = ArrayList(files.sorted().reversed())
        }

        override fun getView(position: Int, convertView: View?, container: ViewGroup): View? {
            // Remember, views are reused.

            var view = if (convertView != null) convertView as ItemFileView else ItemFileView(context!!)
            var fileName = getItem(position) as String

            view.fileName = fileName
            view.fileTimestamp = ourActivity.ourApplication.recordingFiles.timestamp(fileName)
            view.fileSize = ourActivity.ourApplication.recordingFiles.size(fileName)
            view.duration = ourActivity.ourApplication.recordingFiles.duration(fileName)?.div(1000)?.toInt()
            view.onDeleteListener = {
                ourActivity.ourApplication.recordingFiles.delete(fileName)
                removeItem(position)
            }

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

        private fun removeItem(position: Int) {
            files.removeAt(position)

            notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_files, container, false)
        fileList = view.findViewById<ListView>(R.id.file_list)
        audioPlayer = view.findViewById<AudioPlayerView>(R.id.audio_player)

        view.findViewById<TopView>(R.id.top).setLeftButton(R.drawable.ic_arrow_left, View.OnClickListener {
            findNavController().popBackStack()
        })

        return view
    }

    override fun onResume() {
        super.onResume()

        fileList.adapter = FileListAdapter(ourActivity.ourApplication.recordingFiles.list())
        fileList.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            val realAdapter = fileList.adapter as FileListAdapter

            val selectedFile = realAdapter.files[position]
            audioPlayer.load(ourActivity.ourApplication.recordingFiles.open(selectedFile)!!)
        }
    }
}