import 'package:flutter/material.dart';

import '../models/server.dart';
import '../theme/app_colors.dart';

class ServerTile extends StatelessWidget {
  const ServerTile({
    super.key,
    required this.server,
    required this.selected,
    required this.onTap,
  });

  final Server server;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      selected: selected,
      label: server.name,
      child: InkWell(
        borderRadius: BorderRadius.circular(selected ? 18 : 28),
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 180),
          curve: Curves.easeOut,
          width: 58,
          height: 58,
          decoration: BoxDecoration(
            color: selected ? AppColors.blurple : server.color,
            borderRadius: BorderRadius.circular(selected ? 18 : 28),
            boxShadow: selected
                ? const [
                    BoxShadow(
                      color: Color(0x665865F2),
                      blurRadius: 14,
                      offset: Offset(0, 6),
                    ),
                  ]
                : null,
          ),
          alignment: Alignment.center,
          child: Text(
            server.shortName,
            style: const TextStyle(
              color: Colors.white,
              fontWeight: FontWeight.w800,
              fontSize: 15,
            ),
          ),
        ),
      ),
    );
  }
}
