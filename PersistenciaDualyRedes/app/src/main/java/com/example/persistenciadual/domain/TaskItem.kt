package com.example.persistenciadual.domain

data class TaskItem(
    val id: Long = 0L,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
