//
//  DeepAR.h
//  ar
//
//  Created by Kod Biro on 23/07/2020.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

typedef struct {
    BOOL detected;
    float translation[3];
    float rotation[3];
    float poseMatrix[16];
    float landmarks[68*3];
    float landmarks2d[68*3];
    float faceRect[4];
    float emotions[5]; // 0=neutral, 1=happiness, 2=surprise, 3=sadness, 4=anger
    float actionUnits[63];
    int numberOfActionUnits;
} FaceData;

typedef struct {
    FaceData faceData[4];
} MultiFaceData;

typedef struct {
    float x;
    float y;
    float z;
    float w;
} Vector4;

typedef struct {
    float x;
    float y;
    float z;
} Vector3;

typedef enum {
    DEEPAR_ERROR_TYPE_DEBUG,
    DEEPAR_ERROR_TYPE_INFO,
    DEEPAR_ERROR_TYPE_WARNING,
    DEEPAR_ERROR_TYPE_ERROR
} ARErrorType;

@protocol DeepARDelegate <NSObject>

@optional

// Called when screenshot is taken
- (void)didTakeScreenshot:(UIImage*)screenshot;

// Called when the engine initialization is complete.
- (void)didInitialize;

// Called when the face appears or disappears.
- (void)faceVisiblityDidChange:(BOOL)faceVisible;

// A new processed frame is available. Make sure to call startCaptureWithOutputWidth on DeepAR (or startFrameOutputWithOutputWidth if you use ARView) if you want this method to be called whenever a new frame is ready.
- (void)frameAvailable:(CMSampleBufferRef)sampleBuffer;

// Called on each frame where at least one face data is detected.
- (void)faceTracked:(MultiFaceData)faceData;

// Whenever a face is detected or lost from the scene this method is called. facesVisible represents the number of currently detected faces in the frame.
- (void)numberOfFacesVisibleChanged:(NSInteger)facesVisible;

// DeepAR has successfully shut down after the method shutdown call.
- (void)didFinishShutdown;

// DeepAR has the ability to track arbitrary images in the scene, more about it read here: https://help.deepar.ai/en/articles/3493377-image-tracking. This method notifies when tracked image visibility changes. gameObjectName is the name of the game object/node in the filter file to which the image is associated.
- (void)imageVisibilityChanged:(NSString*)gameObjectName imageVisible:(BOOL)imageVisible;

// Called when the switchEffect method has successfully switched given effect on a given slot.
- (void)didSwitchEffect:(NSString*)slot;

// Called when the conditions have been met for the animation to transition to the next state (e.g. mouth open, emotion detected etc.)
- (void)animationTransitionedToState:(NSString*)state;

// Called when DeepAR has started video recording (after calling startVideoRecording method).
- (void)didStartVideoRecording;

// Called when the video recording is finished and video file is saved.
- (void)didFinishVideoRecording:(NSString*)videoFilePath;

// Called if there is error encountered while recording video
- (void)recordingFailedWithError:(NSError*)error;

- (void)onErrorWithCode:(ARErrorType)code error:(NSString*)error;

@end

#define ARViewDelegate DeepARDelegate

@interface DeepAR : NSObject

// The object which implements DeepARDelegate protocol to listen for async events coming from DeepAR.
@property (nonatomic, weak) id<DeepARDelegate> delegate;

// Indicates if computer vision components have been initialized during the initialization process.
@property (nonatomic, readonly) BOOL visionInitialized;

// Indicates if DeepAR rendering components have been initialized during the initialization process.
@property (nonatomic, readonly) BOOL renderingInitialized;

// Indicates if at least one face is detected in the current frame.
@property (nonatomic, readonly) BOOL faceVisible;

// Rendering resolution DeepAR has been initialized with
@property (nonatomic, readonly) CGSize renderingResolution;

@property (nonatomic, strong) NSDictionary* audioCompressionSettings;

// Set the license key for your app. The license key is generated on DeepAR Developer portal. Here are steps how to generate license key:
// Log in/Sign up to developer.deepar.ai
// Create a new project and in that project create an iOS app
// In the create app dialog enter your app name and bundle id that your app is using. Bundle id must match the one you are using in your app, otherwise, the license check will fail. Read more about iOS bundle id https://cocoacasts.com/what-are-app-ids-and-bundle-identifiers/.
// Copy the newly generated license key as a parameter in this method
// You must call this method before you call the initialize.
- (void)setLicenseKey:(NSString*)key;

// Starts the engine initialization where the DeepAR will initialize in rendering mode. This means users can use the rendering functionality of DeepAR, in addition to computer vision features, to load effects in the scene, render the frames in the UI, etc. width and height define rendering resolutions and window parameter is a CAEAGLLayer of an existing view where the DeepAR will render the processed frames.
- (void)initializeWithWidth:(NSInteger)width height:(NSInteger)height window:(CAEAGLLayer*)window;

// Starts the engine initialization where the DeepAR will initialize in vision only mode, meaning it will process frames in terms of detecting faces and their properties that are available in FaceData object. No rendering will be available in this mode.
- (void)initialize;

// Starts the engine initialization for rendering into offscreen texture.
- (void)initializeOffscreenWithWidth:(NSInteger)width height:(NSInteger)height;

// Starts the engine initialization for rendering into created view.
- (UIView*)createARViewWithFrame:(CGRect)frame;

// Indicates if DeepAR has been initialized in vision only mode or not.
- (BOOL)isVisionOnly;

// Changes the rendering/output resolution. Can be called any time.
- (void)setRenderingResolutionWithWidth:(NSInteger)width height:(NSInteger)height;


// Shuts down the DeepAR engine. Reinitialization of a new DeepAR instance which has not been properly shut down can cause crashes and memory leaks.
- (void)shutdown;

// Creates a new view into which engine will render.
- (UIView*)switchToRenderingToViewWithFrame:(CGRect)frame;

// Switches to offscreen rendering.
- (void)switchToRenderingOffscreenWithWidth:(NSInteger)width height:(NSInteger)height;

// Change if should render frame by frame or render continuously.
// liveMode - YES for render continuously; NO for render frame by frame.
- (void)changeLiveMode:(BOOL)liveMode;


// Resumes the rendering if it was previously paused, otherwise doesn't do anything.
- (void)resume;

// Pauses the rendering. This method will not release any resources and should be used only for temporary pause (e.g. user goes to the next screen). Use the shutdown method to stop the engine and to release the resources.
- (void)pause;

// Feed frame to DeepAR for processing. The result can be received in the frameAvailable delegate method. imageBuffer is the input image data that needs processing. mirror indicates whether the image should be flipped vertically before processing (front/back camera).
- (void)processFrame:(CVPixelBufferRef)imageBuffer mirror:(BOOL)mirror;


// Feed frame to DeepAR for processing. Outputs the result in the outputBuffer parameter
// Requires frame capturing to be started!
- (void)processFrameAndReturn:(CVPixelBufferRef)imageBuffer outputBuffer:(CVPixelBufferRef)outputBuffer mirror:(BOOL)mirror;

// Same functionality as processFrame with CMSampleBufferRef  as an input type for frame data which is more suitable if using camera frames via AVFoundation. It is advised to use this method instead of processFrame when using camera frames as input because it will use native textures to fetch frames from the iPhone camera more efficiently.
// mirror indicates whether the image should be flipped vertically before processing (front/back camera).
- (void)enqueueCameraFrame:(CMSampleBufferRef)sampleBuffer mirror:(BOOL)mirror;

// Passes an audio sample to the DeepAR engine. Used in video recording when user wants to record audio too. Audio samples will be processed only if the startVideoRecording method has been called with recordAudio parameter set to true.
- (void)enqueueAudioSample:(CMSampleBufferRef)sampleBuffer;

// Starts video recording of the ARView with given outputWidth x outputHeight resolution.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight;

//Starts video recording of the ARView with given outputWidth x outputHeight resolution. The subframe parameter defines the sub rectangle of the  ARView that you want to record in normalized coordinates (0.0 - 1.0).
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe;

//Starts video recording of the ARView with given outputWidth x outputHeight resolution. The subframe parameter defines the sub rectangle of the  ARView that you want to record in normalized coordinates (0.0 - 1.0). videoCompressionProperties is an NSDictionary used as the value for the key AVVideoCompressionPropertiesKey.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe videoCompressionProperties:(NSDictionary*)videoCompressionProperties;

//Same as the previous method, additionally indicates that you want to record audio too. If recordAudio parameter is set to true the recording will wait until you call enqueueAudioSample on ARView. When DeepAR is ready to receive audio samples it will publish NSNotification with key deepar_start_audio. You can subscribe to this notification and start feeding audio samples once you receive it. If you use provided CameraController this is handled for you by default.
- (void)startVideoRecordingWithOutputWidth:(int)outputWidth outputHeight:(int)outputHeight subframe:(CGRect)subframe videoCompressionProperties:(NSDictionary*)videoCompressionProperties recordAudio:(BOOL)recordAudio;


// Load a DeepAR Studio file as an effect/filter in the scene. path is a string path to a file located in the app bundle or anywhere in the filesystem where the app has access to. For example, one can download the filters from online locations and save them in the Documents directory. Value nil for the path param will remove the effect from the scene.
// The slot specifies a namespace for the effect in the scene. In each slot, there can be only one effect. If you load another effect in the same slot the previous one will be removed and replaced with a new effect.
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path;

//Same as the previous method with added face parameter indication on which face to apply the effect. DeepAR offers tracking up to 4 faces, so valid values for this parameter are 0, 1, 2, and 3. For example, if you call this method with face value 2, the effect will be only applied to the third detected face in the scene. If you want to set an effect on a different face make sure to also use a different value for the slot parameter to avoid removing the previously added effect.
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path face:(NSInteger)face;

//Same as the override with face parameter, but with added targetGameObject which indicates a node in the currently loaded scene/effect into which the new effect will be loaded. By default, effects are loaded in the root node object.
- (void)switchEffectWithSlot:(NSString*)slot path:(NSString*)path face:(NSInteger)face targetGameObject:(NSString*)targetGameObject;

// Produces a snapshot of the current screen preview. Resolution is equal to the dimension with which the DeepAR has been initialized. The DeepARDelegate  method didTakeScreenshot will be called upon successful screenshot capture is finished with a path where the image has been temporarily stored. 
- (void)takeScreenshot;

// Finishes the video recording. Delegate method didFinishVideoRecording will be called when the recording is done with the temporary path of the recorded video.
- (void)finishVideoRecording;

// Pauses video recording if it has been started beforehand.
- (void)pauseVideoRecording;

// Resumes video recording after it has been paused with pauseVideoRecording.
- (void)resumeVideoRecording;

// Enables or disables audio pitch processing for video recording.
- (void)enableAudioProcessing:(BOOL)enabled;

// Sets the pitch change amount. Negative values will make the recorded audio lower in pitch and positive values will make it higher in pitch. Must call enableAudioProcessing to enable the pitch processing beforehand.
- (void)setAudioProcessingSemitone:(float)sts;

// By default DeepARDelegate will not call frameAvailable method on each new processed frame to save on processing time and resources. If we want the processed frames to be available in frameAvailable method of DeepARDelegate we need to call this method first on ARView. outputHeight and outputWidth define the size of the processed frames and subframe defines a subrectangle of DeepAR rendering which will be outputted. This means that the output frame in frameAvailable does not need to be the same size and/or position as the one rendered.
- (void)startCaptureWithOutputWidth:(NSInteger)outputWidth outputHeight:(NSInteger)outputHeight subframe:(CGRect)subframe;

// Stops outputting frames to frameAvailable.
- (void)stopCapture;

// Fire trigger for all animation controllers
- (void)fireTrigger:(NSString*)trigger;

- (void)touchEvent;

// Display debuging stats on screen (if rendering is on).
- (void)showStats:(BOOL) enabled;

// This method allows the user to change face detection sensitivity. The sensitivity parameter can range from 0 to 3, where 0 is the fastest but might not recognize smaller (further away) faces, and 3 is the slowest but will find smaller faces. By default, this parameter is set to 1.
- (void)setFaceDetectionSensitivity:(NSInteger)sensitivity;

// Change a float value on a GameObject given by value parameter. The parameter  is the name of the parameter you want to change, e.g. scalar uniform on a shader or blendshape. For more details about changeParameter API read our article https://help.deepar.ai/en/articles/3732006-changing-filter-parameters-from-code.
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter floatValue:(CGFloat)value;
// Change a vector of 4 elements on a GameObject given by value parameter. The parameter  is the name of the parameter you want to change, e.g. an uniform on a shader or blendshape. For more details about changeParameter API read our article https://help.deepar.ai/en/articles/3732006-changing-filter-parameters-from-code.
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter vectorValue:(Vector4)value;
// Change a vector of 3 elements on a GameObject given by value parameter. The parameter  is the name of the parameter you want to change, e.g. an uniform on a shader or blendshape. For more details about changeParameter API read our article https://help.deepar.ai/en/articles/3732006-changing-filter-parameters-from-code.
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter vector3Value:(Vector3)value;
// Change a boolean value on a GameObject given by value parameter. The parameter is the name of the parameter you want to change. Most common use case for this override is to set the enabled property of a game object. For more details about changeParameter API read our article https://help.deepar.ai/en/articles/3732006-changing-filter-parameters-from-code.
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter boolValue:(BOOL)value;
// Change an image parameter on a game object. The parameter is the name of the parameter you want to change. Most common use case for this override is to change the texture of a shader on a given game object. For more details about changeParameter API read our article https://help.deepar.ai/en/articles/3732006-changing-filter-parameters-from-code.
- (void)changeParameter:(NSString*)gameObject component:(NSString*)component parameter:(NSString*)parameter image:(UIImage*)image;


- (void)setParameterWithKey:(NSString*)key value:(NSString*)value;

@end

#import "ARView.h"
#import "CameraController.h"
