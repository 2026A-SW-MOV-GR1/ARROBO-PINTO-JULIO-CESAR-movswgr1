import 'dart:async';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';

import '../models/package_data.dart';
import '../services/intent_service.dart';
import '../services/location_service.dart';
import '../widgets/delivery_status_card.dart';
import '../widgets/package_information_card.dart';

class DeliveryScreen extends StatefulWidget {
  const DeliveryScreen({super.key});

  @override
  State<DeliveryScreen> createState() => _DeliveryScreenState();
}

class _DeliveryScreenState extends State<DeliveryScreen> {
  PackageData? _packageData;

  DeliveryStatus _deliveryStatus = DeliveryStatus.waiting;

  Position? _currentPosition;

  String? _errorMessage;

  bool _loadingLocation = false;

  GoogleMapController? _mapController;

  StreamSubscription<String>? _packageSubscription;

  @override
  void initState() {
    super.initState();

    /*
     * Escucha los paquetes nuevos cuando LogiRoute
     * ya se encuentra abierta.
     */
    _packageSubscription =
        IntentService.instance.packageStream.listen(
          _processPackageJson,
        );

    /*
     * Comprueba si LogiRoute fue abierta inicialmente
     * mediante un Intent enviado por LogiSend.
     */
    _initializeIntentCommunication();
  }

  Future<void> _initializeIntentCommunication() async {
    try {
      final initialPackageJson =
      await IntentService.instance.initialize();

      if (!mounted) {
        return;
      }

      if (initialPackageJson != null &&
          initialPackageJson.trim().isNotEmpty) {
        _processPackageJson(initialPackageJson);
      }
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _errorMessage =
        'No se pudo iniciar la comunicación: $error';
      });
    }
  }

  void _processPackageJson(String jsonText) {
    try {
      final receivedPackage =
      PackageData.fromJsonString(jsonText);

      if (!mounted) {
        return;
      }

      setState(() {
        _packageData = receivedPackage;
        _deliveryStatus = DeliveryStatus.received;
        _errorMessage = null;
      });

      /*
       * Si el mapa ya estaba creado porque se recibió
       * un segundo paquete, reajusta la cámara.
       */
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (!mounted) {
          return;
        }

        _fitPackageOnMap(
          receivedPackage,
          includeCourier: true,
        );
      });

      _showMessage(
        'Paquete ${receivedPackage.packageId} '
            'recibido correctamente.',
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      setState(() {
        _errorMessage =
        'Los datos recibidos no son válidos: $error';
      });

      _showMessage(
        'No se pudo interpretar el paquete recibido.',
      );
    }
  }

  Future<void> _obtainCurrentLocation() async {
    if (_loadingLocation) {
      return;
    }

    setState(() {
      _loadingLocation = true;
    });

    try {
      final position =
      await LocationService.determineCurrentPosition();

      if (!mounted) {
        return;
      }

      setState(() {
        _currentPosition = position;
      });

      /*
       * Después de obtener la ubicación del transportista,
       * muestra origen, destino y transportista en el mapa.
       */
      final packageData = _packageData;

      if (packageData != null) {
        await _fitPackageOnMap(
          packageData,
          includeCourier: true,
        );
      }

      _showMessage(
        'Ubicación del transportista obtenida correctamente.',
      );
    } catch (error) {
      if (!mounted) {
        return;
      }

      _showMessage(error.toString());
    } finally {
      if (mounted) {
        setState(() {
          _loadingLocation = false;
        });
      }
    }
  }

  void _startRoute() {
    if (_packageData == null) {
      _showMessage(
        'Primero debes recibir un paquete.',
      );

      return;
    }

    if (_deliveryStatus != DeliveryStatus.received) {
      return;
    }

    setState(() {
      _deliveryStatus = DeliveryStatus.inRoute;
    });

    _showMessage(
      'La entrega está ahora en ruta.',
    );
  }

  void _markAsDelivered() {
    if (_deliveryStatus != DeliveryStatus.inRoute) {
      return;
    }

    setState(() {
      _deliveryStatus = DeliveryStatus.delivered;
    });

    _showMessage(
      'Entrega completada correctamente.',
    );
  }

  void _showMessage(String message) {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!mounted) {
        return;
      }

      final messenger = ScaffoldMessenger.of(context);

      messenger.hideCurrentSnackBar();

      messenger.showSnackBar(
        SnackBar(
          content: Text(message),
        ),
      );
    });
  }

  @override
  void dispose() {
    _packageSubscription?.cancel();
    _mapController?.dispose();

    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'LogiRoute',
              style: TextStyle(
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              'Gestión de entrega',
              style: TextStyle(
                fontSize: 12,
                fontWeight: FontWeight.normal,
              ),
            ),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 16),
            child: Tooltip(
              message: _packageData == null
                  ? 'Esperando conexión con LogiSend'
                  : 'Paquete recibido desde LogiSend',
              child: Icon(
                _packageData == null
                    ? Icons.link_off
                    : Icons.link,
              ),
            ),
          ),
        ],
      ),
      body: SafeArea(
        child: _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    if (_packageData == null) {
      return _buildWaitingScreen();
    }

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        if (_errorMessage != null) ...[
          _buildErrorCard(),
          const SizedBox(height: 14),
        ],
        PackageInformationCard(
          packageData: _packageData!,
        ),
        const SizedBox(height: 14),
        _buildTransporterLocationCard(),
        const SizedBox(height: 14),
        _buildDeliveryMap(),
        const SizedBox(height: 14),
        DeliveryStatusCard(
          status: _deliveryStatus,
          onStartRoute: _startRoute,
          onMarkDelivered: _markAsDelivered,
        ),
        const SizedBox(height: 30),
      ],
    );
  }

  Widget _buildWaitingScreen() {
    return Center(
      child: SingleChildScrollView(
        padding: const EdgeInsets.all(28),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.move_to_inbox_outlined,
              size: 90,
              color: Theme.of(context).colorScheme.primary,
            ),
            const SizedBox(height: 22),
            Text(
              'Esperando paquete',
              textAlign: TextAlign.center,
              style: Theme.of(context)
                  .textTheme
                  .headlineSmall
                  ?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            const Text(
              'Registra un paquete en LogiSend y presiona '
                  '“Enviar paquete a LogiRoute”.',
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            const Chip(
              avatar: Icon(
                Icons.sync,
                size: 18,
              ),
              label: Text(
                'Comunicación disponible',
              ),
            ),
            if (_errorMessage != null) ...[
              const SizedBox(height: 20),
              _buildErrorCard(),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildErrorCard() {
    final colorScheme =
        Theme.of(context).colorScheme;

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: colorScheme.errorContainer,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(
            Icons.error_outline,
            color: colorScheme.onErrorContainer,
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Text(
              _errorMessage ??
                  'Ocurrió un error desconocido.',
              style: TextStyle(
                color: colorScheme.onErrorContainer,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTransporterLocationCard() {
    return Card(
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Ubicación del transportista',
              style: Theme.of(context)
                  .textTheme
                  .titleLarge
                  ?.copyWith(
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 14),
            if (_currentPosition == null)
              const Text(
                'Todavía no se ha obtenido la ubicación actual.',
              )
            else ...[
              Row(
                children: [
                  const Icon(
                    Icons.location_on_outlined,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Latitud: '
                          '${_currentPosition!.latitude.toStringAsFixed(6)}',
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  const Icon(
                    Icons.explore_outlined,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      'Longitud: '
                          '${_currentPosition!.longitude.toStringAsFixed(6)}',
                    ),
                  ),
                ],
              ),
            ],
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _loadingLocation
                  ? null
                  : _obtainCurrentLocation,
              icon: _loadingLocation
                  ? const SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                ),
              )
                  : const Icon(
                Icons.my_location,
              ),
              label: Text(
                _loadingLocation
                    ? 'Obteniendo ubicación...'
                    : 'Obtener mi ubicación',
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDeliveryMap() {
    final packageData = _packageData!;

    final origin = LatLng(
      packageData.origin.latitude,
      packageData.origin.longitude,
    );

    final destination = LatLng(
      packageData.destination.latitude,
      packageData.destination.longitude,
    );

    final courierPosition =
    _currentPosition == null
        ? null
        : LatLng(
      _currentPosition!.latitude,
      _currentPosition!.longitude,
    );

    final markers = <Marker>{
      Marker(
        markerId: const MarkerId('origin'),
        position: origin,
        infoWindow: InfoWindow(
          title: 'Punto de recolección',
          snippet: _coordinateText(origin),
        ),
        icon: BitmapDescriptor.defaultMarkerWithHue(
          BitmapDescriptor.hueGreen,
        ),
      ),
      Marker(
        markerId: const MarkerId('destination'),
        position: destination,
        infoWindow: InfoWindow(
          title: 'Punto de entrega',
          snippet: _coordinateText(destination),
        ),
        icon: BitmapDescriptor.defaultMarkerWithHue(
          BitmapDescriptor.hueRed,
        ),
      ),
      if (courierPosition != null)
        Marker(
          markerId: const MarkerId('courier'),
          position: courierPosition,
          infoWindow: InfoWindow(
            title: 'Transportista',
            snippet: _coordinateText(courierPosition),
          ),
          icon: BitmapDescriptor.defaultMarkerWithHue(
            BitmapDescriptor.hueAzure,
          ),
        ),
    };

    final polylines = <Polyline>{
      Polyline(
        polylineId: const PolylineId(
          'delivery_route',
        ),
        points: [
          origin,
          destination,
        ],
        width: 6,
        color: Theme.of(context).colorScheme.primary,
        geodesic: true,
      ),
    };

    return Card(
      elevation: 1,
      clipBehavior: Clip.antiAlias,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(
              18,
              18,
              18,
              12,
            ),
            child: Row(
              children: [
                Icon(
                  Icons.map_outlined,
                  color:
                  Theme.of(context).colorScheme.primary,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'Mapa de la entrega',
                    style: Theme.of(context)
                        .textTheme
                        .titleLarge
                        ?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                IconButton(
                  tooltip: 'Centrar recorrido',
                  onPressed: () {
                    _fitPackageOnMap(
                      packageData,
                      includeCourier: true,
                    );
                  },
                  icon: const Icon(
                    Icons.center_focus_strong,
                  ),
                ),
              ],
            ),
          ),
          SizedBox(
            height: 350,
            child: GoogleMap(
              initialCameraPosition: CameraPosition(
                target: origin,
                zoom: 13,
              ),
              mapType: MapType.normal,
              markers: markers,
              polylines: polylines,
              myLocationEnabled:
              _currentPosition != null,
              myLocationButtonEnabled:
              _currentPosition != null,
              zoomControlsEnabled: true,
              compassEnabled: true,
              mapToolbarEnabled: true,
              rotateGesturesEnabled: true,
              scrollGesturesEnabled: true,
              tiltGesturesEnabled: true,
              zoomGesturesEnabled: true,
              onMapCreated: (
                  GoogleMapController controller,
                  ) {
                _mapController = controller;

                WidgetsBinding.instance
                    .addPostFrameCallback((_) {
                  if (!mounted) {
                    return;
                  }

                  _fitPackageOnMap(
                    packageData,
                    includeCourier: true,
                  );
                });
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment:
              CrossAxisAlignment.start,
              children: [
                const _MapLegendRow(
                  color: Colors.green,
                  label: 'Origen del paquete',
                ),
                const SizedBox(height: 8),
                const _MapLegendRow(
                  color: Colors.red,
                  label: 'Destino del paquete',
                ),
                if (courierPosition != null) ...[
                  const SizedBox(height: 8),
                  const _MapLegendRow(
                    color: Colors.blue,
                    label:
                    'Ubicación del transportista',
                  ),
                ],
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _fitPackageOnMap(
      PackageData packageData, {
        required bool includeCourier,
      }) async {
    final points = <LatLng>[
      LatLng(
        packageData.origin.latitude,
        packageData.origin.longitude,
      ),
      LatLng(
        packageData.destination.latitude,
        packageData.destination.longitude,
      ),
    ];

    if (includeCourier &&
        _currentPosition != null) {
      points.add(
        LatLng(
          _currentPosition!.latitude,
          _currentPosition!.longitude,
        ),
      );
    }

    await _fitPointsOnMap(points);
  }

  Future<void> _fitPointsOnMap(
      List<LatLng> points,
      ) async {
    final controller = _mapController;

    if (controller == null || points.isEmpty) {
      return;
    }

    /*
     * Da tiempo al mapa para terminar de calcular
     * su tamaño antes de aplicar LatLngBounds.
     */
    await Future<void>.delayed(
      const Duration(milliseconds: 350),
    );

    if (!mounted) {
      return;
    }

    if (_allPointsAreEqual(points)) {
      await controller.animateCamera(
        CameraUpdate.newLatLngZoom(
          points.first,
          16,
        ),
      );

      return;
    }

    var minimumLatitude = points.first.latitude;
    var maximumLatitude = points.first.latitude;
    var minimumLongitude = points.first.longitude;
    var maximumLongitude = points.first.longitude;

    for (final point in points.skip(1)) {
      if (point.latitude < minimumLatitude) {
        minimumLatitude = point.latitude;
      }

      if (point.latitude > maximumLatitude) {
        maximumLatitude = point.latitude;
      }

      if (point.longitude < minimumLongitude) {
        minimumLongitude = point.longitude;
      }

      if (point.longitude > maximumLongitude) {
        maximumLongitude = point.longitude;
      }
    }

    final bounds = LatLngBounds(
      southwest: LatLng(
        minimumLatitude,
        minimumLongitude,
      ),
      northeast: LatLng(
        maximumLatitude,
        maximumLongitude,
      ),
    );

    try {
      await controller.animateCamera(
        CameraUpdate.newLatLngBounds(
          bounds,
          70,
        ),
      );
    } catch (_) {
      /*
       * Respaldo para dispositivos donde el mapa todavía
       * no tenga dimensiones suficientes.
       */
      await controller.animateCamera(
        CameraUpdate.newLatLngZoom(
          points.first,
          13,
        ),
      );
    }
  }

  bool _allPointsAreEqual(
      List<LatLng> points,
      ) {
    if (points.length <= 1) {
      return true;
    }

    final firstPoint = points.first;

    return points.every(
          (point) =>
      point.latitude == firstPoint.latitude &&
          point.longitude == firstPoint.longitude,
    );
  }

  String _coordinateText(LatLng point) {
    return '${point.latitude.toStringAsFixed(6)}, '
        '${point.longitude.toStringAsFixed(6)}';
  }
}

class _MapLegendRow extends StatelessWidget {
  final Color color;
  final String label;

  const _MapLegendRow({
    required this.color,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 14,
          height: 14,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: Text(label),
        ),
      ],
    );
  }
}