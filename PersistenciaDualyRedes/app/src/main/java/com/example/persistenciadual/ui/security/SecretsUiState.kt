package com.example.persistenciadual.ui.security

import com.example.persistenciadual.data.security.SecretStorageType

data class SecretsUiState(
    val key: String = "",
    val value: String = "",
    val selectedStorage:
    SecretStorageType =
        SecretStorageType.SHARED_PREFERENCES,
    val isBusy: Boolean = false,
    val recoveredValue: String? = null,
    val message: String? = null
)