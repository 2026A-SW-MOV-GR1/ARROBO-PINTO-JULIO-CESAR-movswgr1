import 'dart:convert';

import '../utils/logistics_contract.dart';

class PackageLocation {
  final double latitude;
  final double longitude;

  const PackageLocation({required this.latitude, required this.longitude});

  factory PackageLocation.fromJson(Map<String, dynamic> json) {
    return PackageLocation(
      latitude: _toDouble(json['latitude'], 'latitude'),
      longitude: _toDouble(json['longitude'], 'longitude'),
    );
  }

  static double _toDouble(Object? value, String fieldName) {
    if (value is num) {
      return value.toDouble();
    }

    final parsedValue = double.tryParse(value?.toString() ?? '');

    if (parsedValue == null) {
      throw FormatException('La coordenada $fieldName no es válida.');
    }

    return parsedValue;
  }
}

class PackageData {
  final String contractVersion;
  final String packageId;
  final String senderName;
  final String recipientName;
  final String description;
  final double weightKg;
  final String priority;
  final String status;
  final PackageLocation origin;
  final PackageLocation destination;
  final int createdAtEpochMs;

  const PackageData({
    required this.contractVersion,
    required this.packageId,
    required this.senderName,
    required this.recipientName,
    required this.description,
    required this.weightKg,
    required this.priority,
    required this.status,
    required this.origin,
    required this.destination,
    required this.createdAtEpochMs,
  });

  factory PackageData.fromJsonString(String jsonText) {
    final decodedValue = jsonDecode(jsonText);

    if (decodedValue is! Map) {
      throw const FormatException(
        'El contenido recibido no es un objeto JSON.',
      );
    }

    return PackageData.fromJson(Map<String, dynamic>.from(decodedValue));
  }

  factory PackageData.fromJson(Map<String, dynamic> json) {
    final contractVersion = _requiredString(json, 'contractVersion');

    if (contractVersion != LogisticsContract.contractVersion) {
      throw FormatException(
        'Versión de contrato no compatible: '
        '$contractVersion',
      );
    }

    return PackageData(
      contractVersion: contractVersion,
      packageId: _requiredString(json, 'packageId'),
      senderName: _requiredString(json, 'senderName'),
      recipientName: _requiredString(json, 'recipientName'),
      description: _requiredString(json, 'description'),
      weightKg: _requiredDouble(json, 'weightKg'),
      priority: _requiredString(json, 'priority'),
      status: _stringOrDefault(json, 'status', 'REGISTRADO'),
      origin: PackageLocation.fromJson(_requiredMap(json, 'origin')),
      destination: PackageLocation.fromJson(_requiredMap(json, 'destination')),
      createdAtEpochMs: _requiredInt(json, 'createdAtEpochMs'),
    );
  }

  static String _requiredString(Map<String, dynamic> json, String fieldName) {
    final value = json[fieldName]?.toString().trim() ?? '';

    if (value.isEmpty) {
      throw FormatException('El campo $fieldName es obligatorio.');
    }

    return value;
  }

  static String _stringOrDefault(
    Map<String, dynamic> json,
    String fieldName,
    String defaultValue,
  ) {
    final value = json[fieldName]?.toString().trim() ?? '';

    return value.isEmpty ? defaultValue : value;
  }

  static double _requiredDouble(Map<String, dynamic> json, String fieldName) {
    final value = json[fieldName];

    if (value is num) {
      return value.toDouble();
    }

    final parsedValue = double.tryParse(value?.toString() ?? '');

    if (parsedValue == null) {
      throw FormatException('El campo $fieldName no es numérico.');
    }

    return parsedValue;
  }

  static int _requiredInt(Map<String, dynamic> json, String fieldName) {
    final value = json[fieldName];

    if (value is int) {
      return value;
    }

    if (value is num) {
      return value.toInt();
    }

    final parsedValue = int.tryParse(value?.toString() ?? '');

    if (parsedValue == null) {
      throw FormatException('El campo $fieldName no es válido.');
    }

    return parsedValue;
  }

  static Map<String, dynamic> _requiredMap(
    Map<String, dynamic> json,
    String fieldName,
  ) {
    final value = json[fieldName];

    if (value is! Map) {
      throw FormatException('El campo $fieldName no contiene coordenadas.');
    }

    return Map<String, dynamic>.from(value);
  }
}
