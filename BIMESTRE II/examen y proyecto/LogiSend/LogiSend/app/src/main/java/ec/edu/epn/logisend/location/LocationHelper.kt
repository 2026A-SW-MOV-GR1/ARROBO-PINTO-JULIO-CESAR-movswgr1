package ec.edu.epn.logisend.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

/**
 * Funciones relacionadas con permisos y ubicación.
 */
object LocationHelper {

    /**
     * Verifica si existe permiso fino o aproximado.
     */
    fun hasLocationPermission(
        context: Context
    ): Boolean {

        val finePermission =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission =
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return finePermission || coarsePermission
    }

    /**
     * Verifica que GPS o ubicación por red estén activos.
     */
    fun isLocationEnabled(
        context: Context
    ): Boolean {

        val locationManager =
            context.getSystemService(
                Context.LOCATION_SERVICE
            ) as LocationManager

        val gpsEnabled =
            locationManager.isProviderEnabled(
                LocationManager.GPS_PROVIDER
            )

        val networkEnabled =
            locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )

        return gpsEnabled || networkEnabled
    }

    /**
     * Obtiene una ubicación actual.
     *
     * Esta función debe llamarse únicamente después
     * de verificar o solicitar los permisos.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        context: Context,
        onSuccess: (LatLng) -> Unit,
        onError: (String) -> Unit
    ) {

        if (!hasLocationPermission(context)) {
            onError(
                "La aplicación no tiene permiso de ubicación."
            )
            return
        }

        if (!isLocationEnabled(context)) {
            onError(
                "La ubicación del dispositivo está desactivada. " +
                        "Activa el GPS e inténtalo nuevamente."
            )
            return
        }

        val fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(context)

        val cancellationTokenSource =
            CancellationTokenSource()

        try {

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->

                if (location != null) {

                    onSuccess(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )

                } else {

                    /*
                     * En algunos emuladores la ubicación actual
                     * puede retornar null. Como respaldo se
                     * consulta la última ubicación disponible.
                     */
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->

                            if (lastLocation != null) {

                                onSuccess(
                                    LatLng(
                                        lastLocation.latitude,
                                        lastLocation.longitude
                                    )
                                )

                            } else {

                                onError(
                                    "No se pudo obtener la ubicación. " +
                                            "Comprueba el GPS o configura " +
                                            "una ubicación en el emulador."
                                )
                            }
                        }
                        .addOnFailureListener { exception ->

                            onError(
                                exception.message
                                    ?: "No se pudo obtener la ubicación."
                            )
                        }
                }
            }.addOnFailureListener { exception ->

                onError(
                    exception.message
                        ?: "Error consultando la ubicación."
                )
            }

        } catch (exception: SecurityException) {

            onError(
                "No existe autorización para consultar la ubicación."
            )
        }
    }
}