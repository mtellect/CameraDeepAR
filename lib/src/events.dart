/*
 * *
 *  * Created by mapps on 12/8/21, 7:23 AM
 *  * Copyright (c) 2021 . All rights reserved.
 *  * Last modified 12/8/21, 7:23 AM
 *
 */

// ignore: public_member_api_docs
typedef CameraReadyCallback = void Function(bool err);

// ignore: public_member_api_docs
typedef VideoRecordingCallback = void Function(String path);

// ignore: public_member_api_docs
typedef SnapPhotoCallback = void Function(String path);

typedef SwitchEffectCallback = void Function(String path);

///
/// In the callbacks, the application should avoid time-consuming
/// tasks or call blocking APIs (such as SendMessage), otherwise,
/// the SDK may not work properly.
///
class DeepArEventHandler {
  ///
  /// The `CameraReadyCallback` typedef includes the following parameter:
  /// the bool isReady
  ///
  CameraReadyCallback? onCameraReady;

  ///
  /// The `VideoRecordingCallback` typedef includes the following parameter:
  /// the video path
  ///
  VideoRecordingCallback? onVideoRecordingComplete;

  ///
  /// The `SnapPhotoCallback` typedef includes the following parameter:
  /// the photo path
  ///
  SnapPhotoCallback? onSnapPhotoCompleted;
  SnapPhotoCallback? onSwitchEffect;

  ///
  /// Constructs a [DeepArEventHandler]
  ///

  DeepArEventHandler(
      {this.onCameraReady,
      this.onVideoRecordingComplete,
      this.onSnapPhotoCompleted,
      this.onSwitchEffect});

  void process(String methodName, Map data) {
    switch (methodName) {
      case "onCameraReady":
        bool isReady = data['isReady'];
        onCameraReady?.call(isReady);
        break;
      case "onVideoRecordingComplete":
        onVideoRecordingComplete?.call(data['path']);
        break;
      case "onSnapPhotoCompleted":
        onSnapPhotoCompleted?.call(data['path']);
        break;
      case "onSwitchEffect":
        onSwitchEffect?.call(data['path']);
        break;
    }
  }
}
