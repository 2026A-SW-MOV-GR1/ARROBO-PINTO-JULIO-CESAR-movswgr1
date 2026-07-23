package com.example.persistenciadual.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.persistenciadual.core.AppLogger
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private val Context.secretDataStore:
        DataStore<Preferences> by preferencesDataStore(
    name = "secret_datastore"
)

@Suppress("DEPRECATION")
class SecretRepository(
    private val context: Context,
    private val logger: AppLogger,
    private val ioDispatcher: CoroutineDispatcher =
        Dispatchers.IO
) {

    private val sharedPreferences by lazy {
        context.getSharedPreferences(
            SHARED_PREFERENCES_FILE,
            Context.MODE_PRIVATE
        )
    }

    private val encryptedSharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(
                MasterKey.KeyScheme.AES256_GCM
            )
            .build()

        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFERENCES_FILE,
            masterKey,
            EncryptedSharedPreferences
                .PrefKeyEncryptionScheme
                .AES256_SIV,
            EncryptedSharedPreferences
                .PrefValueEncryptionScheme
                .AES256_GCM
        )
    }

    suspend fun save(
        storageType: SecretStorageType,
        key: String,
        value: String
    ) {
        withContext(ioDispatcher) {
            when (storageType) {
                SecretStorageType.SHARED_PREFERENCES -> {
                    val saved = sharedPreferences
                        .edit()
                        .putString(key, value)
                        .commit()

                    if (!saved) {
                        throw IOException(
                            "No se pudo guardar en SharedPreferences"
                        )
                    }
                }

                SecretStorageType.DATASTORE -> {
                    context.secretDataStore.edit {
                            preferences ->
                        preferences[
                            stringPreferencesKey(key)
                        ] = value
                    }
                }

                SecretStorageType
                    .ENCRYPTED_SHARED_PREFERENCES -> {

                    val saved =
                        encryptedSharedPreferences
                            .edit()
                            .putString(key, value)
                            .commit()

                    if (!saved) {
                        throw IOException(
                            "No se pudo guardar el dato cifrado"
                        )
                    }
                }
            }

            // Nunca se registran la llave ni el valor.
            logger.info(
                TAG,
                "INFO dato guardado mediante ${storageType.name}"
            )
        }
    }

    suspend fun recover(
        storageType: SecretStorageType,
        key: String
    ): String? {
        return withContext(ioDispatcher) {
            val result = when (storageType) {
                SecretStorageType.SHARED_PREFERENCES -> {
                    if (
                        sharedPreferences.contains(key)
                    ) {
                        sharedPreferences.getString(
                            key,
                            null
                        )
                    } else {
                        null
                    }
                }

                SecretStorageType.DATASTORE -> {
                    val preferences =
                        context.secretDataStore
                            .data
                            .first()

                    preferences[
                        stringPreferencesKey(key)
                    ]
                }

                SecretStorageType
                    .ENCRYPTED_SHARED_PREFERENCES -> {

                    if (
                        encryptedSharedPreferences
                            .contains(key)
                    ) {
                        encryptedSharedPreferences
                            .getString(key, null)
                    } else {
                        null
                    }
                }
            }

            logger.info(
                TAG,
                "INFO recuperación mediante " +
                        "${storageType.name}. " +
                        "Encontrado=${result != null}"
            )

            result
        }
    }

    companion object {
        private const val TAG = "SecretRepository"

        private const val SHARED_PREFERENCES_FILE =
            "simple_secrets"

        private const val ENCRYPTED_PREFERENCES_FILE =
            "encrypted_secrets"
    }
}