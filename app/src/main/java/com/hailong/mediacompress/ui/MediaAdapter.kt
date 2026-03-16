package com.hailong.mediacompress.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hailong.mediacompress.R
import com.hailong.mediacompress.model.CompressionStatus
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType

class MediaAdapter : ListAdapter<MediaItem, MediaAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val sizeTextView: TextView = itemView.findViewById(R.id.sizeTextView)

        fun bind(item: MediaItem) {
            nameTextView.text = item.name
            statusTextView.text = item.status.name
            progressBar.visibility = if (item.status == CompressionStatus.PROCESSING) View.VISIBLE else View.GONE
            progressBar.progress = (item.progress * 100).toInt()
            
            val originalSize = formatSize(item.size)
            val compressedSize = if (item.compressedSize > 0) formatSize(item.compressedSize) else ""
            sizeTextView.text = if (compressedSize.isNotEmpty()) "$originalSize -> $compressedSize" else originalSize
        }

        private fun formatSize(size: Long): String {
            val kb = size / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1) String.format("%.2f MB", mb) else String.format("%.2f KB", kb)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaItem>() {
        override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem == newItem
    }
}
