import 'dart:async';

import 'package:flutter/services.dart';

import '../utils/logistics_contract.dart';

class IntentService {
  IntentService._();

  static final IntentService instance = IntentService._();

  static const MethodChannel _channel = MethodChannel(
    LogisticsContract.channelName,
  );

  final StreamController<String> _packageController =
      StreamController<String>.broadcast();

  bool _initialized = false;

  Stream<String> get packageStream => _packageController.stream;

  Future<String?> initialize() async {
    if (!_initialized) {
      _channel.setMethodCallHandler(_handleNativeCall);

      _initialized = true;
    }

    try {
      return await _channel.invokeMethod<String>(
        LogisticsContract.getInitialPackageMethod,
      );
    } on PlatformException catch (error) {
      throw Exception(
        error.message ?? 'No se pudo consultar el Intent inicial.',
      );
    }
  }

  Future<void> _handleNativeCall(MethodCall call) async {
    if (call.method != LogisticsContract.onPackageReceivedMethod) {
      return;
    }

    final arguments = call.arguments;

    if (arguments is String && arguments.trim().isNotEmpty) {
      _packageController.add(arguments);
    }
  }
}
