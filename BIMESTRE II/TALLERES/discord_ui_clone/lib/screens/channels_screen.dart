import 'package:flutter/material.dart';

import '../data/mock_data.dart';
import '../models/channel.dart';
import '../theme/app_colors.dart';
import '../widgets/channel_tile.dart';

class ChannelsScreen extends StatefulWidget {
  const ChannelsScreen({super.key});

  @override
  State<ChannelsScreen> createState() => _ChannelsScreenState();
}

class _ChannelsScreenState extends State<ChannelsScreen> {
  final TextEditingController _searchController = TextEditingController();
  final Set<String> _favoriteIds = {
    for (final channel in MockData.channels)
      if (channel.isFavorite) channel.id,
  };

  String _query = '';
  String _selectedId = 'general';

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  List<Channel> get _filteredChannels {
    final normalized = _query.trim().toLowerCase();
    if (normalized.isEmpty) return MockData.channels;

    return MockData.channels
        .where(
          (channel) =>
              channel.name.toLowerCase().contains(normalized) ||
              channel.category.toLowerCase().contains(normalized),
        )
        .toList(growable: false);
  }

  @override
  Widget build(BuildContext context) {
    final channels = _filteredChannels;

    return SafeArea(
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 18, 16, 10),
            child: Row(
              children: [
                const CircleAvatar(
                  radius: 22,
                  backgroundColor: AppColors.blurple,
                  child: Text(
                    'EPN',
                    style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Comunidad EPN',
                        style: Theme.of(context).textTheme.titleLarge?.copyWith(
                              color: AppColors.textPrimary,
                              fontWeight: FontWeight.w900,
                            ),
                      ),
                      const Text(
                        'Canales y categorías',
                        style: TextStyle(color: AppColors.textMuted),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  tooltip: 'Opciones',
                  onPressed: () {},
                  icon: const Icon(Icons.more_vert_rounded),
                ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 0, 12, 10),
            child: TextField(
              controller: _searchController,
              onChanged: (value) => setState(() => _query = value),
              textInputAction: TextInputAction.search,
              decoration: InputDecoration(
                hintText: 'Buscar canal o categoría',
                prefixIcon: const Icon(
                  Icons.search_rounded,
                  color: AppColors.textMuted,
                ),
                suffixIcon: _query.isEmpty
                    ? null
                    : IconButton(
                        tooltip: 'Limpiar búsqueda',
                        onPressed: () {
                          _searchController.clear();
                          setState(() => _query = '');
                        },
                        icon: const Icon(Icons.close_rounded),
                      ),
              ),
            ),
          ),
          if (_favoriteIds.isNotEmpty && _query.isEmpty)
            SizedBox(
              height: 54,
              child: ListView(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                scrollDirection: Axis.horizontal,
                children: _favoriteIds.map((id) {
                  final channel = MockData.channels.firstWhere(
                    (item) => item.id == id,
                  );
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ActionChip(
                      avatar: const Icon(
                        Icons.star_rounded,
                        color: AppColors.warning,
                        size: 18,
                      ),
                      label: Text('# ${channel.name}'),
                      onPressed: () {
                        setState(() => _selectedId = channel.id);
                      },
                    ),
                  );
                }).toList(growable: false),
              ),
            ),
          Expanded(
            child: AnimatedSwitcher(
              duration: const Duration(milliseconds: 180),
              child: channels.isEmpty
                  ? const _EmptyChannels(key: ValueKey('empty'))
                  : ListView.builder(
                      key: ValueKey('list-$_query'),
                      padding: const EdgeInsets.only(bottom: 24),
                      itemCount: channels.length,
                      itemBuilder: (context, index) {
                        final channel = channels[index];
                        final bool showHeader = index == 0 ||
                            channels[index - 1].category != channel.category;

                        return Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            if (showHeader)
                              Padding(
                                padding:
                                    const EdgeInsets.fromLTRB(16, 14, 16, 4),
                                child: Text(
                                  channel.category,
                                  style: const TextStyle(
                                    color: AppColors.textMuted,
                                    fontSize: 12,
                                    fontWeight: FontWeight.w800,
                                    letterSpacing: 0.7,
                                  ),
                                ),
                              ),
                            ChannelTile(
                              key: ValueKey(channel.id),
                              channel: channel,
                              selected: channel.id == _selectedId,
                              favorite: _favoriteIds.contains(channel.id),
                              onTap: () {
                                setState(() => _selectedId = channel.id);
                              },
                              onFavorite: () {
                                setState(() {
                                  if (!_favoriteIds.add(channel.id)) {
                                    _favoriteIds.remove(channel.id);
                                  }
                                });
                              },
                            ),
                          ],
                        );
                      },
                    ),
            ),
          ),
        ],
      ),
    );
  }
}

class _EmptyChannels extends StatelessWidget {
  const _EmptyChannels({super.key});

  @override
  Widget build(BuildContext context) {
    return const Center(
      child: Padding(
        padding: EdgeInsets.all(32),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              Icons.search_off_rounded,
              size: 54,
              color: AppColors.textMuted,
            ),
            SizedBox(height: 10),
            Text(
              'No se encontraron canales',
              style: TextStyle(
                color: AppColors.textPrimary,
                fontWeight: FontWeight.w800,
              ),
            ),
            SizedBox(height: 4),
            Text(
              'Prueba con otra palabra.',
              style: TextStyle(color: AppColors.textMuted),
            ),
          ],
        ),
      ),
    );
  }
}
