package ec.edu.epn.logiroute

import android.content.Intent
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    companion object {

        /*
         * Nombre del canal utilizado para comunicar
         * el código Kotlin nativo con Flutter.
         */
        private const val CHANNEL_NAME =
            "ec.edu.epn.logistics/intent"

        /*
         * Acción que LogiSend utiliza para abrir LogiRoute.
         * Debe ser exactamente igual en las dos aplicaciones.
         */
        private const val ACTION_RECEIVE_PACKAGE =
            "ec.edu.epn.logistics.RECEIVE_PACKAGE"

        /*
         * Nombre del extra que contiene el JSON.
         */
        private const val EXTRA_PACKAGE_JSON =
            "extra_package_json"

        /*
         * Flutter invoca este método para solicitar
         * el paquete con el que se abrió LogiRoute.
         */
        private const val METHOD_GET_INITIAL_PACKAGE =
            "getInitialPackage"

        /*
         * Kotlin invoca este método cuando LogiRoute ya estaba
         * abierta y recibe un paquete nuevo.
         */
        private const val METHOD_ON_PACKAGE_RECEIVED =
            "onPackageReceived"

        /*
         * SharedPreferences permite conservar el último
         * paquete recibido aunque se cierre la aplicación.
         */
        private const val PREFERENCES_NAME =
            "logiroute_preferences"

        private const val LAST_PACKAGE_KEY =
            "last_package_json"
    }

    /*
     * Canal de comunicación Kotlin → Flutter.
     */
    private var methodChannel: MethodChannel? = null

    /*
     * Guarda temporalmente el JSON mientras Flutter termina
     * de inicializarse.
     */
    private var pendingPackageJson: String? = null

    override fun configureFlutterEngine(
        flutterEngine: FlutterEngine
    ) {
        super.configureFlutterEngine(flutterEngine)

        /*
         * Comprueba si LogiRoute fue abierta directamente
         * mediante el Intent enviado por LogiSend.
         */
        pendingPackageJson =
            extractPackageJson(intent)

        /*
         * Conserva el último paquete recibido.
         */
        pendingPackageJson?.let { packageJson ->
            saveLastPackage(packageJson)
        }

        /*
         * Crea el MethodChannel que conecta Kotlin con Dart.
         */
        methodChannel = MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL_NAME
        )

        /*
         * Atiende las solicitudes enviadas desde Flutter.
         */
        methodChannel?.setMethodCallHandler { call, result ->

            when (call.method) {

                METHOD_GET_INITIAL_PACKAGE -> {

                    /*
                     * Busca el paquete en este orden:
                     *
                     * 1. Paquete pendiente.
                     * 2. Intent actual.
                     * 3. Último paquete guardado.
                     */
                    val packageJson =
                        pendingPackageJson
                            ?: extractPackageJson(intent)
                            ?: readLastPackage()

                    /*
                     * Una vez entregado a Flutter,
                     * limpia la variable temporal.
                     */
                    pendingPackageJson = null

                    result.success(packageJson)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    /*
     * Se ejecuta cuando LogiRoute ya está abierta
     * y LogiSend envía otro paquete.
     *
     * Esto funciona junto con:
     * android:launchMode="singleTask"
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        /*
         * Actualiza el Intent actual de la actividad.
         */
        setIntent(intent)

        /*
         * Extrae el JSON.
         * Si el Intent no vino desde LogiSend, termina.
         */
        val packageJson =
            extractPackageJson(intent)
                ?: return

        /*
         * Conserva temporalmente el paquete.
         */
        pendingPackageJson = packageJson

        /*
         * Guarda el paquete para recuperarlo cuando
         * se abra LogiRoute desde su ícono.
         */
        saveLastPackage(packageJson)

        /*
         * Envía inmediatamente el JSON a Flutter cuando
         * el motor y el canal ya están activos.
         */
        methodChannel?.invokeMethod(
            METHOD_ON_PACKAGE_RECEIVED,
            packageJson
        )
    }

    /*
     * Valida que el Intent corresponda al contrato
     * utilizado por LogiSend y obtiene el JSON.
     */
    private fun extractPackageJson(
        sourceIntent: Intent?
    ): String? {

        if (
            sourceIntent?.action !=
            ACTION_RECEIVE_PACKAGE
        ) {
            return null
        }

        return sourceIntent
            .getStringExtra(EXTRA_PACKAGE_JSON)
            ?.takeIf { packageJson ->
                packageJson.isNotBlank()
            }
    }

    /*
     * Guarda el último JSON recibido en el almacenamiento
     * privado de LogiRoute.
     */
    private fun saveLastPackage(
        packageJson: String
    ) {
        getSharedPreferences(
            PREFERENCES_NAME,
            MODE_PRIVATE
        )
            .edit()
            .putString(
                LAST_PACKAGE_KEY,
                packageJson
            )
            .apply()
    }

    /*
     * Recupera el último paquete recibido.
     */
    private fun readLastPackage(): String? {
        return getSharedPreferences(
            PREFERENCES_NAME,
            MODE_PRIVATE
        ).getString(
            LAST_PACKAGE_KEY,
            null
        )
    }
}