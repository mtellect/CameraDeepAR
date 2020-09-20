import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// class CameraDeepAr {
//   static const MethodChannel _channel =
//       const MethodChannel('camera_deep_ar');
//
//   static Future<String> get platformVersion async {
//     final String version = await _channel.invokeMethod('getPlatformVersion');
//     return version;
//   }
// }

typedef void CameraDeepArCallback(CameraDeepArController controller);

class CameraDeepAr extends StatefulWidget {
  final CameraDeepArCallback cameraDeepArCallback;

  const CameraDeepAr({Key key, @required this.cameraDeepArCallback})
      : super(key: key);
  @override
  _CameraDeepArState createState() => _CameraDeepArState();
}

class _CameraDeepArState extends State<CameraDeepAr> {
  @override
  Widget build(BuildContext context) {
    final Map<String, String> args = {"someInit": "initData"};

    if (defaultTargetPlatform == TargetPlatform.android) {
      return AndroidView(
          viewType: 'plugins.flutter.io/deep_ar_camera',
          onPlatformViewCreated: _onPlatformViewCreated,
          creationParams: args,
          creationParamsCodec: StandardMessageCodec());
    }
    return UiKitView(
        viewType: 'plugins.flutter.io/deep_ar_camera',
        onPlatformViewCreated: _onPlatformViewCreated,
        creationParams: args,
        creationParamsCodec: StandardMessageCodec());
  }

  void _onPlatformViewCreated(int id) {
    if (widget.cameraDeepArCallback == null) {
      return;
    }
    widget.cameraDeepArCallback(new CameraDeepArController._(id));
  }
}

class CameraDeepArController {
  CameraDeepArController._(int id)
      : _channel = new MethodChannel('plugins.flutter.io/deep_ar_camera/$id');
  final MethodChannel _channel;

  Future initialize({@required String licenceKey}) async {
    // TODO(amirh): remove this on when the invokeMethod update makes it to stable Flutter.
    // https://github.com/flutter/flutter/issues/26431
    // ignore: strong_mode_implicit_dynamic_method
    return _channel.invokeMethod('initialize', <String, dynamic>{
      'licenceKey': licenceKey,
    });
  }
}
