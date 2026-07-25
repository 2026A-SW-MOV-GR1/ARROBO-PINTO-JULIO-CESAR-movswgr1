import 'package:flutter/material.dart';

class Server {
  const Server({
    required this.id,
    required this.name,
    required this.shortName,
    required this.color,
    this.unread = false,
  });

  final String id;
  final String name;
  final String shortName;
  final Color color;
  final bool unread;
}
