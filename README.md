# camera_deep_ar


A new Flutter plugin for Camera video and Photo Augmented reality recording. This plug-in requires Android SDK 19+ and iOS 10+


## Getting Started

Get your ApiKeys from DeepAr [a link](https://www.deepar.ai/)

![DeepAr Camera Demo](demo.gif)



```dart

## Create DeepAr Configuration

DeepArConfig config = DeepArConfig(
androidKey:
"3b58c448bd650192e7c53d965cfe5dc1c341d2568b663a3962b7517c4ac6eeed0ba1fb2afe491a4b",
ioskey:
"53618212114fc16bbd7499c0c04c2ca11a4eed188dc20ed62a7f7eec02b41cb34d638e72945a6bf6",
displayMode: DisplayMode.camera,
// displayMode: DisplayMode.camera,
);


## Create the controller

final deepArController = CameraDeepArController(config);
String _platformVersion = 'Unknown';
bool isRecording = false;
CameraMode cameraMode = config.cameraMode;
DisplayMode displayMode = config.displayMode;


deepArController.setEventHandler(DeepArEventHandler(onCameraReady: (v) {
_platformVersion = "onCameraReady $v";
setState(() {});
}, onSnapPhotoCompleted: (v) {
_platformVersion = "onSnapPhotoCompleted $v";
setState(() {});
}, onVideoRecordingComplete: (v) {
_platformVersion = "onVideoRecordingComplete $v";
setState(() {});
}, onSwitchEffect: (v) {
_platformVersion = "onSwitchEffect $v";
setState(() {});
}));


## Create DeepAR Camera preview widget

DeepArPreview(deepArController),


## Controller Functions

// To take photos
deepArController.snapPhoto();

// To start recording
deepArController.startVideoRecording();

// To switch effects
deepArController.switchEffect(cameraMode, imgPath);

// To stop video recording
deepArController.stopVideoRecording();


```


## Android

You can use [Permission_handler](https://pub.dev/packages/permission_handler), a permissions plugin for Flutter.
Require and add the following permissions in your manifest:

Add this to the proguard-rules.pro

```

-keepclassmembers class ai.deepar.ar.DeepAR { *; }

```

for release mode modify and add to your BuildType in your build.gradle

```
buildTypes {
release {
    // TODO: Add your own signing config for the release build.
    // Signing with the debug keys for now, so `flutter run --release` works.
    signingConfig signingConfigs.debug
    proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
}
}

```


```java

<uses-permission android:name="android.permission.FLASHLIGHT" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />


```

## iOS

You only need add the permission message on the Info.plist

```swift

<key>NSCameraUsageDescription</key>
<string>Allows you to capture your best moment</string>
<key>io.flutter.embedded_views_preview</key>
<true/>
<key>NSPhotoLibraryAddUsageDescription</key>
<string>Allows you to capture your best moment</string>
<key>NSMicrophoneUsageDescription</key>
<string>Needs access to your mic to help your record voice notes on chat/message conversations</string>

```