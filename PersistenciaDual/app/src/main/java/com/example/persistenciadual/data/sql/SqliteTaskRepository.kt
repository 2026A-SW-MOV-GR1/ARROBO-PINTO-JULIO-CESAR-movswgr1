package com.example.persistenciadual.data.sql

import com.example.persistenciadual.core.AppLogger
import com.example.persistenciadual.domain.TaskItem
import com.example.persistenciadual.domain.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SqliteTaskRepository(
    private val helper: TaskSqliteHelper,
    private val logger: AppLogger,
    applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    private val mutex = Mutex()

    private val _items = MutableStateFlow<List<TaskItem>>(emptyList())
    override val items: StateFlow<List<TaskItem>> = _items.asStateFlow()

    init {
        applicationScope.launch {
            try {
                refresh()
            } catch (exception: Exception) {
                logger.error(
                    TAG,
                    "ERROR al cargar inicialmente SQLite",
                    exception
                )
            }
        }
    }

    override suspend fun refresh() {
        withContext(ioDispatcher) {
            mutex.withLock {
                reloadLocked()
                logger.debug(
                    TAG,
                    "DEBUG SQLite recargado. Registros=${_items.value.size}"
                )
            }
        }
    }

    override suspend fun insert(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val generatedId = helper.insert(item)
                reloadLocked()

                logger.info(
                    TAG,
                    "INFO inserción SQLite correcta. id=$generatedId"
                )
            }
        }
    }

    override suspend fun update(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val affectedRows = helper.update(item)

                if (affectedRows == 0) {
                    throw IllegalStateException(
                        "No se encontró el registro SQLite ${item.id}"
                    )
                }

                reloadLocked()

                logger.info(
                    TAG,
                    "INFO actualización SQLite correcta. id=${item.id}"
                )
            }
        }
    }

    override suspend fun delete(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val affectedRows = helper.delete(item.id)

                if (affectedRows == 0) {
                    throw IllegalStateException(
                        "No se encontró el registro SQLite ${item.id}"
                    )
                }

                reloadLocked()

                logger.info(
                    TAG,
                    "INFO eliminación SQLite correcta. id=${item.id}"
                )
            }
        }
    }

    private fun reloadLocked() {
        _items.value = helper.readAll()
    }

    companion object {
        private const val TAG = "SQLiteRepository"
    }
}