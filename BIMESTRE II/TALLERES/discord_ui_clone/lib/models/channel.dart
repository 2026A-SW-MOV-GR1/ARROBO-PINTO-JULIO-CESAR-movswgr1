import 'package:flutter/material.dart';

class Channel {
  const Channel({
    required this.id,
    required this.name,
    required this.category,
    required this.icon,
    this.isFavorite = false,
  });

  final String id;
  final String name;
  final String category;
  final IconData icon;
  final bool isFavorite;
}
