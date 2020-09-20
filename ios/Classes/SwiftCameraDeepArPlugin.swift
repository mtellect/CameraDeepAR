import Flutter
import UIKit
import DeepAR

public class SwiftCameraDeepArPlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "camera_deep_ar", binaryMessenger: registrar.messenger())
    let instance = SwiftCameraDeepArPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    result("iOS " + UIDevice.current.systemVersion)
  }
}
