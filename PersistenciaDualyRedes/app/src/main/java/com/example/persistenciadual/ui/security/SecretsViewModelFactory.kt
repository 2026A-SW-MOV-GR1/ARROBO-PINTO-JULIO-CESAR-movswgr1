package com.example.persistenciadual.ui.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.persistenciadual.data.security.SecretRepository

class SecretsViewModelFactory(
    private val repository: SecretRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (
            modelClass.isAssignableFrom(
                SecretsViewModel::class.java
            )
        ) {
            @Suppress("UNCHECKED_CAST")
            return SecretsViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "ViewModel desconocido: ${modelClass.name}"
        )
    }
}