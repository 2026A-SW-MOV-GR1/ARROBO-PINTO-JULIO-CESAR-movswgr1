import 'package:flutter/material.dart';

enum DeliveryStatus { waiting, received, inRoute, delivered }

extension DeliveryStatusExtension on DeliveryStatus {
  String get label {
    switch (this) {
      case DeliveryStatus.waiting:
        return 'Esperando paquete';

      case DeliveryStatus.received:
        return 'Paquete recibido';

      case DeliveryStatus.inRoute:
        return 'En ruta';

      case DeliveryStatus.delivered:
        return 'Entregado';
    }
  }

  double get progress {
    switch (this) {
      case DeliveryStatus.waiting:
        return 0;

      case DeliveryStatus.received:
        return 0.33;

      case DeliveryStatus.inRoute:
        return 0.66;

      case DeliveryStatus.delivered:
        return 1;
    }
  }

  IconData get icon {
    switch (this) {
      case DeliveryStatus.waiting:
        return Icons.hourglass_empty;

      case DeliveryStatus.received:
        return Icons.inventory_2_outlined;

      case DeliveryStatus.inRoute:
        return Icons.local_shipping_outlined;

      case DeliveryStatus.delivered:
        return Icons.check_circle_outline;
    }
  }
}

class DeliveryStatusCard extends StatelessWidget {
  final DeliveryStatus status;
  final VoidCallback? onStartRoute;
  final VoidCallback? onMarkDelivered;

  const DeliveryStatusCard({
    super.key,
    required this.status,
    this.onStartRoute,
    this.onMarkDelivered,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;

    return Card(
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              'Estado de la entrega',
              style: Theme.of(
                context,
              ).textTheme.titleLarge?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 18),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Row(
                children: [
                  Icon(
                    status.icon,
                    color: colorScheme.onPrimaryContainer,
                    size: 32,
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Text(
                      status.label,
                      style: TextStyle(
                        color: colorScheme.onPrimaryContainer,
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            LinearProgressIndicator(
              value: status.progress,
              minHeight: 8,
              borderRadius: BorderRadius.circular(10),
            ),
            const SizedBox(height: 18),
            if (status == DeliveryStatus.received)
              FilledButton.icon(
                onPressed: onStartRoute,
                icon: const Icon(Icons.route_outlined),
                label: const Text('Iniciar ruta'),
              ),
            if (status == DeliveryStatus.inRoute)
              FilledButton.icon(
                onPressed: onMarkDelivered,
                icon: const Icon(Icons.task_alt),
                label: const Text('Marcar como entregado'),
              ),
            if (status == DeliveryStatus.delivered)
              const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.verified, color: Colors.green),
                  SizedBox(width: 8),
                  Flexible(
                    child: Text(
                      'Entrega completada correctamente',
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: Colors.green,
                      ),
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}
