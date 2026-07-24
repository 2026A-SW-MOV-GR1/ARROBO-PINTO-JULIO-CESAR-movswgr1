package ec.edu.epn.logisend.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Calcula la distancia geográfica aproximada
 * entre dos coordenadas.
 */
object DistanceUtils {

    fun calculateDistanceKm(
        origin: LatLng,
        destination: LatLng
    ): Double {

        val result = FloatArray(1)

        Location.distanceBetween(
            origin.latitude,
            origin.longitude,
            destination.latitude,
            destination.longitude,
            result
        )

        val distanceMeters = result[0]

        return distanceMeters / 1000.0
    }
}