package com.example.persistenciadual.data.security

enum class SecretStorageType(
    val displayName: String,
    val description: String
) {
    SHARED_PREFERENCES(
        displayName = "SharedPreferences",
        description =
            "Clave-valor simple en texto no cifrado."
    ),

    DATASTORE(
        displayName = "Preferences DataStore",
        description =
            "Clave-valor asíncrono mediante Coroutines y Flow."
    ),

    ENCRYPTED_SHARED_PREFERENCES(
        displayName = "EncryptedSharedPreferences",
        description =
            "Llaves y valores cifrados antes de guardarse."
    )
}