package com.example.persistenciadual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.persistenciadual.ui.AppRoot
import com.example.persistenciadual.ui.TaskViewModel
import com.example.persistenciadual.ui.TaskViewModelFactory
import com.example.persistenciadual.ui.network.PostViewModel
import com.example.persistenciadual.ui.network.PostViewModelFactory
import com.example.persistenciadual.ui.security.SecretsViewModel
import com.example.persistenciadual.ui.security.SecretsViewModelFactory

class MainActivity : ComponentActivity() {

    private val taskViewModel:
            TaskViewModel by viewModels {
        val app =
            application as PersistenceDualApplication

        TaskViewModelFactory(
            repository =
                app.container.dualTaskRepository
        )
    }

    private val postViewModel:
            PostViewModel by viewModels {
        val app =
            application as PersistenceDualApplication

        PostViewModelFactory(
            repository =
                app.container.postRepository
        )
    }

    private val secretsViewModel:
            SecretsViewModel by viewModels {
        val app =
            application as PersistenceDualApplication

        SecretsViewModelFactory(
            repository =
                app.container.secretRepository
        )
    }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                AppRoot(
                    taskViewModel = taskViewModel,
                    postViewModel = postViewModel,
                    secretsViewModel =
                        secretsViewModel
                )
            }
        }
    }
}