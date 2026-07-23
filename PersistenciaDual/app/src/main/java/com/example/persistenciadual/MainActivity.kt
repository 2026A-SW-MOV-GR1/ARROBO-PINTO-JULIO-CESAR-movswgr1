package com.example.persistenciadual

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.persistenciadual.ui.theme.TaskScreen
import com.example.persistenciadual.ui.TaskViewModel
import com.example.persistenciadual.ui.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        val app = application as PersistenceDualApplication

        TaskViewModelFactory(
            repository = app.container.dualTaskRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TaskScreen(viewModel)
                }
            }
        }
    }
}