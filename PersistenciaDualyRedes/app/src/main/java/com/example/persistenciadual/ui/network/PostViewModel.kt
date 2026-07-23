package com.example.persistenciadual.ui.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.persistenciadual.data.network.ApiException
import com.example.persistenciadual.data.network.PostDto
import com.example.persistenciadual.data.network.PostRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostViewModel(
    private val repository: PostRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostUiState())

    val uiState: StateFlow<PostUiState> =
        _uiState.asStateFlow()

    fun onIdChanged(value: String) {
        val numericValue = value
            .filter { character ->
                character.isDigit()
            }
            .take(5)

        _uiState.update {
            it.copy(
                idInput = numericValue,
                loadedPostId = null,
                title = "",
                body = "",
                statusCode = null,
                statusMessage = null
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.update {
            it.copy(title = value)
        }
    }

    fun onBodyChanged(value: String) {
        _uiState.update {
            it.copy(body = value)
        }
    }

    fun getPost() {
        val id = _uiState.value.idInput.toIntOrNull()

        if (id == null || id <= 0) {
            _uiState.update {
                it.copy(
                    statusMessage =
                        "Ingrese un identificador numérico válido"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusCode = null,
                    statusMessage =
                        "Consultando el servidor..."
                )
            }

            try {
                val result = repository.getPost(id)

                _uiState.update {
                    it.copy(
                        loadedPostId = result.data.id,
                        userId = result.data.userId,
                        title = result.data.title,
                        body = result.data.body,
                        statusCode = result.statusCode,
                        statusMessage =
                            "Post obtenido correctamente"
                    )
                }
            } catch (exception: ApiException) {
                val message = if (
                    exception.statusCode == 404
                ) {
                    "No se encontró un post con ese ID"
                } else {
                    exception.message
                        ?: "Error HTTP ${exception.statusCode}"
                }

                _uiState.update {
                    it.copy(
                        loadedPostId = null,
                        title = "",
                        body = "",
                        statusCode = exception.statusCode,
                        statusMessage = message
                    )
                }
            } catch (exception: IOException) {
                _uiState.update {
                    it.copy(
                        statusMessage =
                            "No fue posible conectarse al servidor"
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage =
                            exception.message
                                ?: "Ocurrió un error inesperado"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun updatePost() {
        val currentState = _uiState.value
        val postId = currentState.loadedPostId

        if (postId == null) {
            _uiState.update {
                it.copy(
                    statusMessage =
                        "Primero debe obtener un post"
                )
            }
            return
        }

        if (
            currentState.title.isBlank() ||
            currentState.body.isBlank()
        ) {
            _uiState.update {
                it.copy(
                    statusMessage =
                        "El título y el contenido son obligatorios"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    statusCode = null,
                    statusMessage =
                        "Enviando actualización..."
                )
            }

            try {
                val post = PostDto(
                    userId = currentState.userId,
                    id = postId,
                    title = currentState.title.trim(),
                    body = currentState.body.trim()
                )

                val result = repository.updatePost(post)

                _uiState.update {
                    it.copy(
                        loadedPostId = result.data.id,
                        userId = result.data.userId,
                        title = result.data.title,
                        body = result.data.body,
                        statusCode = result.statusCode,
                        statusMessage =
                            "Actualización confirmada por el servidor"
                    )
                }
            } catch (exception: ApiException) {
                _uiState.update {
                    it.copy(
                        statusCode = exception.statusCode,
                        statusMessage =
                            exception.message
                                ?: "Error al actualizar"
                    )
                }
            } catch (exception: IOException) {
                _uiState.update {
                    it.copy(
                        statusMessage =
                            "No fue posible conectarse al servidor"
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        statusMessage =
                            exception.message
                                ?: "Ocurrió un error inesperado"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}