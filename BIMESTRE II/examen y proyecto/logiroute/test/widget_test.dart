import 'package:flutter_test/flutter_test.dart';
import 'package:logiroute/main.dart';

void main() {
  testWidgets('LogiRoute muestra la pantalla de espera al iniciar', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const LogiRouteApp());

    await tester.pumpAndSettle();

    expect(find.text('LogiRoute'), findsOneWidget);

    expect(find.text('Esperando paquete'), findsOneWidget);

    expect(find.text('Comunicación disponible'), findsOneWidget);
  });
}
