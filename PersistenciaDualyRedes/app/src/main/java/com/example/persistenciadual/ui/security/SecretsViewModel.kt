package com.example.persistenciadual.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistenciadual.data.security.SecretRepository
import com.example.persistenciadual.data.security.SecretStorageType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SecretsViewModel(
    private val repository: SecretRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(SecretsUiState())

    val uiState: StateFlow<SecretsUiState> =
        _uiState.asStateFlow()

    fun onKeyChanged(value: String) {
        _uiState.update {
            it.copy(
                key = value,
                recoveredValue = null,
                message = null
            )
        }
    }

    fun onValueChanged(value: String) {
        _uiState.update {
            it.copy(
                value = value,
                recoveredValue = null,
                message = null
            )
        }
    }

    fun onStorageChanged(
        storageType: SecretStorageType
    ) {
        _uiState.update {
            it.copy(
                selectedStorage = storageType,
                recoveredValue = null,
                message = null
            )
        }
    }

    fun save() {
        val currentState = _uiState.value
        val key = currentState.key.trim()
        val value = currentState.value

        if (key.isBlank()) {
            _uiState.update {
                it.copy(
                    message = "La llave es obligatoria"
                )
            }
            return
        }

        if (value.isBlank()) {
            _uiState.update {
                it.copy(
                    message = "El valor es obligatorio"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    recoveredValue = null,
                    message = "Guardando..."
                )
            }

            try {
                repository.save(
                    storageType =
                        currentState.selectedStorage,
                    key = key,
                    value = value
                )

                _uiState.update {
                    it.copy(
                        message =
                            "Dato guardado correctamente en " +
                                    currentState
                                        .selectedStorage
                                        .displayName
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        message =
                            exception.message
                                ?: "No se pudo guardar el dato"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isBusy = false)
                }
            }
        }
    }

    fun recover() {
        val currentState = _uiState.value
        val key = currentState.key.trim()

        if (key.isBlank()) {
            _uiState.update {
                it.copy(
                    message = "Ingrese la llave a recuperar"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBusy = true,
                    recoveredValue = null,
                    message = "Buscando el dato..."
                )
            }

            try {
                val recoveredValue =
                    repository.recover(
                        storageType =
                            currentState.selectedStorage,
                        key = key
                    )

                if (recoveredValue == null) {
                    _uiState.update {
                        it.copy(
                            value = "",
                            recoveredValue = null,
                            message =
                                "El secreto no fue encontrado"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            value = recoveredValue,
                            recoveredValue = recoveredValue,
                            message =
                                "Dato recuperado correctamente"
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        recoveredValue = null,
                        message =
                            exception.message
                                ?: "No se pudo recuperar el dato"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isBusy = false)
                }
            }
        }
    }

    fun clearValue() {
        _uiState.update {
            it.copy(
                value = "",
                recoveredValue = null,
                message = null
            )
        }
    }
}