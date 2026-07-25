import 'package:flutter/material.dart';

class ChatMessage {
  const ChatMessage({
    required this.id,
    required this.author,
    required this.content,
    required this.time,
    required this.avatarColor,
    this.isMine = false,
    this.reactions = 0,
  });

  final String id;
  final String author;
  final String content;
  final String time;
  final Color avatarColor;
  final bool isMine;
  final int reactions;
}
