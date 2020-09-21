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
typedef void OnImageCaptured(String path);
typedef void OnVideoRecorded(String path);
typedef void OnCameraReady(bool isCameraReady);

class CameraDeepAr extends StatefulWidget {
  final CameraDeepArCallback cameraDeepArCallback;
  final OnImageCaptured onImageCaptured;
  final OnVideoRecorded onVideoRecorded;
  final OnCameraReady onCameraReady;
  final String androidLicenceKey, iosLicenceKey;

  const CameraDeepAr(
      {Key key,
      @required this.cameraDeepArCallback,
      @required this.androidLicenceKey,
      @required this.iosLicenceKey,
      @required this.onImageCaptured,
      @required this.onVideoRecorded,
      @required this.onCameraReady})
      : super(key: key);
  @override
  _CameraDeepArState createState() => _CameraDeepArState();
}

class _CameraDeepArState extends State<CameraDeepAr> {
  StreamController stream = StreamController<String>.broadcast();
  CameraDeepArController _controller;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    stream.stream.listen((path) {});
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
    stream?.close();
  }

  @override
  Widget build(BuildContext context) {
    final Map<String, String> args = {
      "androidLicenceKey": widget.androidLicenceKey ?? "",
      "iosLicenceKey": widget.iosLicenceKey ?? ""
    };

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

  void _onPlatformViewCreated(int id) async {
    final CameraDeepArController controller = await CameraDeepArController.init(
      id,
      this,
    );
    if (widget.cameraDeepArCallback == null) {
      return;
    }
    widget.cameraDeepArCallback(controller);
    _controller = controller;
  }

  void onImageCaptured(String path) {
    widget.onImageCaptured(path);
  }

  void onVideoRecorded(String path) {
    widget.onVideoRecorded(path);
  }

  void onCameraReady(bool ready) {
    widget.onCameraReady(ready);
  }
}

class CameraDeepArController {
  // CameraDeepArController._(int id)
  //     : _channel = new MethodChannel('plugins.flutter.io/deep_ar_camera/$id');
  // final MethodChannel _channel;

  CameraDeepArController._(
    this.channel,
    this._cameraDeepArState,
  ) : assert(channel != null) {
    channel.setMethodCallHandler(_handleMethodCall);
  }

  static Future<CameraDeepArController> init(
    int id,
    _CameraDeepArState _cameraDeepArState,
  ) async {
    assert(id != null);
    final MethodChannel channel =
        MethodChannel('plugins.flutter.io/deep_ar_camera/$id');
    String resp = await channel.invokeMethod('isCameraReady');
    print("Camera Status $resp");
    return CameraDeepArController._(
      channel,
      _cameraDeepArState,
    );
  }

  @visibleForTesting
  final MethodChannel channel;

  final _CameraDeepArState _cameraDeepArState;

  Future<dynamic> _handleMethodCall(MethodCall call) async {
    switch (call.method) {
      case "onCameraReady":
        bool isReady = call.arguments['isReady'] as bool;
        _cameraDeepArState.onCameraReady(isReady);
        break;
      case "didFinishVideoRecording":
        String path = call.arguments['path'] as String;
        _cameraDeepArState.onVideoRecorded(path);
        break;
      case "didFinishSnapPhoto":
        String path = call.arguments['path'] as String;
        _cameraDeepArState.onImageCaptured(path);
        break;
      default:
        throw MissingPluginException();
    }
  }

  // Future isCameraReady() async {
  //   return channel.invokeMethod('isCameraReady');
  // }

  Future switchCamera({@required String licenceKey}) async {
    return channel.invokeMethod('switchCamera', <String, dynamic>{
      'licenceKey': licenceKey,
    });
  }

  Future snapPhoto() async {
    return channel.invokeMethod('snapPhoto');
  }

  Future startRecording() async {
    return channel.invokeMethod('startRecording');
  }

  Future stopRecording() async {
    return channel.invokeMethod('stopRecording');
  }

  Future next({@required String licenceKey}) async {
    return channel.invokeMethod('next');
  }

  Future previous({@required String licenceKey}) async {
    return channel.invokeMethod('previous');
  }

  Future setCameraMode({@required CameraMode camMode}) async {
    return channel.invokeMethod('setCameraMode', <String, dynamic>{
      'cameraMode': CameraMode.values.indexOf(camMode),
    });
  }

  Future setCameraEffect({@required CameraEffects camEffects}) async {
    return channel.invokeMethod('setCameraEffect', <String, dynamic>{
      'cameraEffect': CameraEffects.values.indexOf(camEffects),
    });
  }

  Future switchCameraDirection({@required CameraDirection direction}) async {
    return channel.invokeMethod('switchCameraDirection', <String, dynamic>{
      'direction': CameraDirection.values.indexOf(direction),
    });
  }
}

enum CameraMode { photo, video, lowQVideo }

enum CameraEffects { masks, effects, filters }

enum CameraDirection { front, back }
