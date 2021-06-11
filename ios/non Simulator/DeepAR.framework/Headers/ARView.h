//
//  ARView.h
//
//  Copyright © 2017 DeepAR. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>
#import "DeepAR.h"

@interface ARView : UIView

@property (nonatomic, strong) DeepAR* deepAR;

@property (nonatomic, readonly) BOOL initialized;
@property (nonatomic, readonly) BOOL faceVisible;
@property (nonatomic, readonly) CGSize renderingResolution;

@property (nonatomic, weak) id<DeepARDelegate> delegate;

// You can get your license key on https://developer.deepar.ai
- (void)setLicenseKey:(NSString*)key;

// Starts the engine initialization.
- (void)initialize;

// Resumes the rendering
- (void)resume;

// Pauses the rendering.
- (void)pause;

// Load and switch to effect.
// slot - this parameter is used to specify a "namespace" for effect. No two effects can be in
// one slot, so if we load new effect into already occupied slot, the old effect will be
// removed.
// path - The absolute path to the effect file.
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path;

// Switch effect for the face. Allowed values for face parameters are 0,1,2,3.
// This will only work if the DeepAR SDK build has multi face tracking enabled
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path face:(uint32_t)face;
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path face:(uint32_t)face targetGameObject:(NSString*)targetGameObject;

// Produces a snapshot of the current screen preview. Resolution is equal to the dimension with which the DeepAR has been initialized. The DeepARDelegate  method didTakeScreenshot will be called upon successful screenshot capture is finished with a path where the image has been temporarily stored. 
- (void)takeScreenshot;

// Starts video recording of the ARView with given outputWidth x outputHeight resolution.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight;

//Starts video recording of the ARView with given outputWidth x outputHeight resolution. The subframe parameter defines the sub rectangle of the  ARView that you want to record in normalized coordinates (0.0 - 1.0).
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe;

//Starts video recording of the ARView with given outputWidth x outputHeight resolution. The subframe parameter defines the sub rectangle of the  ARView that you want to record in normalized coordinates (0.0 - 1.0). videoCompressionProperties is an NSDictionary used as the value for the key AVVideoCompressionPropertiesKey.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe videoCompressionProperties:(NSDictionary*)videoCompressionProperties;

//Same as the previous method, additionally indicates that you want to record audio too. If recordAudio parameter is set to true the recording will wait until you call enqueueAudioSample on ARView. When DeepAR is ready to receive audio samples it will publish NSNotification with key deepar_start_audio. You can subscribe to this notification and start feeding audio samples once you receive it. If you use provided CameraController this is handled for you by default.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe videoCompressionProperties:(NSDictionary*)videoCompressionProperties recordAudio:(BOOL)recordAudio;

// Finishes the video recording. Delegate method didFinishVideoRecording will be called when the recording is done with the temporary path of the recorded video.
- (void)finishVideoRecording;

//Pauses video recording if it has been started beforehand.
- (void)pauseVideoRecording;

// Resumes video recording after it has been paused with pauseVideoRecording.
- (void)resumeVideoRecording;


// Enables or disables audio pitch processing for video recording.
- (void)enableAudioProcessing:(BOOL)enabled;

// Sets the pitch change amount. Negative values will make the recorded audio lower in pitch and positive values will make it higher in pitch. Must call enableAudioProcessing to enable the pitch processing beforehand.
- (void)setAudioProcessingSemitone:(float)sts;

// Shutdowns the engine
- (void)shutdown;

// Starts streaming the subframes to delegate method frameAvailable.
- (void)startFrameOutputWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe;

// Stops streaming
- (void)stopFrameOutput;

// Passes the camera frame into the engine. This should only be called after initializeWithCustomCameraUsingPreset.
- (void)enqueueCameraFrame:(CMSampleBufferRef)sampleBuffer mirror:(BOOL)mirror;

// Passes the audio sample into the engine. This is used for the video/audio recording and only if initialized with initializeWithCustomCameraUsingPreset.
- (void)enqueueAudioSample:(CMSampleBufferRef)sampleBuffer;


// Change a float parameter on a GameObject, the parameter variable contains parameter name, eg. blendshape name
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter floatValue:(float)value;
// Change a vector4 parameter on a GameObject, the parameter variable contains parameter name, eg. uniform name 
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter vectorValue:(Vector4)value;
// Change a vector3 parameter on a GameObject, the parameter variable contains parameter name, eg. transform name
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter vector3Value:(Vector3)value;
// Change a bool parameter on a GameObject, the parameter variable contains parameter name, eg. uniform name 
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter boolValue:(bool)value;
// Change an image parameter on a GameObject, the parameter variable contains parameter name, eg. uniform name
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter image:(UIImage*)image;

// Fire trigger for all animation controllers
- (void)fireTrigger:(NSString*)trigger;

// Change face detection sensitivity
- (void)setFaceDetectionSensitivity:(int)sensitivity;

// Display debuging stats on screen
- (void)showStats:(bool)enabled;

// Sets the internal SDK parameter
- (void)setParameterWithKey:(NSString*)key value:(NSString*)value;
@end

#import "ARView.h"
#import "CameraController.h"
