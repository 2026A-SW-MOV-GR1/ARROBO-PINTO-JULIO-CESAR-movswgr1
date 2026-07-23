package com.example.persistenciadual.data.nosql

import android.content.Context
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
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class JsonTaskRepository(
    context: Context,
    private val logger: AppLogger,
    applicationScope: CoroutineScope,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : TaskRepository {

    private val file = File(
        context.filesDir,
        FILE_NAME
    )

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
                    "ERROR al cargar inicialmente el archivo NoSQL",
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
                    "DEBUG NoSQL recargado. Documentos=${_items.value.size}"
                )
            }
        }
    }

    override suspend fun insert(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val currentItems = readItemsFromFile().toMutableList()

                val nextId = (
                        currentItems.maxOfOrNull { it.id } ?: 0L
                        ) + 1L

                currentItems.add(
                    0,
                    item.copy(id = nextId)
                )

                writeItemsToFile(currentItems)
                _items.value = currentItems

                logger.info(
                    TAG,
                    "INFO inserción NoSQL correcta. id=$nextId"
                )
            }
        }
    }

    override suspend fun update(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val currentItems = readItemsFromFile().toMutableList()
                val index = currentItems.indexOfFirst {
                    it.id == item.id
                }

                if (index == -1) {
                    throw IllegalStateException(
                        "No se encontró el documento NoSQL ${item.id}"
                    )
                }

                currentItems[index] = item
                currentItems.sortByDescending { it.createdAt }

                writeItemsToFile(currentItems)
                _items.value = currentItems

                logger.info(
                    TAG,
                    "INFO actualización NoSQL correcta. id=${item.id}"
                )
            }
        }
    }

    override suspend fun delete(item: TaskItem) {
        withContext(ioDispatcher) {
            mutex.withLock {
                val currentItems = readItemsFromFile().toMutableList()

                val removed = currentItems.removeAll {
                    it.id == item.id
                }

                if (!removed) {
                    throw IllegalStateException(
                        "No se encontró el documento NoSQL ${item.id}"
                    )
                }

                writeItemsToFile(currentItems)
                _items.value = currentItems

                logger.info(
                    TAG,
                    "INFO eliminación NoSQL correcta. id=${item.id}"
                )
            }
        }
    }

    private fun reloadLocked() {
        _items.value = readItemsFromFile()
            .sortedByDescending { it.createdAt }
    }

    private fun readItemsFromFile(): List<TaskItem> {
        if (!file.exists()) {
            return emptyList()
        }

        val content = file.readText()

        if (content.isBlank()) {
            return emptyList()
        }

        val jsonArray = JSONArray(content)
        val result = mutableListOf<TaskItem>()

        for (position in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(position)

            result.add(
                TaskItem(
                    id = jsonObject.getLong(KEY_ID),
                    title = jsonObject.getString(KEY_TITLE),
                    description = jsonObject.optString(
                        KEY_DESCRIPTION,
                        ""
                    ),
                    completed = jsonObject.optBoolean(
                        KEY_COMPLETED,
                        false
                    ),
                    createdAt = jsonObject.optLong(
                        KEY_CREATED_AT,
                        System.currentTimeMillis()
                    )
                )
            )
        }

        return result
    }

    private fun writeItemsToFile(items: List<TaskItem>) {
        val jsonArray = JSONArray()

        items.forEach { item ->
            val jsonObject = JSONObject().apply {
                put(KEY_ID, item.id)
                put(KEY_TITLE, item.title)
                put(KEY_DESCRIPTION, item.description)
                put(KEY_COMPLETED, item.completed)
                put(KEY_CREATED_AT, item.createdAt)
            }

            jsonArray.put(jsonObject)
        }

        file.writeText(jsonArray.toString(2))
    }

    companion object {
        private const val TAG = "JsonRepository"
        private const val FILE_NAME = "tasks_nosql.json"

        private const val KEY_ID = "id"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_COMPLETED = "completed"
        private const val KEY_CREATED_AT = "createdAt"
    }
}