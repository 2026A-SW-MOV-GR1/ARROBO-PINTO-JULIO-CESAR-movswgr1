package ec.edu.epn.logisend.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import ec.edu.epn.logisend.data.PackageJsonBuilder
import ec.edu.epn.logisend.location.LocationHelper
import ec.edu.epn.logisend.navigation.MapsNavigationHelper
import ec.edu.epn.logisend.navigation.PackageIntentSender
import ec.edu.epn.logisend.util.DistanceUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * Posición inicial aproximada de Quito.
 * Se usa únicamente mientras todavía no se obtiene
 * la ubicación real del dispositivo.
 */
private val DEFAULT_LOCATION =
    LatLng(
        -0.1807,
        -78.4678
    )

private const val SELECT_ORIGIN =
    "ORIGIN"

private const val SELECT_DESTINATION =
    "DESTINATION"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogiSendScreen() {

    val context =
        LocalContext.current

    val coroutineScope =
        rememberCoroutineScope()

    val snackbarHostState =
        remember {
            SnackbarHostState()
        }

    /*
     * Datos del formulario.
     */
    var packageId by rememberSaveable {
        mutableStateOf(
            generatePackageId()
        )
    }

    var senderName by rememberSaveable {
        mutableStateOf("")
    }

    var recipientName by rememberSaveable {
        mutableStateOf("")
    }

    var description by rememberSaveable {
        mutableStateOf("")
    }

    var weightText by rememberSaveable {
        mutableStateOf("")
    }

    var priority by rememberSaveable {
        mutableStateOf("MEDIA")
    }

    /*
     * Coordenadas seleccionadas.
     */
    var origin by remember {
        mutableStateOf<LatLng?>(null)
    }

    var destination by remember {
        mutableStateOf<LatLng?>(null)
    }

    var currentLocation by remember {
        mutableStateOf<LatLng?>(null)
    }

    /*
     * Define cuál marcador se modificará
     * cuando el usuario toque el mapa.
     */
    var selectionMode by rememberSaveable {
        mutableStateOf(
            SELECT_ORIGIN
        )
    }

    /*
     * Estado del permiso.
     */
    var hasLocationPermission by remember {
        mutableStateOf(
            LocationHelper.hasLocationPermission(
                context
            )
        )
    }

    /*
     * Se incrementa cada vez que el usuario
     * solicita actualizar su ubicación.
     */
    var locationRequestNumber by remember {
        mutableStateOf(0)
    }

    /*
     * Cámara inicial.
     */
    val cameraPositionState =
        rememberCameraPositionState {

            position =
                CameraPosition.fromLatLngZoom(
                    DEFAULT_LOCATION,
                    12f
                )
        }

    /*
     * Función interna para mostrar mensajes.
     */
    val showMessage: (String) -> Unit = {
            message ->

        coroutineScope.launch {

            snackbarHostState.showSnackbar(
                message
            )
        }
    }

    /*
     * Solicitud de permisos en tiempo de ejecución.
     */
    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract =
                ActivityResultContracts
                    .RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission
                        .ACCESS_COARSE_LOCATION
                ] == true

            hasLocationPermission =
                fineGranted || coarseGranted

            if (hasLocationPermission) {

                locationRequestNumber += 1

            } else {

                showMessage(
                    "Permiso de ubicación denegado. " +
                            "Puedes seleccionar las coordenadas " +
                            "manualmente tocando el mapa."
                )
            }
        }

    /*
     * Consulta la ubicación cuando existe permiso
     * y cambia locationRequestNumber.
     */
    LaunchedEffect(
        hasLocationPermission,
        locationRequestNumber
    ) {

        if (hasLocationPermission) {

            LocationHelper.getCurrentLocation(
                context = context,

                onSuccess = { point ->

                    currentLocation = point

                    /*
                     * Si todavía no existe origen,
                     * la ubicación actual se utiliza
                     * como punto de recolección.
                     */
                    if (origin == null) {
                        origin = point
                    }

                    coroutineScope.launch {

                        cameraPositionState.animate(
                            CameraUpdateFactory
                                .newLatLngZoom(
                                    point,
                                    16f
                                )
                        )
                    }

                    showMessage(
                        "Ubicación actual obtenida correctamente."
                    )
                },

                onError = { error ->

                    showMessage(
                        error
                    )
                }
            )
        }
    }

    /*
     * Distancia entre origen y destino.
     */
    val distanceKm =
        remember(
            origin,
            destination
        ) {

            if (
                origin != null &&
                destination != null
            ) {

                DistanceUtils
                    .calculateDistanceKm(
                        origin!!,
                        destination!!
                    )

            } else {

                null
            }
        }

    Scaffold(

        topBar = {

            CenterAlignedTopAppBar(

                title = {

                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "LogiSend",
                            fontWeight =
                                FontWeight.Bold
                        )

                        Text(
                            text =
                                "Registro y despacho de paquetes",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelMedium
                        )
                    }
                }
            )
        },

        snackbarHost = {

            SnackbarHost(
                hostState =
                    snackbarHostState
            )
        }

    ) { innerPadding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),

            contentPadding =
                PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 32.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    12.dp
                )

        ) {

            /*
             * SECCIÓN 1: DATOS DEL PAQUETE
             */
            item {

                SectionTitle(
                    title = "1. Datos del paquete"
                )
            }

            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceContainer
                        )
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                16.dp
                            ),

                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
                    ) {

                        OutlinedTextField(
                            value = packageId,
                            onValueChange = {},
                            modifier =
                                Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Código del paquete"
                                )
                            },
                            readOnly = true,
                            singleLine = true
                        )

                        TextButton(
                            onClick = {

                                packageId =
                                    generatePackageId()
                            }
                        ) {

                            Text(
                                "Generar un nuevo código"
                            )
                        }

                        OutlinedTextField(
                            value = senderName,
                            onValueChange = {
                                senderName = it
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Nombre del remitente"
                                )
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = {
                                recipientName = it
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Nombre del destinatario"
                                )
                            },
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = {
                                description = it
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Descripción del contenido"
                                )
                            },
                            minLines = 2,
                            maxLines = 4
                        )

                        OutlinedTextField(
                            value = weightText,
                            onValueChange = {
                                    newValue ->

                                /*
                                 * Acepta números, punto o coma.
                                 */
                                if (
                                    newValue.all {
                                            character ->

                                        character.isDigit() ||
                                                character == '.' ||
                                                character == ','
                                    }
                                ) {

                                    weightText =
                                        newValue
                                }
                            },
                            modifier =
                                Modifier.fillMaxWidth(),
                            label = {
                                Text(
                                    "Peso en kilogramos"
                                )
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        KeyboardType.Decimal
                                ),
                            singleLine = true
                        )

                        Text(
                            text =
                                "Prioridad del envío",
                            style =
                                MaterialTheme
                                    .typography
                                    .labelLarge,
                            fontWeight =
                                FontWeight.SemiBold
                        )

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),

                            horizontalArrangement =
                                Arrangement.spacedBy(
                                    8.dp
                                )
                        ) {

                            PriorityChip(
                                text = "BAJA",
                                selected =
                                    priority == "BAJA",
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    priority = "BAJA"
                                }
                            )

                            PriorityChip(
                                text = "MEDIA",
                                selected =
                                    priority == "MEDIA",
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    priority = "MEDIA"
                                }
                            )

                            PriorityChip(
                                text = "ALTA",
                                selected =
                                    priority == "ALTA",
                                modifier =
                                    Modifier.weight(1f),
                                onClick = {
                                    priority = "ALTA"
                                }
                            )
                        }
                    }
                }
            }

            /*
             * SECCIÓN 2: UBICACIÓN
             */
            item {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                SectionTitle(
                    title = "2. Ubicación del envío"
                )
            }

            item {

                Button(
                    onClick = {

                        if (
                            LocationHelper
                                .hasLocationPermission(
                                    context
                                )
                        ) {

                            hasLocationPermission =
                                true

                            locationRequestNumber += 1

                        } else {

                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission
                                        .ACCESS_FINE_LOCATION,

                                    Manifest.permission
                                        .ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Usar mi ubicación como origen"
                    )
                }
            }

            item {

                Text(
                    text =
                        "Selecciona el punto que deseas " +
                                "modificar y después toca el mapa.",

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

            item {

                Row(
                    modifier =
                        Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {

                    FilterChip(
                        selected =
                            selectionMode ==
                                    SELECT_ORIGIN,

                        onClick = {
                            selectionMode =
                                SELECT_ORIGIN
                        },

                        label = {
                            Text(
                                "Seleccionar origen"
                            )
                        },

                        modifier =
                            Modifier.weight(1f)
                    )

                    FilterChip(
                        selected =
                            selectionMode ==
                                    SELECT_DESTINATION,

                        onClick = {
                            selectionMode =
                                SELECT_DESTINATION
                        },

                        label = {
                            Text(
                                "Seleccionar destino"
                            )
                        },

                        modifier =
                            Modifier.weight(1f)
                    )
                }
            }

            /*
             * MAPA INTERACTIVO
             */
            item {

                Card(
                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(390.dp)
                    ) {

                        GoogleMap(

                            modifier =
                                Modifier.fillMaxSize(),

                            cameraPositionState =
                                cameraPositionState,

                            properties =
                                MapProperties(
                                    isMyLocationEnabled =
                                        hasLocationPermission
                                ),

                            uiSettings =
                                MapUiSettings(
                                    zoomControlsEnabled =
                                        true,

                                    compassEnabled =
                                        true,

                                    myLocationButtonEnabled =
                                        false,

                                    mapToolbarEnabled =
                                        true
                                ),

                            onMapClick = {
                                    selectedPoint ->

                                when (
                                    selectionMode
                                ) {

                                    SELECT_ORIGIN -> {

                                        origin =
                                            selectedPoint

                                        showMessage(
                                            "Punto de origen actualizado."
                                        )
                                    }

                                    SELECT_DESTINATION -> {

                                        destination =
                                            selectedPoint

                                        showMessage(
                                            "Punto de destino actualizado."
                                        )
                                    }
                                }
                            }

                        ) {

                            /*
                             * Marcador verde: origen.
                             */
                            origin?.let {
                                    point ->

                                Marker(
                                    state =
                                        rememberUpdatedMarkerState(
                                            position =
                                                point
                                        ),

                                    title =
                                        "Punto de recolección",

                                    snippet =
                                        point
                                            .toCoordinateText(),

                                    icon =
                                        BitmapDescriptorFactory
                                            .defaultMarker(
                                                BitmapDescriptorFactory
                                                    .HUE_GREEN
                                            )
                                )
                            }

                            /*
                             * Marcador rojo: destino.
                             */
                            destination?.let {
                                    point ->

                                Marker(
                                    state =
                                        rememberUpdatedMarkerState(
                                            position =
                                                point
                                        ),

                                    title =
                                        "Punto de entrega",

                                    snippet =
                                        point
                                            .toCoordinateText(),

                                    icon =
                                        BitmapDescriptorFactory
                                            .defaultMarker(
                                                BitmapDescriptorFactory
                                                    .HUE_RED
                                            )
                                )
                            }

                            /*
                             * Marcador azul: ubicación actual.
                             */
                            currentLocation?.let {
                                    point ->

                                Marker(
                                    state =
                                        rememberUpdatedMarkerState(
                                            position =
                                                point
                                        ),

                                    title =
                                        "Mi ubicación actual",

                                    snippet =
                                        point
                                            .toCoordinateText(),

                                    icon =
                                        BitmapDescriptorFactory
                                            .defaultMarker(
                                                BitmapDescriptorFactory
                                                    .HUE_AZURE
                                            )
                                )
                            }

                            /*
                             * Línea entre origen y destino.
                             */
                            if (
                                origin != null &&
                                destination != null
                            ) {

                                Polyline(
                                    points =
                                        listOf(
                                            origin!!,
                                            destination!!
                                        ),

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .primary,

                                    width = 10f,

                                    geodesic = true
                                )
                            }
                        }
                    }
                }
            }

            /*
             * COORDENADAS
             */
            item {

                CoordinatesCard(
                    origin = origin,
                    destination =
                        destination,
                    distanceKm =
                        distanceKm
                )
            }

            /*
             * BOTÓN PARA RUTA EXTERNA
             */
            item {

                OutlinedButton(

                    enabled =
                        origin != null &&
                                destination != null,

                    onClick = {

                        if (
                            origin == null ||
                            destination == null
                        ) {

                            showMessage(
                                "Selecciona el origen y el destino."
                            )

                            return@OutlinedButton
                        }

                        val opened =
                            MapsNavigationHelper
                                .openRoute(
                                    context =
                                        context,

                                    origin =
                                        origin!!,

                                    destination =
                                        destination!!
                                )

                        if (!opened) {

                            showMessage(
                                "No existe una aplicación disponible " +
                                        "para mostrar la ruta."
                            )
                        }
                    },

                    modifier =
                        Modifier.fillMaxWidth()
                ) {

                    Text(
                        "Abrir ruta de navegación"
                    )
                }
            }

            /*
             * SECCIÓN 3: ENVÍO A LA APP 2
             */
            item {

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                HorizontalDivider()

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                SectionTitle(
                    title = "3. Enviar a LogiRoute"
                )
            }

            item {

                Button(

                    modifier =
                        Modifier.fillMaxWidth(),

                    onClick = {

                        /*
                         * Validación de campos.
                         */
                        if (
                            senderName.isBlank()
                        ) {

                            showMessage(
                                "Ingresa el nombre del remitente."
                            )

                            return@Button
                        }

                        if (
                            recipientName.isBlank()
                        ) {

                            showMessage(
                                "Ingresa el nombre del destinatario."
                            )

                            return@Button
                        }

                        if (
                            description.isBlank()
                        ) {

                            showMessage(
                                "Ingresa una descripción del paquete."
                            )

                            return@Button
                        }

                        val normalizedWeight =
                            weightText
                                .replace(
                                    ",",
                                    "."
                                )

                        val weight =
                            normalizedWeight
                                .toDoubleOrNull()

                        if (
                            weight == null ||
                            weight <= 0
                        ) {

                            showMessage(
                                "Ingresa un peso válido mayor que cero."
                            )

                            return@Button
                        }

                        if (
                            origin == null
                        ) {

                            showMessage(
                                "Selecciona el punto de origen."
                            )

                            return@Button
                        }

                        if (
                            destination == null
                        ) {

                            showMessage(
                                "Selecciona el punto de destino."
                            )

                            return@Button
                        }

                        if (
                            origin == destination
                        ) {

                            showMessage(
                                "El origen y el destino " +
                                        "no pueden ser iguales."
                            )

                            return@Button
                        }

                        /*
                         * Construcción del contrato JSON.
                         */
                        val packageJson =
                            PackageJsonBuilder.build(

                                packageId =
                                    packageId,

                                senderName =
                                    senderName,

                                recipientName =
                                    recipientName,

                                description =
                                    description,

                                weightKg =
                                    weight,

                                priority =
                                    priority,

                                origin =
                                    origin!!,

                                destination =
                                    destination!!
                            )

                        /*
                         * Envío hacia LogiRoute.
                         */
                        val sent =
                            PackageIntentSender
                                .sendPackage(
                                    context =
                                        context,

                                    packageJson =
                                        packageJson
                                )

                        if (!sent) {

                            showMessage(
                                "No se encontró LogiRoute. " +
                                        "Instala primero la Aplicación 2 " +
                                        "en este mismo dispositivo."
                            )
                        }
                    }
                ) {

                    Text(
                        "Enviar paquete a LogiRoute"
                    )
                }
            }
        }
    }
}

/**
 * Encabezado reutilizable de cada sección.
 */
@Composable
private fun SectionTitle(
    title: String
) {

    Text(
        text = title,
        style =
            MaterialTheme
                .typography
                .titleLarge,
        fontWeight =
            FontWeight.Bold
    )
}

/**
 * Botones para la selección de prioridad.
 */
@Composable
private fun PriorityChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {

            Text(
                text = text
            )
        },
        modifier = modifier
    )
}

/**
 * Tarjeta que muestra origen, destino y distancia.
 */
@Composable
private fun CoordinatesCard(
    origin: LatLng?,
    destination: LatLng?,
    distanceKm: Double?
) {

    Card(
        modifier =
            Modifier.fillMaxWidth(),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme
                        .colorScheme
                        .surfaceContainer
            )
    ) {

        Column(
            modifier =
                Modifier.padding(
                    16.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {

            Text(
                text =
                    "Resumen geográfico",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.Bold
            )

            Text(
                text =
                    "Origen: ${
                        origin?.toCoordinateText()
                            ?: "No seleccionado"
                    }"
            )

            Text(
                text =
                    "Destino: ${
                        destination?.toCoordinateText()
                            ?: "No seleccionado"
                    }"
            )

            Text(
                text =
                    if (distanceKm != null) {

                        "Distancia aproximada: ${
                            String.format(
                                Locale.getDefault(),
                                "%.2f km",
                                distanceKm
                            )
                        }"

                    } else {

                        "Distancia aproximada: pendiente"
                    }
            )
        }
    }
}

/**
 * Convierte una coordenada a un formato legible.
 */
private fun LatLng.toCoordinateText(): String {

    return String.format(
        Locale.US,
        "%.6f, %.6f",
        latitude,
        longitude
    )
}

/**
 * Genera un código único basado en fecha y hora.
 */
private fun generatePackageId(): String {

    val formatter =
        SimpleDateFormat(
            "yyyyMMdd-HHmmss",
            Locale.getDefault()
        )

    return "PKG-${
        formatter.format(
            Date()
        )
    }"
}