package ec.edu.epn.logisend.navigation

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import ec.edu.epn.logisend.data.LogisticsContract

object PackageIntentSender {

    private const val TAG =
        "PackageIntentSender"

    fun sendPackage(
        context: Context,
        packageJson: String
    ): Boolean {

        /*
         * Intent explícito:
         * abre directamente MainActivity de LogiRoute.
         */
        val intent = Intent().apply {

            component = ComponentName(
                LogisticsContract.TARGET_PACKAGE,
                "${LogisticsContract.TARGET_PACKAGE}.MainActivity"
            )

            action =
                LogisticsContract.ACTION_RECEIVE_PACKAGE

            addCategory(
                Intent.CATEGORY_DEFAULT
            )

            putExtra(
                LogisticsContract.EXTRA_PACKAGE_JSON,
                packageJson
            )

            /*
             * NEW_TASK:
             * lleva LogiRoute a su propia tarea.
             *
             * CLEAR_TOP + SINGLE_TOP:
             * reutilizan la instancia existente.
             */
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        return try {

            Log.i(
                TAG,
                "Enviando paquete a LogiRoute: $packageJson"
            )

            context.startActivity(intent)

            true

        } catch (
            exception: ActivityNotFoundException
        ) {

            Log.e(
                TAG,
                "No se encontró LogiRoute.",
                exception
            )

            false

        } catch (
            exception: SecurityException
        ) {

            Log.e(
                TAG,
                "LogiRoute no permite recibir el Intent.",
                exception
            )

            false

        } catch (
            exception: Exception
        ) {

            Log.e(
                TAG,
                "No se pudo abrir LogiRoute.",
                exception
            )

            false
        }
    }
}