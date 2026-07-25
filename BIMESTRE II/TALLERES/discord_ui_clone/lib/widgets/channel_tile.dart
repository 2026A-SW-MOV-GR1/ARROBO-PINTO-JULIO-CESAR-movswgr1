import 'package:flutter/material.dart';

import '../models/channel.dart';
import '../theme/app_colors.dart';

class ChannelTile extends StatelessWidget {
  const ChannelTile({
    super.key,
    required this.channel,
    required this.selected,
    required this.favorite,
    required this.onTap,
    required this.onFavorite,
  });

  final Channel channel;
  final bool selected;
  final bool favorite;
  final VoidCallback onTap;
  final VoidCallback onFavorite;

  @override
  Widget build(BuildContext context) {
    return RepaintBoundary(
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 160),
        margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 2),
        decoration: BoxDecoration(
          color: selected ? AppColors.selected : Colors.transparent,
          borderRadius: BorderRadius.circular(9),
        ),
        child: ListTile(
          dense: true,
          onTap: onTap,
          leading: Icon(
            channel.icon,
            color: selected ? AppColors.textPrimary : AppColors.textMuted,
          ),
          title: Text(
            channel.name,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
              color: selected ? AppColors.textPrimary : AppColors.textSecondary,
              fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
            ),
          ),
          trailing: IconButton(
            tooltip: favorite ? 'Quitar de favoritos' : 'Agregar a favoritos',
            onPressed: onFavorite,
            icon: Icon(
              favorite ? Icons.star_rounded : Icons.star_border_rounded,
              color: favorite ? AppColors.warning : AppColors.textMuted,
              size: 21,
            ),
          ),
        ),
      ),
    );
  }
}
