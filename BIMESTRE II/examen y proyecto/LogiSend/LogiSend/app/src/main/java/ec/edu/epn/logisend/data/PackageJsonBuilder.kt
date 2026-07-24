package ec.edu.epn.logisend.data

import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

/**
 * Construye el JSON que será enviado hacia LogiRoute.
 */
object PackageJsonBuilder {

    fun build(
        packageId: String,
        senderName: String,
        recipientName: String,
        description: String,
        weightKg: Double,
        priority: String,
        origin: LatLng,
        destination: LatLng
    ): String {

        val originJson = JSONObject().apply {
            put("latitude", origin.latitude)
            put("longitude", origin.longitude)
        }

        val destinationJson = JSONObject().apply {
            put("latitude", destination.latitude)
            put("longitude", destination.longitude)
        }

        val packageJson = JSONObject().apply {

            put(
                "contractVersion",
                LogisticsContract.CONTRACT_VERSION
            )

            put(
                "packageId",
                packageId
            )

            put(
                "senderName",
                senderName.trim()
            )

            put(
                "recipientName",
                recipientName.trim()
            )

            put(
                "description",
                description.trim()
            )

            put(
                "weightKg",
                weightKg
            )

            put(
                "priority",
                priority
            )

            put(
                "status",
                "REGISTRADO"
            )

            put(
                "origin",
                originJson
            )

            put(
                "destination",
                destinationJson
            )

            put(
                "createdAtEpochMs",
                System.currentTimeMillis()
            )
        }

        return packageJson.toString()
    }
}