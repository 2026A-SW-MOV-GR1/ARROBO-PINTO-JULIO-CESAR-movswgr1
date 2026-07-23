package com.example.persistenciadual.data

import com.example.persistenciadual.core.AppLogger
import com.example.persistenciadual.domain.StorageEngine
import com.example.persistenciadual.domain.TaskItem
import com.example.persistenciadual.domain.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

class DualTaskRepository(
    private val sqlRepository: TaskRepository,
    private val noSqlRepository: TaskRepository,
    private val logger: AppLogger
) {

    private val _activeEngine =
        MutableStateFlow(StorageEngine.SQL)

    val activeEngine: StateFlow<StorageEngine> =
        _activeEngine.asStateFlow()

    val items: Flow<List<TaskItem>> = combine(
        activeEngine,
        sqlRepository.items,
        noSqlRepository.items
    ) { engine, sqlItems, noSqlItems ->

        when (engine) {
            StorageEngine.SQL -> sqlItems
            StorageEngine.NOSQL -> noSqlItems
        }
    }

    fun switchEngine(engine: StorageEngine) {
        if (_activeEngine.value == engine) {
            return
        }

        val previousEngine = _activeEngine.value
        _activeEngine.value = engine

        logger.info(
            TAG,
            "INFO cambio de motor: $previousEngine -> $engine"
        )
    }

    suspend fun insert(item: TaskItem) {
        executeOperation("INSERT") {
            activeRepository().insert(item)
        }
    }

    suspend fun update(item: TaskItem) {
        executeOperation("UPDATE") {
            activeRepository().update(item)
        }
    }

    suspend fun delete(item: TaskItem) {
        executeOperation("DELETE") {
            activeRepository().delete(item)
        }
    }

    suspend fun refreshActiveRepository() {
        executeOperation("REFRESH") {
            activeRepository().refresh()
        }
    }

    private fun activeRepository(): TaskRepository {
        return when (_activeEngine.value) {
            StorageEngine.SQL -> sqlRepository
            StorageEngine.NOSQL -> noSqlRepository
        }
    }

    private suspend fun executeOperation(
        operation: String,
        action: suspend () -> Unit
    ) {
        val engine = _activeEngine.value

        try {
            logger.debug(
                TAG,
                "DEBUG inicio $operation. engine=$engine"
            )

            action()

            logger.info(
                TAG,
                "INFO $operation completado. engine=$engine"
            )
        } catch (exception: Exception) {
            logger.error(
                TAG,
                "ERROR en $operation. engine=$engine",
                exception
            )

            throw exception
        }
    }

    companion object {
        private const val TAG = "DualRepository"
    }
}