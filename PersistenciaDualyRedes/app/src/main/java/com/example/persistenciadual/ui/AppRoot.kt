package com.example.persistenciadual.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.persistenciadual.ui.network.PostScreen
import com.example.persistenciadual.ui.network.PostViewModel
import com.example.persistenciadual.ui.security.SecretsScreen
import com.example.persistenciadual.ui.security.SecretsViewModel

private enum class AppSection(
    val label: String,
    val iconText: String
) {
    PERSISTENCE(
        label = "Datos",
        iconText = "BD"
    ),

    NETWORK(
        label = "Red",
        iconText = "HTTP"
    ),

    SECURITY(
        label = "Secretos",
        iconText = "KEY"
    )
}

@Composable
fun AppRoot(
    taskViewModel: TaskViewModel,
    postViewModel: PostViewModel,
    secretsViewModel: SecretsViewModel
) {
    var selectedSection by rememberSaveable {
        mutableStateOf(AppSection.PERSISTENCE)
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppSection.values().forEach {
                        section ->

                    NavigationBarItem(
                        selected =
                            selectedSection == section,
                        onClick = {
                            selectedSection = section
                        },
                        icon = {
                            Text(section.iconText)
                        },
                        label = {
                            Text(section.label)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedSection) {
                AppSection.PERSISTENCE -> {
                    TaskScreen(
                        viewModel = taskViewModel
                    )
                }

                AppSection.NETWORK -> {
                    PostScreen(
                        viewModel = postViewModel
                    )
                }

                AppSection.SECURITY -> {
                    SecretsScreen(
                        viewModel = secretsViewModel
                    )
                }
            }
        }
    }
}