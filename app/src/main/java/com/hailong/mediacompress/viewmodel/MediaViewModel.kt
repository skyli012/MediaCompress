package com.hailong.mediacompress.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hailong.mediacompress.model.MediaItem
import com.hailong.mediacompress.model.MediaType
import com.hailong.mediacompress.repository.MediaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MediaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    val compressionTasks: StateFlow<List<MediaItem>> = repository.compressionTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedItems = MutableStateFlow<List<MediaItem>>(emptyList())
    val selectedItems: StateFlow<List<MediaItem>> = _selectedItems

    private val _keepOriginal = MutableStateFlow(true)
    val keepOriginal: StateFlow<Boolean> = _keepOriginal

    fun setKeepOriginal(keep: Boolean) {
        _keepOriginal.value = keep
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    fun addTasks(items: List<MediaItem>) {
        _selectedItems.value = items
        viewModelScope.launch {
            repository.addTasks(items)
        }
    }

    fun startImageCompression(quality: Int, format: String, scale: Float) {
        viewModelScope.launch {
            val itemsToCompress = _selectedItems.value.filter { it.type == MediaType.IMAGE }
            repository.compressImages(itemsToCompress, quality, format, scale, _keepOriginal.value)
        }
    }

    fun startVideoCompression(quality: String, resolution: String, removeAudio: Boolean) {
        viewModelScope.launch {
            _selectedItems.value.firstOrNull { it.type == MediaType.VIDEO }?.let { item ->
                repository.compressVideo(item, quality, resolution, removeAudio, _keepOriginal.value)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
