
import 'dart:async';

import 'package:flutter/services.dart';

class CameraDeepAr {
  static const MethodChannel _channel =
      const MethodChannel('camera_deep_ar');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
