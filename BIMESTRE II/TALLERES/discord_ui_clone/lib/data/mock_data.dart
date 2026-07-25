import 'package:flutter/material.dart';

import '../models/channel.dart';
import '../models/chat_message.dart';
import '../models/server.dart';

abstract final class MockData {
  static const List<Color> _serverColors = [
    Color(0xFF5865F2),
    Color(0xFF23A559),
    Color(0xFFEB459E),
    Color(0xFFF0B232),
    Color(0xFF3BA55D),
    Color(0xFFED4245),
  ];

  static final List<Server> servers = List.generate(
    30,
    (index) => Server(
      id: 'server-$index',
      name: index == 0 ? 'Comunidad EPN' : 'Comunidad ${index + 1}',
      shortName: index == 0 ? 'EPN' : 'C${index + 1}',
      color: _serverColors[index % _serverColors.length],
      unread: index % 4 == 0,
    ),
  );

  static final List<Channel> channels = [
    const Channel(
      id: 'bienvenida',
      name: 'bienvenida',
      category: 'INFORMACIÓN',
      icon: Icons.campaign_rounded,
      isFavorite: true,
    ),
    const Channel(
      id: 'reglas',
      name: 'reglas',
      category: 'INFORMACIÓN',
      icon: Icons.rule_rounded,
    ),
    const Channel(
      id: 'anuncios',
      name: 'anuncios',
      category: 'INFORMACIÓN',
      icon: Icons.notifications_rounded,
    ),
    const Channel(
      id: 'general',
      name: 'general',
      category: 'COMUNIDAD',
      icon: Icons.tag_rounded,
      isFavorite: true,
    ),
    const Channel(
      id: 'tareas',
      name: 'tareas-y-proyectos',
      category: 'COMUNIDAD',
      icon: Icons.tag_rounded,
    ),
    const Channel(
      id: 'recursos',
      name: 'recursos',
      category: 'COMUNIDAD',
      icon: Icons.folder_rounded,
    ),
    const Channel(
      id: 'soporte',
      name: 'soporte-técnico',
      category: 'AYUDA',
      icon: Icons.support_agent_rounded,
      isFavorite: true,
    ),
    const Channel(
      id: 'dudas',
      name: 'dudas-rápidas',
      category: 'AYUDA',
      icon: Icons.help_rounded,
    ),
    ...List.generate(
      50,
      (index) => Channel(
        id: 'channel-auto-$index',
        name: 'canal-${index + 1}',
        category: index < 18
            ? 'ASIGNATURAS'
            : index < 34
                ? 'PROYECTOS'
                : 'COMUNIDADES',
        icon: index.isEven ? Icons.tag_rounded : Icons.forum_rounded,
        isFavorite: index % 13 == 0,
      ),
    ),
  ];

  static const List<String> _authors = [
    'Andrea',
    'Mateo',
    'Nicole',
    'Fabián',
    'Carlos',
    'Valentina',
    'Diego',
    'Sofía',
  ];

  static const List<String> _texts = [
    '¿Alguien ya revisó el nuevo entregable del taller?',
    'La lista funciona bien incluso con bastantes elementos.',
    'Recuerden usar componentes nativos y evitar WebView.',
    'Ya implementé la búsqueda de canales y mejoró la navegación.',
    'El contraste del texto secundario todavía puede mejorarse.',
    'Probé el desplazamiento y se mantiene fluido.',
    'Voy a subir una captura de la interfaz final.',
    'La propuesta de mejora debe resolver un problema real de UX.',
  ];

  static final List<ChatMessage> messages = List.generate(
    250,
    (index) => ChatMessage(
      id: 'message-$index',
      author: _authors[index % _authors.length],
      content: _texts[index % _texts.length],
      time:
          '${(8 + (index ~/ 60)) % 24}:${(index % 60).toString().padLeft(2, '0')}',
      avatarColor: _serverColors[index % _serverColors.length],
      isMine: index % 9 == 0,
      reactions: index % 7 == 0 ? (index % 5) + 1 : 0,
    ),
  );
}
