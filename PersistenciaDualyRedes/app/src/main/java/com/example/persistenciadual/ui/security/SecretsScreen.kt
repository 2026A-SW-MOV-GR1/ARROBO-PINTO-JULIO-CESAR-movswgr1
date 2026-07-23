package com.example.persistenciadual.ui.security

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.persistenciadual.data.security.SecretStorageType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretsScreen(
    viewModel: SecretsViewModel
) {
    val uiState by viewModel.uiState
        .collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Gestión de secretos")
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
                text = "Almacenamiento llave–valor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Ingrese una llave y un valor, " +
                        "seleccione el almacenamiento y pulse " +
                        "Guardar. Para recuperar, escriba la llave " +
                        "y seleccione el mecanismo correspondiente."
            )

            OutlinedTextField(
                value = uiState.key,
                onValueChange =
                    viewModel::onKeyChanged,
                label = {
                    Text("Llave")
                },
                placeholder = {
                    Text("Ejemplo: token_api")
                },
                singleLine = true,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.value,
                onValueChange =
                    viewModel::onValueChanged,
                label = {
                    Text("Valor")
                },
                placeholder = {
                    Text("Valor que desea almacenar")
                },
                minLines = 2,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Seleccione el mecanismo",
                fontWeight = FontWeight.Bold
            )

            SecretStorageType.values().forEach {
                    storageType ->

                StorageOption(
                    storageType = storageType,
                    selected =
                        uiState.selectedStorage ==
                                storageType,
                    enabled = !uiState.isBusy,
                    onSelected = {
                        viewModel.onStorageChanged(
                            storageType
                        )
                    }
                )
            }

            if (uiState.isBusy) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }

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
                    Text("Guardar")
                }

                OutlinedButton(
                    onClick = viewModel::recover,
                    enabled = !uiState.isBusy,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Recuperar")
                }
            }

            OutlinedButton(
                onClick = viewModel::clearValue,
                enabled = !uiState.isBusy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpiar valor")
            }

            uiState.message?.let { message ->
                Surface(
                    color = MaterialTheme
                        .colorScheme
                        .secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = message,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            uiState.recoveredValue?.let {
                    recoveredValue ->

                Surface(
                    color = MaterialTheme
                        .colorScheme
                        .primaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Valor recuperado",
                            fontWeight = FontWeight.Bold
                        )

                        Text(recoveredValue)
                    }
                }
            }

            Text(
                text = "La aplicación no enumera las llaves " +
                        "existentes. El usuario debe conocer " +
                        "previamente la llave.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StorageOption(
    storageType: SecretStorageType,
    selected: Boolean,
    enabled: Boolean,
    onSelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onSelected
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
                enabled = enabled
            )

            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = storageType.displayName,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = storageType.description,
                    style = MaterialTheme
                        .typography
                        .bodySmall
                )
            }
        }
    }
}