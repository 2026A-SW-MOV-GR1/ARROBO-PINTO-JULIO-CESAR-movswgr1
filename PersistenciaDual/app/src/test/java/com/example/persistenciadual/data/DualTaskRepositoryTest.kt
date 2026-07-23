package com.example.persistenciadual.data

import com.example.persistenciadual.core.AppLogger
import com.example.persistenciadual.domain.StorageEngine
import com.example.persistenciadual.domain.TaskItem
import com.example.persistenciadual.domain.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DualTaskRepositoryTest {

    @Test
    fun `insertar en SQL no modifica NoSQL`() = runTest {
        val sqlRepository = FakeTaskRepository()
        val noSqlRepository = FakeTaskRepository()

        val repository = DualTaskRepository(
            sqlRepository = sqlRepository,
            noSqlRepository = noSqlRepository,
            logger = NoOpLogger()
        )

        repository.insert(
            TaskItem(
                title = "Registro SQL",
                description = "Solo debe existir en SQLite"
            )
        )

        val sqlItems = repository.items.first()

        assertEquals(1, sqlItems.size)
        assertEquals("Registro SQL", sqlItems.first().title)

        repository.switchEngine(StorageEngine.NOSQL)

        val noSqlItems = repository.items.first()

        assertTrue(noSqlItems.isEmpty())
    }

    @Test
    fun `cambiar el motor muestra los datos del repositorio seleccionado`() =
        runTest {
            val sqlRepository = FakeTaskRepository()
            val noSqlRepository = FakeTaskRepository()

            sqlRepository.insert(
                TaskItem(
                    title = "Dato de SQLite",
                    description = "Persistencia relacional"
                )
            )

            noSqlRepository.insert(
                TaskItem(
                    title = "Dato de JSON",
                    description = "Persistencia documental"
                )
            )

            val repository = DualTaskRepository(
                sqlRepository = sqlRepository,
                noSqlRepository = noSqlRepository,
                logger = NoOpLogger()
            )

            val initialItems = repository.items.first()

            assertEquals(
                "Dato de SQLite",
                initialItems.single().title
            )

            repository.switchEngine(StorageEngine.NOSQL)

            val switchedItems = repository.items.first()

            assertEquals(
                "Dato de JSON",
                switchedItems.single().title
            )
        }
}

private class FakeTaskRepository : TaskRepository {

    private val mutableItems =
        MutableStateFlow<List<TaskItem>>(emptyList())

    override val items: StateFlow<List<TaskItem>> =
        mutableItems.asStateFlow()

    private var nextId = 1L

    override suspend fun insert(item: TaskItem) {
        val newItem = item.copy(
            id = if (item.id == 0L) {
                nextId++
            } else {
                item.id
            }
        )

        mutableItems.value = listOf(newItem) +
                mutableItems.value
    }

    override suspend fun update(item: TaskItem) {
        mutableItems.value = mutableItems.value.map {
            if (it.id == item.id) {
                item
            } else {
                it
            }
        }
    }

    override suspend fun delete(item: TaskItem) {
        mutableItems.value = mutableItems.value.filterNot {
            it.id == item.id
        }
    }

    override suspend fun refresh() {
        // No requiere operación en memoria.
    }
}

private class NoOpLogger : AppLogger {

    override fun debug(tag: String, message: String) = Unit

    override fun info(tag: String, message: String) = Unit

    override fun error(
        tag: String,
        message: String,
        throwable: Throwable?
    ) = Unit
}