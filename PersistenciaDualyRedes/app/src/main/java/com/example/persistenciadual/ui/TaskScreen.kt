package com.example.persistenciadual.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.persistenciadual.domain.StorageEngine
import com.example.persistenciadual.domain.TaskItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(uiState.message) {
        val message = uiState.message

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text("Persistencia Dual")
                },
                actions = {
                    Text(
                        text = if (
                            uiState.activeEngine ==
                            StorageEngine.SQL
                        ) {
                            "SQL"
                        } else {
                            "NoSQL"
                        },
                        fontWeight = FontWeight.Bold
                    )

                    Switch(
                        checked =
                            uiState.activeEngine ==
                                    StorageEngine.NOSQL,
                        onCheckedChange =
                            viewModel::onEngineChanged,
                        enabled = !uiState.isBusy,
                        modifier = Modifier.padding(
                            horizontal = 12.dp
                        )
                    )
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            OriginIndicator(
                engine = uiState.activeEngine,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Text(
                text = if (uiState.isEditing) {
                    "Editar registro"
                } else {
                    "Crear registro"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange =
                    viewModel::onTitleChanged,
                label = {
                    Text("Título")
                },
                singleLine = true,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedTextField(
                value = uiState.description,
                onValueChange =
                    viewModel::onDescriptionChanged,
                label = {
                    Text("Descripción")
                },
                minLines = 2,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (
                            uiState.isEditing
                        ) {
                            "Actualizar"
                        } else {
                            "Guardar"
                        }
                    )
                }

                if (uiState.isEditing) {
                    OutlinedButton(
                        onClick =
                            viewModel::cancelEditing,
                        enabled = !uiState.isBusy,
                        modifier =
                            Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            HorizontalDivider()

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Registros almacenados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            if (uiState.items.isEmpty()) {
                Text(
                    text = if (
                        uiState.activeEngine ==
                        StorageEngine.SQL
                    ) {
                        "No existen registros en SQLite."
                    } else {
                        "No existen documentos en NoSQL."
                    },
                    modifier = Modifier.padding(
                        vertical = 24.dp
                    )
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.items,
                        key = { item ->
                            "${uiState.activeEngine}-${item.id}"
                        }
                    ) { item ->
                        TaskCard(
                            item = item,
                            enabled = !uiState.isBusy,
                            onToggle = {
                                viewModel
                                    .toggleCompleted(item)
                            },
                            onEdit = {
                                viewModel.edit(item)
                            },
                            onDelete = {
                                viewModel.delete(item)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OriginIndicator(
    engine: StorageEngine,
    modifier: Modifier = Modifier
) {
    val text = when (engine) {
        StorageEngine.SQL -> {
            "Origen activo: SQLite — almacenamiento relacional"
        }

        StorageEngine.NOSQL -> {
            "Origen activo: JSON — almacenamiento NoSQL"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color =
            MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun TaskCard(
    item: TaskItem,
    enabled: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = item.completed,
                    onCheckedChange = {
                        onToggle()
                    },
                    enabled = enabled
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = item.title,
                        style =
                            MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration =
                            if (item.completed) {
                                TextDecoration.LineThrough
                            } else {
                                TextDecoration.None
                            }
                    )

                    if (
                        item.description.isNotBlank()
                    ) {
                        Text(
                            text = item.description,
                            style =
                                MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "ID local: ${item.id}",
                        style =
                            MaterialTheme.typography.labelSmall
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = enabled
                ) {
                    Text("Editar")
                }

                OutlinedButton(
                    onClick = onDelete,
                    enabled = enabled,
                    modifier = Modifier.padding(
                        start = 8.dp
                    )
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}