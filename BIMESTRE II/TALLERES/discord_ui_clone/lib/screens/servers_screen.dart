import 'package:flutter/material.dart';

import '../data/mock_data.dart';
import '../models/server.dart';
import '../theme/app_colors.dart';
import '../widgets/server_tile.dart';

class ServersScreen extends StatefulWidget {
  const ServersScreen({super.key});

  @override
  State<ServersScreen> createState() => _ServersScreenState();
}

class _ServersScreenState extends State<ServersScreen> {
  String _selectedId = MockData.servers.first.id;

  @override
  Widget build(BuildContext context) {
    final selected = MockData.servers.firstWhere(
      (server) => server.id == _selectedId,
    );

    return SafeArea(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const _ScreenHeader(
            title: 'Servidores',
            subtitle: 'Selector horizontal virtualizado',
          ),
          SizedBox(
            height: 88,
            child: ListView.separated(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              scrollDirection: Axis.horizontal,
              itemCount: MockData.servers.length,
              separatorBuilder: (_, __) => const SizedBox(width: 12),
              itemBuilder: (context, index) {
                final server = MockData.servers[index];
                return Stack(
                  alignment: Alignment.centerLeft,
                  children: [
                    if (server.unread)
                      Positioned(
                        left: -4,
                        child: Container(
                          width: 4,
                          height: 18,
                          decoration: BoxDecoration(
                            color: AppColors.textPrimary,
                            borderRadius: BorderRadius.circular(4),
                          ),
                        ),
                      ),
                    Padding(
                      padding: const EdgeInsets.only(left: 4),
                      child: ServerTile(
                        key: ValueKey(server.id),
                        server: server,
                        selected: server.id == _selectedId,
                        onTap: () {
                          setState(() => _selectedId = server.id);
                        },
                      ),
                    ),
                  ],
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 14, 16, 8),
            child: Text(
              'Actividad reciente',
              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    color: AppColors.textPrimary,
                    fontWeight: FontWeight.w800,
                  ),
            ),
          ),
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
              itemCount: 40,
              itemBuilder: (context, index) {
                return _ActivityCard(
                  key: ValueKey('activity-$index'),
                  server: selected,
                  index: index,
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}

class _ScreenHeader extends StatelessWidget {
  const _ScreenHeader({
    required this.title,
    required this.subtitle,
  });

  final String title;
  final String subtitle;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 18, 16, 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  color: AppColors.textPrimary,
                  fontWeight: FontWeight.w900,
                ),
          ),
          const SizedBox(height: 3),
          Text(
            subtitle,
            style: const TextStyle(color: AppColors.textMuted),
          ),
        ],
      ),
    );
  }
}

class _ActivityCard extends StatelessWidget {
  const _ActivityCard({
    super.key,
    required this.server,
    required this.index,
  });

  final Server server;
  final int index;

  @override
  Widget build(BuildContext context) {
    return RepaintBoundary(
      child: Card(
        margin: const EdgeInsets.only(bottom: 10),
        color: AppColors.surface,
        elevation: 0,
        child: ListTile(
          leading: CircleAvatar(
            backgroundColor: server.color,
            child: Text(
              server.shortName.characters.first,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
          title: Text(
            index.isEven
                ? 'Nuevo mensaje en #general'
                : 'Actividad en ${server.name}',
            style: const TextStyle(
              color: AppColors.textPrimary,
              fontWeight: FontWeight.w700,
            ),
          ),
          subtitle: Text(
            'Actualización simulada ${index + 1}',
            style: const TextStyle(color: AppColors.textMuted),
          ),
          trailing: const Icon(
            Icons.chevron_right_rounded,
            color: AppColors.textMuted,
          ),
        ),
      ),
    );
  }
}
