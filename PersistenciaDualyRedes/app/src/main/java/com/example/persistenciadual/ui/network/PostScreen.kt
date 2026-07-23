package com.example.persistenciadual.ui.network

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    viewModel: PostViewModel
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Conectividad REST")
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Consulta y actualización de posts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ingrese un ID, obtenga el post, " +
                        "modifique el título o contenido y " +
                        "envíe la actualización al servidor."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.idInput,
                    onValueChange =
                        viewModel::onIdChanged,
                    label = {
                        Text("ID del post")
                    },
                    placeholder = {
                        Text("Ejemplo: 1")
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = viewModel::getPost,
                    enabled = !uiState.isLoading,
                    modifier = Modifier
                        .padding(top = 8.dp)
                ) {
                    Text("Obtener")
                }
            }

            if (uiState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.statusMessage?.let { message ->
                Surface(
                    color = MaterialTheme
                        .colorScheme
                        .secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = message,
                            fontWeight = FontWeight.Bold
                        )

                        uiState.statusCode?.let { code ->
                            Text("Código HTTP: $code")
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange =
                    viewModel::onTitleChanged,
                label = {
                    Text("Título")
                },
                enabled = !uiState.isLoading &&
                        uiState.loadedPostId != null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.body,
                onValueChange =
                    viewModel::onBodyChanged,
                label = {
                    Text("Contenido")
                },
                minLines = 5,
                enabled = !uiState.isLoading &&
                        uiState.loadedPostId != null,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = viewModel::updatePost,
                enabled = uiState.canUpdate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar mediante PUT")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Nota: JSONPlaceholder responde a la " +
                        "actualización, pero no conserva el cambio " +
                        "permanentemente.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}