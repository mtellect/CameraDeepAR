import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:camera_deep_ar/camera_deep_ar.dart';

void main() {
  const MethodChannel channel = MethodChannel('camera_deep_ar');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await CameraDeepAr.platformVersion, '42');
  });
}
