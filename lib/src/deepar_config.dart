import 'package:camera_deep_ar/src/deepar_controller.dart';

import 'enums.dart';

/// The state of a [CameraDeepArController].

class DeepArConfig {
  /// This is used to set the android key for deepar
  final String androidKey;

  /// This is used to set the ios key for deepar
  final String ioskey;

  /// This is the current display mode for deepar
  final DisplayMode displayMode;

  /// This is the current camera mode for deepar
  final CameraMode cameraMode;

  /// This is the current camera direction for deepar
  final CameraDirection cameraDirection;

  /// This is the current video recording mode for deepar
  final RecordingMode recordingMode;

  /// True after [CameraDeepArController.initialize] has completed successfully.
  final bool isInitialized;

  /// Value of the view is set after [CameraDeepArController.initialize] has completed successfully.
  final int? viewId;

  final bool? isRecording;

  /// Creates a new deepAr camera controller state.
  const DeepArConfig(
      {required this.androidKey,
      required this.ioskey,
      this.isInitialized = false,
      this.isRecording = false,
      this.displayMode = DisplayMode.camera,
      this.cameraMode = CameraMode.mask,
      this.cameraDirection = CameraDirection.front,
      this.recordingMode = RecordingMode.photo,
      this.viewId});

  /// Creates a modified copy of the object.
  ///
  /// Explicitly specified fields get the specified value, all other fields get
  /// the same value of the current object.

  DeepArConfig copyWith(
      {String? androidKey,
      String? ioskey,
      DisplayMode? displayMode,
      CameraMode? cameraMode,
      CameraDirection? cameraDirection,
      RecordingMode? recordingMode,
      bool? isInitialized,
      bool? isRecording,
      int? viewId}) {
    return DeepArConfig(
        androidKey: androidKey ?? this.androidKey,
        ioskey: ioskey ?? this.ioskey,
        displayMode: displayMode ?? this.displayMode,
        cameraMode: cameraMode ?? this.cameraMode,
        cameraDirection: cameraDirection ?? this.cameraDirection,
        recordingMode: recordingMode ?? this.recordingMode,
        isInitialized: isInitialized ?? this.isInitialized,
        isRecording: isRecording ?? this.isRecording,
        viewId: viewId ?? this.viewId);
  }

  @override
  String toString() {
    return '$runtimeType('
        'androidKey: $androidKey, '
        'ioskey: $ioskey, '
        'displayMode: $displayMode, '
        'cameraMode: $cameraMode, '
        'cameraDirection: $cameraDirection, '
        'recordingMode: $recordingMode, '
        'isInitialized: $isInitialized)';
  }

  Map<String, Object> toJson() {
    return {
      'androidLicenceKey': androidKey,
      'iosLicenceKey': ioskey,
      'displayMode': DisplayMode.values.indexOf(displayMode),
      'cameraMode': CameraMode.values.indexOf(cameraMode),
      'cameraDirection': CameraDirection.values.indexOf(cameraDirection),
      'recordingMode': RecordingMode.values.indexOf(recordingMode),
    };
  }
}
