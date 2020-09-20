#import "CameraDeepArPlugin.h"
#if __has_include(<camera_deep_ar/camera_deep_ar-Swift.h>)
#import <camera_deep_ar/camera_deep_ar-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "camera_deep_ar-Swift.h"
#endif

@implementation CameraDeepArPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCameraDeepArPlugin registerWithRegistrar:registrar];
}
@end
