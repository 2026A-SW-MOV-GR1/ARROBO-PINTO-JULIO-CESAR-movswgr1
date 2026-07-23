package com.example.persistenciadual.di

import android.content.Context
import com.example.persistenciadual.core.AndroidAppLogger
import com.example.persistenciadual.data.DualTaskRepository
import com.example.persistenciadual.data.network.NetworkModule
import com.example.persistenciadual.data.network.PostRepository
import com.example.persistenciadual.data.nosql.JsonTaskRepository
import com.example.persistenciadual.data.security.SecretRepository
import com.example.persistenciadual.data.sql.SqliteTaskRepository
import com.example.persistenciadual.data.sql.TaskSqliteHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(
    context: Context
) {

    private val applicationScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val logger = AndroidAppLogger()

    private val sqliteHelper =
        TaskSqliteHelper(context)

    private val sqliteRepository =
        SqliteTaskRepository(
            helper = sqliteHelper,
            logger = logger,
            applicationScope = applicationScope
        )

    private val jsonRepository =
        JsonTaskRepository(
            context = context,
            logger = logger,
            applicationScope = applicationScope
        )

    val dualTaskRepository =
        DualTaskRepository(
            sqlRepository = sqliteRepository,
            noSqlRepository = jsonRepository,
            logger = logger
        )

    val postRepository =
        PostRepository(
            apiService =
                NetworkModule.postApiService,
            logger = logger
        )

    val secretRepository =
        SecretRepository(
            context = context,
            logger = logger
        )
}