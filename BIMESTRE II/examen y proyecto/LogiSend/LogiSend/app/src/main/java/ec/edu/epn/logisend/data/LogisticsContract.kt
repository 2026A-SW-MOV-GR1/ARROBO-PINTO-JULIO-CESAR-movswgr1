package ec.edu.epn.logisend.data

/**
 * Contrato compartido entre LogiSend y LogiRoute.
 *
 * Los valores de este archivo deben ser exactamente
 * iguales en la Aplicación 2.
 */
object LogisticsContract {

    /**
     * Acción personalizada que identifica el Intent.
     */
    const val ACTION_RECEIVE_PACKAGE =
        "ec.edu.epn.logistics.RECEIVE_PACKAGE"

    /**
     * Nombre del paquete Android de la Aplicación 2.
     */
    const val TARGET_PACKAGE =
        "ec.edu.epn.logiroute"

    /**
     * Nombre del parámetro que transportará el JSON.
     */
    const val EXTRA_PACKAGE_JSON =
        "extra_package_json"

    /**
     * Versión de la estructura JSON.
     */
    const val CONTRACT_VERSION =
        "1.0"
}