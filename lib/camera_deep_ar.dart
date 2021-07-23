import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

typedef void CameraDeepArCallback(CameraDeepArController controller);
typedef void OnImageCaptured(String path);
typedef void OnVideoRecorded(String path);
typedef void OnCameraReady(bool isCameraReady);

enum RecordingMode { photo, video, lowQVideo }

enum CameraMode { masks, effects, filters }

enum CameraDirection { back, front }

// NOTE: We use Masks.empty to replace none to force the underlying plugin to apply a mask as otherwise it would not.
// Causing issues with loading maskes later on.
// Needed to jumpstart the masks
enum Masks {
  empty,
  aviators,
  bigmouth,
  dalmatian,
  bcgSeg,
  look2,
  fatify,
  flowers,
  grumpycat,
  koala,
  lion,
  mudMask,
  obama,
  pug,
  slash,
  sleepingmask,
  smallface,
  teddycigar,
  tripleface,
  twistedFace,
}

enum Effects {
  none,
  fire,
  heart,
  blizzard,
  rain,
}

enum Filters {
  none,
  tv80,
  drawingmanga,
  sepia,
  bleachbypass,
  realvhs,
  filmcolorperfection,
}

class CameraDeepAr extends StatefulWidget {
  final CameraDeepArCallback cameraDeepArCallback;
  final OnImageCaptured onImageCaptured;
  final OnVideoRecorded onVideoRecorded;
  final OnCameraReady onCameraReady;
  final String androidLicenceKey, iosLicenceKey;
  final RecordingMode recordingMode;
  final CameraDirection cameraDirection;
  final CameraMode cameraMode;
  final List<Filters> supportedFilters;
  final List<Masks> supportedMasks;
  final List<Effects> supportedEffects;
  final String mode;

  const CameraDeepAr(
      {Key? key,
      required this.cameraDeepArCallback,
      required this.androidLicenceKey,
      required this.iosLicenceKey,
      required this.onImageCaptured,
      required this.onVideoRecorded,
      required this.onCameraReady,
      this.mode = "camera",
      this.cameraMode = CameraMode.masks,
      this.cameraDirection = CameraDirection.front,
      this.recordingMode = RecordingMode.video,
      this.supportedFilters = const [
        Filters.sepia,
        Filters.bleachbypass,
      ],
      this.supportedMasks = const [
        Masks.empty,
        Masks.aviators,
        Masks.bigmouth,
        Masks.dalmatian,
        Masks.look2,
        Masks.flowers,
        Masks.grumpycat,
        Masks.lion,
      ],
      this.supportedEffects = const [
        Effects.none,
        Effects.fire,
        Effects.heart,
      ]})
      : super(key: key);
  @override
  _CameraDeepArState createState() => _CameraDeepArState();
}

class _CameraDeepArState extends State<CameraDeepAr> {
  late CameraDeepArController _controller;
  bool hasPermission = false;
  List<Effects> get supportedEffects => widget.supportedEffects;
  List<Filters> get supportedFilters => widget.supportedFilters;
  List<Masks> get supportedMasks => widget.supportedMasks;

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
    // DeepCameraArPermissions.checkForPermission().then((value) {
    //   print("Value checked.... $value");
    //
    //   if (this.mounted)
    //     setState(() {
    //       hasPermission = value;
    //     });
    // });
  }

  @override
  void dispose() {
    // TODO: implement dispose
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final Map<String, Object> args = {
      "androidLicenceKey": widget.androidLicenceKey,
      "iosLicenceKey": widget.iosLicenceKey,
      "recordingMode": RecordingMode.values.indexOf(widget.recordingMode),
      "direction": CameraDirection.values.indexOf(widget.cameraDirection),
      "cameraMode": CameraMode.values.indexOf(widget.cameraMode),
      "mode": widget.mode,
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

class DeepCameraArPermissions {
  static const MethodChannel _channel = const MethodChannel('camera_deep_ar');

  static Future<bool> checkForPermission() async {
    return await _channel.invokeMethod('checkForPermission');
  }
}

class CameraDeepArController {
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
      case "onVideoRecordingComplete":
        String path = call.arguments['path'] as String;
        _cameraDeepArState.onVideoRecorded(path);
        break;
      case "onSnapPhotoCompleted":
        String path = call.arguments['path'] as String;
        _cameraDeepArState.onImageCaptured(path);
        break;
      default:
        throw MissingPluginException();
    }
  }

  Future isCameraReady() async {
    return channel.invokeMethod('isCameraReady');
  }

  Future dispose() async {
    return channel.invokeMethod('dispose');
  }

  Future switchCamera() async {
    return channel.invokeMethod('switchCamera');
  }

  Future snapPhoto() async {
    return channel.invokeMethod('snapPhoto');
  }

  Future startVideoRecording() async {
    return channel.invokeMethod('startVideoRecording');
  }

  Future stopVideoRecording() async {
    return channel.invokeMethod('stopVideoRecording');
  }

  // Future next({@required String licenceKey}) async {
  //   return channel.invokeMethod('next');
  // }
  //
  // Future previous({@required String licenceKey}) async {
  //   return channel.invokeMethod('previous');
  // }

  Future setCameraMode({required CameraMode camMode}) async {
    return channel.invokeMethod('setCameraMode', <String, dynamic>{
      'cameraMode': CameraMode.values.indexOf(camMode),
    });
  }

  Future setRecordingMode({required RecordingMode recordingMode}) async {
    return channel.invokeMethod('setRecordingMode', <String, dynamic>{
      'recordingMode': RecordingMode.values.indexOf(recordingMode),
    });
  }

  Future switchCameraDirection({required CameraDirection direction}) async {
    return channel.invokeMethod('switchCameraDirection', <String, dynamic>{
      'direction': CameraDirection.values.indexOf(direction),
    });
  }

  Future zoomTo(int p) async {
    return channel.invokeMethod('zoomTo', <String, dynamic>{
      'zoom': p,
    });
  }

  Future changeMask(int p) async {
    int sendNative = p;
    if (_cameraDeepArState.supportedEffects.isNotEmpty) {
      Masks e = _cameraDeepArState.supportedMasks[p];
      sendNative = Masks.values.indexOf(e);
    }
    if (p > Masks.values.length - 1) p = 0;
    return channel.invokeMethod('changeMask', <String, dynamic>{
      'mask': sendNative,
    });
  }

  Future changeEffect(int p) async {
    int sendNative = p;
    if (_cameraDeepArState.supportedEffects.isNotEmpty) {
      Effects e = _cameraDeepArState.supportedEffects[p];
      sendNative = Effects.values.indexOf(e);
    }
    if (p > Effects.values.length - 1) p = 0;
    return channel.invokeMethod('changeEffect', <String, dynamic>{
      'effect': sendNative,
    });
  }

  Future changeFilter(int p) async {
    int sendNative = p;
    if (_cameraDeepArState.supportedEffects.isNotEmpty) {
      Filters e = _cameraDeepArState.supportedFilters[p];
      sendNative = Filters.values.indexOf(e);
    }
    if (p > Filters.values.length - 1) p = 0;
    return channel.invokeMethod('changeFilter', <String, dynamic>{
      'filter': sendNative,
    });
  }

  Future switchEffect(String mode, String path) async {
    return channel.invokeMethod('switchEffect', <String, dynamic>{
      'mode': mode,
      'path': path,
    });
  }

  Future changeParameterFloat(String changeParameter, String component,
      String parameter, double floatValue) async {
    return channel.invokeMethod('changeParameterFloat', <String, dynamic>{
      'changeParameter': changeParameter,
      'component': component,
      'parameter': parameter,
      'floatValue': floatValue,
    });
  }

  Future changeImage(String filePath) async {
    print("Damon - Involking change Image Flutter");
    return channel.invokeMethod('changeImage', <String, dynamic>{
      'filePath': filePath,
    });
  }

  //TODO: Implement this on both Android and IOS
  Future changeParameterTexture(String changeParameter, String component,
      String parameter, String texturePath) async {
    return channel.invokeMethod('changeParameterTexture', <String, dynamic>{
      'changeParameter': changeParameter,
      'component': component,
      'parameter': parameter,
      'texturePath': texturePath,
    });
  }
}
