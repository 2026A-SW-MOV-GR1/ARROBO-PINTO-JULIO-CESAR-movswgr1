package com.example.persistenciadual.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistenciadual.data.DualTaskRepository
import com.example.persistenciadual.domain.StorageEngine
import com.example.persistenciadual.domain.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: DualTaskRepository
) : ViewModel() {

    private data class FormState(
        val title: String = "",
        val description: String = "",
        val editingItem: TaskItem? = null
    )

    private val formState = MutableStateFlow(FormState())
    private val isBusy = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState = combine(
        repository.items,
        repository.activeEngine,
        formState,
        isBusy,
        message
    ) { items, engine, form, busy, currentMessage ->

        TaskUiState(
            items = items,
            activeEngine = engine,
            title = form.title,
            description = form.description,
            editingId = form.editingItem?.id,
            isBusy = busy,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskUiState()
    )

    fun onTitleChanged(value: String) {
        formState.value = formState.value.copy(
            title = value
        )
    }

    fun onDescriptionChanged(value: String) {
        formState.value = formState.value.copy(
            description = value
        )
    }

    fun onEngineChanged(useNoSql: Boolean) {
        val selectedEngine = if (useNoSql) {
            StorageEngine.NOSQL
        } else {
            StorageEngine.SQL
        }

        // Se cancela cualquier edición para evitar actualizar
        // accidentalmente un registro del otro almacenamiento.
        clearForm()

        repository.switchEngine(selectedEngine)
    }

    fun save() {
        val currentForm = formState.value

        if (currentForm.title.isBlank()) {
            message.value = "El título es obligatorio"
            return
        }

        viewModelScope.launch {
            isBusy.value = true

            try {
                val editingItem = currentForm.editingItem

                if (editingItem == null) {
                    repository.insert(
                        TaskItem(
                            title = currentForm.title.trim(),
                            description =
                                currentForm.description.trim()
                        )
                    )

                    message.value = "Registro guardado correctamente"
                } else {
                    repository.update(
                        editingItem.copy(
                            title = currentForm.title.trim(),
                            description =
                                currentForm.description.trim()
                        )
                    )

                    message.value =
                        "Registro actualizado correctamente"
                }

                clearForm()
            } catch (exception: Exception) {
                message.value =
                    exception.message ?: "Error al guardar"
            } finally {
                isBusy.value = false
            }
        }
    }

    fun edit(item: TaskItem) {
        formState.value = FormState(
            title = item.title,
            description = item.description,
            editingItem = item
        )
    }

    fun cancelEditing() {
        clearForm()
    }

    fun toggleCompleted(item: TaskItem) {
        viewModelScope.launch {
            isBusy.value = true

            try {
                repository.update(
                    item.copy(
                        completed = !item.completed
                    )
                )

                message.value = "Estado actualizado"
            } catch (exception: Exception) {
                message.value =
                    exception.message ?: "Error al actualizar"
            } finally {
                isBusy.value = false
            }
        }
    }

    fun delete(item: TaskItem) {
        viewModelScope.launch {
            isBusy.value = true

            try {
                repository.delete(item)

                if (formState.value.editingItem?.id == item.id) {
                    clearForm()
                }

                message.value = "Registro eliminado"
            } catch (exception: Exception) {
                message.value =
                    exception.message ?: "Error al eliminar"
            } finally {
                isBusy.value = false
            }
        }
    }

    fun clearMessage() {
        message.value = null
    }

    private fun clearForm() {
        formState.value = FormState()
    }
}