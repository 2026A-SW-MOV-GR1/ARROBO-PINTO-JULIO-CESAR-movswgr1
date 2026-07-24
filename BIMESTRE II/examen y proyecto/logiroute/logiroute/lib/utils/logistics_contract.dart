class LogisticsContract {
  LogisticsContract._();

  // Canal entre Kotlin nativo y Flutter.
  static const String channelName = 'ec.edu.epn.logistics/intent';

  // Acción utilizada por LogiSend.
  static const String actionReceivePackage =
      'ec.edu.epn.logistics.RECEIVE_PACKAGE';

  // Extra que contiene el JSON.
  static const String extraPackageJson = 'extra_package_json';

  // Aplicación receptora.
  static const String targetPackage = 'ec.edu.epn.logiroute';

  // Versión del contrato JSON.
  static const String contractVersion = '1.0';

  // Métodos del MethodChannel.
  static const String getInitialPackageMethod = 'getInitialPackage';

  static const String onPackageReceivedMethod = 'onPackageReceived';
}
