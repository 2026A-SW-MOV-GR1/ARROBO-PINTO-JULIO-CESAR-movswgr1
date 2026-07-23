package com.example.persistenciadual.domain
import kotlinx.coroutines.flow.StateFlow

interface TaskRepository {
    val items: StateFlow<List<TaskItem>>

    suspend fun insert(item: TaskItem)

    suspend fun update(item: TaskItem)

    suspend fun delete(item: TaskItem)

    suspend fun refresh()
}