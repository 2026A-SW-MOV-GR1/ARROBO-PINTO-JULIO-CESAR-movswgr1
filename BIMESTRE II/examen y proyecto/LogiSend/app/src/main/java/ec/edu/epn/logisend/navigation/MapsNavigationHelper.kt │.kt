package ec.edu.epn.logisend.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

/**
 * Abre una ruta vial externa desde el origen
 * hasta el destino.
 */
object MapsNavigationHelper {

    fun openRoute(
        context: Context,
        origin: LatLng,
        destination: LatLng
    ): Boolean {

        val originValue = String.format(
            Locale.US,
            "%.6f,%.6f",
            origin.latitude,
            origin.longitude
        )

        val destinationValue = String.format(
            Locale.US,
            "%.6f,%.6f",
            destination.latitude,
            destination.longitude
        )

        val routeUri = Uri.parse(
            "https://www.google.com/maps/dir/?" +
                    "api=1" +
                    "&origin=$originValue" +
                    "&destination=$destinationValue" +
                    "&travelmode=driving"
        )

        val googleMapsIntent =
            Intent(
                Intent.ACTION_VIEW,
                routeUri
            ).apply {

                setPackage(
                    "com.google.android.apps.maps"
                )

                if (context !is Activity) {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                    )
                }
            }

        return try {

            context.startActivity(
                googleMapsIntent
            )

            true

        } catch (
            googleMapsNotInstalled:
            ActivityNotFoundException
        ) {

            /*
             * Si Google Maps no está instalado,
             * se intenta abrir la ruta en el navegador.
             */
            try {

                val browserIntent =
                    Intent(
                        Intent.ACTION_VIEW,
                        routeUri
                    ).apply {

                        if (context !is Activity) {
                            addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            )
                        }
                    }

                context.startActivity(
                    browserIntent
                )

                true

            } catch (
                browserNotFound:
                ActivityNotFoundException
            ) {

                false
            }
        }
    }
}