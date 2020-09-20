//  CameraController.h
//
//  Copyright © 2020 DeepAR. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "DeepAR.h"

@class ARView;

@interface CameraController : NSObject

@property (nonatomic, weak) DeepAR* deepAR;
@property (nonatomic, weak) ARView* arview;

@property (nonatomic, assign) AVCaptureDevicePosition position;
@property (nonatomic, strong) AVCaptureSessionPreset preset;
@property (nonatomic, assign) AVCaptureVideoOrientation videoOrientation;

- (instancetype)init;

// Checks camera permissions
- (void)checkCameraPermission;

// Checks microphone permissions
- (void)checkMicrophonePermission;

// Starts camera preview using AVFoundation. Checks camera permissions and asks if none has been given. If DeepAR started in rendering mode will render camera frames to the ARView.
- (void)startCamera;

// Stops camera preview.
- (void)stopCamera;

//Starts capturing audio samples using AVFoundation. Checks permissions and asks if none has been given. Must be called if startRecording has been called with recordAudio parameter set to true.
- (void)startAudio;

// Stops capturing audio samples.
- (void)stopAudio;

@end
