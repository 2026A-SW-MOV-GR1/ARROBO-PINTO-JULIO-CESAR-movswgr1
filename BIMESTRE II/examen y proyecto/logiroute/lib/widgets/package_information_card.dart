import 'package:flutter/material.dart';

import '../models/package_data.dart';

class PackageInformationCard extends StatelessWidget {
  final PackageData packageData;

  const PackageInformationCard({super.key, required this.packageData});

  @override
  Widget build(BuildContext context) {
    final createdAt = DateTime.fromMillisecondsSinceEpoch(
      packageData.createdAtEpochMs,
    );

    return Card(
      elevation: 1,
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  Icons.inventory_2_outlined,
                  color: Theme.of(context).colorScheme.primary,
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Text(
                    'Información del paquete',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ],
            ),
            const Divider(height: 28),
            InformationRow(label: 'Código', value: packageData.packageId),
            InformationRow(label: 'Remitente', value: packageData.senderName),
            InformationRow(
              label: 'Destinatario',
              value: packageData.recipientName,
            ),
            InformationRow(label: 'Contenido', value: packageData.description),
            InformationRow(
              label: 'Peso',
              value: '${packageData.weightKg.toStringAsFixed(2)} kg',
            ),
            InformationRow(label: 'Prioridad', value: packageData.priority),
            InformationRow(label: 'Registro', value: _formatDate(createdAt)),
            const Divider(height: 28),
            InformationRow(
              label: 'Origen',
              value:
                  '${packageData.origin.latitude.toStringAsFixed(6)}, '
                  '${packageData.origin.longitude.toStringAsFixed(6)}',
            ),
            InformationRow(
              label: 'Destino',
              value:
                  '${packageData.destination.latitude.toStringAsFixed(6)}, '
                  '${packageData.destination.longitude.toStringAsFixed(6)}',
            ),
          ],
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final day = date.day.toString().padLeft(2, '0');

    final month = date.month.toString().padLeft(2, '0');

    final hour = date.hour.toString().padLeft(2, '0');

    final minute = date.minute.toString().padLeft(2, '0');

    return '$day/$month/${date.year} '
        '$hour:$minute';
  }
}

class InformationRow extends StatelessWidget {
  final String label;
  final String value;

  const InformationRow({super.key, required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 105,
            child: Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
