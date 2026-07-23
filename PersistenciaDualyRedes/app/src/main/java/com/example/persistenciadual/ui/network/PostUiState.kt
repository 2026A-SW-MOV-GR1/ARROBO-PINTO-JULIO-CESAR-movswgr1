package com.example.persistenciadual.ui.network

data class PostUiState(
    val idInput: String = "",
    val loadedPostId: Int? = null,
    val userId: Int = 1,
    val title: String = "",
    val body: String = "",
    val isLoading: Boolean = false,
    val statusCode: Int? = null,
    val statusMessage: String? = null
) {
    val canUpdate: Boolean
        get() = loadedPostId != null &&
                title.isNotBlank() &&
                body.isNotBlank() &&
                !isLoading
}