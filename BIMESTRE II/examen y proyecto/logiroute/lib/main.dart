import 'package:flutter/material.dart';

import 'screens/delivery_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  runApp(const LogiRouteApp());
}

class LogiRouteApp extends StatelessWidget {
  const LogiRouteApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LogiRoute',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF087F72)),
        useMaterial3: true,
      ),
      home: const DeliveryScreen(),
    );
  }
}
