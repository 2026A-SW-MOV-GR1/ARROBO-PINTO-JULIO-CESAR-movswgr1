import 'package:geolocator/geolocator.dart';

class LocationServiceException implements Exception {
  final String message;

  const LocationServiceException(this.message);

  @override
  String toString() => message;
}

class LocationService {
  LocationService._();

  static Future<Position> determineCurrentPosition() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();

    if (!serviceEnabled) {
      throw const LocationServiceException(
        'La ubicación está desactivada. '
        'Activa el GPS e inténtalo nuevamente.',
      );
    }

    var permission = await Geolocator.checkPermission();

    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied) {
      throw const LocationServiceException(
        'El permiso de ubicación fue denegado.',
      );
    }

    if (permission == LocationPermission.deniedForever) {
      throw const LocationServiceException(
        'El permiso de ubicación está bloqueado '
        'permanentemente. Debes habilitarlo desde '
        'la configuración de la aplicación.',
      );
    }

    const locationSettings = LocationSettings(accuracy: LocationAccuracy.high);

    try {
      return await Geolocator.getCurrentPosition(
        locationSettings: locationSettings,
      );
    } catch (error) {
      throw LocationServiceException('No se pudo obtener la ubicación: $error');
    }
  }
}
