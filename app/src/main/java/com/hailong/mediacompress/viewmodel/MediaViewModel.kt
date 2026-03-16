package com.hailong.mediacompress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.repository.MediaRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    val compressionTasks: StateFlow<List<MediaItem>> = repository.compressionTasks

    fun addTasks(items: List<MediaItem>) {
        repository.addTasks(items)
    }

    fun startCompression(quality: Int, maxWidth: Int, maxHeight: Int, crf: Int, scale: String?) {
        viewModelScope.launch {
            compressionTasks.value.forEach { item ->
                if (item.type == MediaType.IMAGE) {
                    repository.compressImage(item, quality, maxWidth, maxHeight)
                } else if (item.type == MediaType.VIDEO) {
                    repository.compressVideo(item, crf, scale)
                }
            }
        }
    }
}
