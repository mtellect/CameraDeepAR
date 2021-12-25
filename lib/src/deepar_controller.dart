import 'dart:async';
import 'dart:io';

import 'package:camera_deep_ar/camera_deep_ar.dart';
import 'package:camera_deep_ar/src/events.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

class CameraDeepArController extends ValueNotifier<DeepArConfig> {
  CameraDeepArController(DeepArConfig value) : super(value);

  // static CameraDeepArController? _instance;
  //
  // /// Get the singleton of [CameraDeepArController].
  // static CameraDeepArController? get instance => _instance;

  bool _isDisposed = false;

  DeepArEventHandler? _eventHandler;

  static late MethodChannel _channel;

  static const EventChannel _eventChannel =
      EventChannel('plugins.flutter.io/deep_ar_camera/events');

  static Stream? _stream;

  static StreamSubscription? _subscription;

  ///
  /// Sets the engine event handler.
  ///
  /// After setting the engine event handler,
  ///
  /// **Parameter** [handler] The event handler.
  ///

  void setEventHandler(DeepArEventHandler handler) {
    _eventHandler = handler;
  }

  void init(int viewId) async {
    value = value.copyWith(isInitialized: true, viewId: viewId);
    _channel = MethodChannel('plugins.flutter.io/deep_ar_camera/$viewId');
    print("init $viewId ${_channel.name}");

    // _channel!.setMethodCallHandler((MethodCall call) async {
    //   print("receivedMessage ${call.method}");
    //   _eventHandler?.process(call.method, call.arguments);
    // });

    ///
    ///start event listening after view is ready!
    ///
    // _stream = _eventChannel.receiveBroadcastStream();
    _subscription = _eventChannel.receiveBroadcastStream().listen((event) {
      print("receiveBroadcastStream $event");
      final eventMap = Map<dynamic, dynamic>.from(event);
      final methodName = eventMap['methodName'] as String;
      final data = eventMap['data'];
      _eventHandler?.process(methodName, data);
    });

    String resp = await _channel.invokeMethod('isCameraReady');
    print("Camera Status $resp");
  }

  Future isCameraReady() async {
    return _channel.invokeMethod('isCameraReady');
  }

  Future switchCamera() async {
    return _channel.invokeMethod('switchCamera');
  }

  Future snapPhoto() async {
    return _channel.invokeMethod('snapPhoto');
  }

  Future startVideoRecording() async {
    return _channel.invokeMethod('startVideoRecording');
  }

  Future stopVideoRecording() async {
    return _channel.invokeMethod('stopVideoRecording');
  }

  Future setCameraMode({required CameraMode mode}) async {
    return _channel.invokeMethod('setCameraMode', <String, dynamic>{
      'mode': CameraMode.values.indexOf(mode),
    });
  }

  Future setDisplayMode({required DisplayMode mode}) async {
    return _channel.invokeMethod('setDisplayMode', <String, dynamic>{
      'mode': DisplayMode.values.indexOf(mode),
    });
  }

  Future setRecordingMode({required RecordingMode recordingMode}) async {
    return _channel.invokeMethod('setRecordingMode', <String, dynamic>{
      'recordingMode': RecordingMode.values.indexOf(recordingMode),
    });
  }

  Future switchCameraDirection({required CameraDirection direction}) async {
    return _channel.invokeMethod('switchCameraDirection', <String, dynamic>{
      'direction': CameraDirection.values.indexOf(direction),
    });
  }

  Future zoomTo(int p) async {
    return _channel.invokeMethod('zoomTo', <String, dynamic>{
      'zoom': p,
    });
  }

  Future switchEffect(CameraMode mode, String path) async {
    late File loadFile;
    late String filePath;

    if (!path.contains("none")) {
      loadFile =
          await createFileFromAsset(path, path.replaceAll("assets/", ""));
      filePath = loadFile.path;
    }
    return _channel.invokeMethod('switchEffect', <String, dynamic>{
      'mode': describeEnum(mode),
      'path': path.contains("none") ? "none" : filePath,
    });
  }

  Future changeParameterFloat(String changeParameter, String component,
      String parameter, double floatValue) async {
    return _channel.invokeMethod('changeParameterFloat', <String, dynamic>{
      'changeParameter': changeParameter,
      'component': component,
      'parameter': parameter,
      'floatValue': floatValue,
    });
  }

  Future changeImage(String filePath) async {
    print("Damon - Involking change Image Flutter");
    return _channel.invokeMethod('changeImage', <String, dynamic>{
      'filePath': filePath,
    });
  }

  //TODO: Implement this on both Android and IOS
  Future changeParameterTexture(String changeParameter, String component,
      String parameter, String texturePath) async {
    return _channel.invokeMethod('changeParameterTexture', <String, dynamic>{
      'changeParameter': changeParameter,
      'component': component,
      'parameter': parameter,
      'texturePath': texturePath,
    });
  }

  Future<File> createFileFromAsset(String path, String name) async {
    final ByteData data = await rootBundle.load(path);
    Directory tempDir = await getTemporaryDirectory();
    File tempFile = File('${tempDir.path}/$name');
    final file =
        await tempFile.writeAsBytes(data.buffer.asUint8List(), flush: true);
    return file;
  }

  @override
  void removeListener(VoidCallback listener) {
    // Prevent ValueListenableBuilder in CameraPreview widget from causing an
    // exception to be thrown by attempting to remove its own listener after
    // the controller has already been disposed.
    if (!_isDisposed) {
      super.removeListener(listener);
    }
  }

  //this is to request permissions
  static checkPermissions() async {
    return await [
      Permission.camera,
      Permission.microphone,
      Permission.storage,
      Permission.manageExternalStorage,
      Permission.mediaLibrary,
    ].request();
  }

  @override
  void dispose() {
    value = value.copyWith();
    _channel.invokeMethod('dispose');
    _subscription?.cancel();
    super.dispose();
  }
}
