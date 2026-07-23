package com.example.persistenciadual.ui

import com.example.persistenciadual.domain.StorageEngine
import com.example.persistenciadual.domain.TaskItem

data class TaskUiState(
    val items: List<TaskItem> = emptyList(),
    val activeEngine: StorageEngine = StorageEngine.SQL,
    val title: String = "",
    val description: String = "",
    val editingId: Long? = null,
    val isBusy: Boolean = false,
    val message: String? = null
) {
    val isEditing: Boolean
        get() = editingId != null
}