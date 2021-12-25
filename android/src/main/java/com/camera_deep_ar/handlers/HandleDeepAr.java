package com.camera_deep_ar.handlers;

import static com.camera_deep_ar.CameraDeepArView.TAG_CAMERA;
import static com.camera_deep_ar.CameraDeepArView.TAG_IMAGE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.camera_deep_ar.CameraDeepArView;
import com.camera_deep_ar.CameraGrabber;
import com.camera_deep_ar.CameraGrabberListener;
import com.camera_deep_ar.ImageGrabber;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;

public class HandleDeepAr {
    private static final String TAG = "HandleDeepAr";

    Handler handler;
    FrameLayout frameView;

    final CameraDeepArView pluginView;
    final Context context;
    final Activity activity;
    DeepAR deepAR;
    final String licenceKey;
    ImageGrabber imageGrabber;
    CameraGrabber cameraGrabber;
    int displayMode;
    private int cameraDevice = Camera.CameraInfo.CAMERA_FACING_FRONT;
    public int width = 720;
    public int height = 1280;

    File videoFile;

    public HandleDeepAr(CameraDeepArView pluginView, Context context, Activity activity, String licenceKey, FrameLayout frameView) {
        this.pluginView = pluginView;
        this.context = context;
        this.activity = activity;
        this.licenceKey = licenceKey;
        this.frameView = frameView;
        deepAR = new DeepAR(activity);
        deepAR.setLicenseKey(licenceKey);
        deepAR.initialize(activity, new HandleDeepArEvents(this));
        deepAR.setFaceDetectionSensitivity(1);

        handler = pluginView.handler;
        init();
    }

    public void setupImage() {
        imageGrabber= new ImageGrabber();
        imageGrabber.setContext(new ContextWrapper(context));
        imageGrabber.setFrameReceiver(deepAR);
//        SystemClock.sleep(500);
//        imageGrabber.loadDefaultBitmap();
//        getImageView().setImageBitmap(imageGrabber.lastImage);
    }

    public void setupCamera() {
        cameraGrabber = new CameraGrabber(cameraDevice);
        setOrientation(getScreenOrientation());
        // Available 1080p, 720p and 480p resolutions
        cameraGrabber.setResolutionPreset(CameraResolutionPreset.P1280x720);
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
            }

            @Override
            public void onCameraError(String errorMsg) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                Log.d("Setup", "Camera Erroring on setup");
                builder.setTitle("Camera error");
                builder.setMessage(errorMsg);
                builder.setCancelable(true);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

//        imgSurface.setOnTouchListener((v, event) -> {
//        ((SurfaceView) getViewAtTag(TAG_CAMERA)).setOnTouchListener((v, event) -> {
//            // Get the pointer ID
//            Camera.Parameters params = cameraGrabber.getCamera().getParameters();
//            int action = event.getAction();
//            if (event.getPointerCount() > 1) {
//
//            } else {
//                // handle single touch events
//                if (action == MotionEvent.ACTION_UP) {
//                    CameraUtils.handleFocus(event,cameraGrabber, ((SurfaceView) getViewAtTag(TAG_CAMERA)));
//                }
//            }
//            return true;
//        });
    }

    private void setOrientation(int orientation) {
        switch (orientation) {
            case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                cameraGrabber.setScreenOrientation(90);
                break;
            case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                cameraGrabber.setScreenOrientation(270);
                break;
            default:
                cameraGrabber.setScreenOrientation(0);
                break;
        }
    }

    private int getScreenOrientation() {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }




    public void changeParameterTexture(String gameObject, String component, String parameter, String image) {
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(image)); //, options  ////R.drawable.texture
            deepAR.changeParameterTexture(gameObject, component, parameter, bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeImage(String filePath) {
        try {
            if(null==imageGrabber)setupImage();
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            handler.post(() -> {
                imageGrabber.loadBitmapFromGallery(bitmap);
//                imageGrabber.refreshBitmap();
            });

        } catch (Exception e) {
            Log.e(TAG, "changeImage", e);
            e.printStackTrace();
        }
    }

    public void changeParameterFloat(String changeParameter, String component, String parameter, float floatParam) {
        deepAR.changeParameterFloat(changeParameter, component, parameter, floatParam);
    }


    public void dispose() {
//        disposed = true;
//        methodChannel.setMethodCallHandler(null);
        deepAR.setAREventListener(null);
        deepAR.release();
        deepAR = null;
    }

    public void switchEffect(String mode, String pathJava) {
        handler.post(() -> {
            if (pathJava.contains("none")) {
                deepAR.switchEffect(mode, (String) null);
            } else {
                //deepAR.switchEffect(mode, pathJava.contains("none")?null:pathJava);
                try (FileInputStream inputStream = new FileInputStream(pathJava)) {
                    deepAR.switchEffect(mode, inputStream);
                } catch (NullPointerException | IOException e) {
                    Log.e(TAG, "Could not change mode", e);
                    e.printStackTrace();
                }
//        if(displayMode==TAG_IMAGE) imageGrabber.refreshBitmap();
            }

        });
    }

    private FileInputStream getAsFileInputStream(String path) {
        try {
            FileInputStream inputStream = new FileInputStream(path);
            return inputStream;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Could not get mask as file input stream", e);
            e.printStackTrace();
            return null;
        }
    }

    ;

    public void zoomTo(int zoom) {
        Camera.Parameters camParams = cameraGrabber.getCamera().getParameters();
        cameraGrabber.getCamera().cancelAutoFocus();
        camParams.setZoom(zoom);
        cameraGrabber.getCamera().setParameters(camParams);
    }

    public void startVideoRecording() {
        CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
        videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/DeepAR_" + now + ".mp4");
        deepAR.startVideoRecording(videoFile.getPath());
    }

    public void setCameraMode(int mode) {

    }

    public void setDisplayMode(int mode) {
        this.displayMode = mode;
//        setViewVisibility(mode);

        
        handler.post(() -> {
            deepAR.changeLiveMode(mode == 0);
            deepAR.setOffscreenRendering(width,height);
            if (mode == 0 ) {
                getCameraView().setVisibility(View.VISIBLE);
                getImageView().setVisibility(View.GONE);
                getCameraView().getHolder().addCallback(new HandleDeepArSurface(this));
                getCameraView().setOnClickListener(view -> onClick());
                getCameraView().setFocusable(true);
                getCameraView().setFocusableInTouchMode(true);
//                imageGrabber=null;
                setupCamera();  
            }
            if (mode == 1) {
                getCameraView().getHolder().removeCallback(new HandleDeepArSurface(this));
                getCameraView().setFocusable(false);
                getCameraView().setFocusableInTouchMode(false);
                getCameraView().setVisibility(View.GONE);
                getImageView().setVisibility(View.VISIBLE);
                setupImage();
                
            }
        });
        Log.d(TAG, "DisplayMode " + (mode == 0 ? "Camera!" : "Image!"));
    }

    public void changeCameraDevice(int direction) {
        cameraDevice = direction == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
        cameraGrabber.changeCameraDevice(cameraDevice);
    }

    public void stopVideoRecording() {
        handler.post(() -> deepAR.stopVideoRecording());
    }

    public void snapPhoto() {
        handler.post(() -> deepAR.takeScreenshot());
    }

    public void onClick() {
        deepAR.onClick();
    }

    public void onVideoRecordingFinished() {
        Map<String, Object> argument = new HashMap<>();
        argument.put("path", videoFile.toString());
        pluginView.sendResponse("onVideoRecordingComplete", argument);
    }

    public void onPhotoTaken(Bitmap bitmap) {
        handler.post(() -> {
            CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
            try {
                File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DeepAR_" + now + ".jpg");
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
                MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, null);
                //Toast.makeText(context, "Screenshot saved", Toast.LENGTH_SHORT).show();
                Map<String, Object> argument = new HashMap<>();
                argument.put("path", imageFile.toString());
                pluginView.sendResponse("onSnapPhotoCompleted", argument);
            } catch (Throwable e) {
                Log.e(TAG, "onPhotoTaken Error", e);
                e.printStackTrace();
            }
        });
    }

    public void setImageFrame(Image image) {
        handler.post(() -> {
           try{
               Log.d("Frame", "Load Image from Assets Task, obj: ");
//
//               ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//               byte[] bytes = new byte[buffer.remaining()];
//               buffer.get(bytes);
//               Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
               
               final Image.Plane[] planes = image.getPlanes();
               Log.d("Frame", "Load Image from Assets Task, obj: " + planes.length);
               final Buffer buffer = planes[0].getBuffer().rewind();
               int pixelStride = planes[0].getPixelStride();
               int rowStride = planes[0].getRowStride();
               int rowPadding = rowStride - pixelStride * width;
               Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
               bitmap.copyPixelsFromBuffer(buffer);

               Log.d("Frame", "Upto here" + buffer.toString());
               getImageView().setImageBitmap(bitmap);
               
           }catch (Exception e){
               Log.e(TAG,"FrameError",e);
           }
        });
    }


    public void onPause() {
        if (null != deepAR) deepAR.setPaused(true);
        if (null != cameraGrabber) {
            cameraGrabber.stopPreview();
            cameraGrabber.releaseCamera();
        }
        Log.d(TAG, "onPause");
    }

    public void onResume() {
        if (null != deepAR) deepAR.setPaused(false);
        init();
        Log.d(TAG, "onResume");
    }

    public void onDestroy() {
        dispose();
        Log.d(TAG, "onDestroy");
    }

    public void init() {

        handler.post(() -> {
//             setupCamera();
//             setupImage();
//            if (displayMode == TAG_CAMERA) setupCamera();
//            if (displayMode == TAG_IMAGE) setupImage();
        });
        Log.d(TAG, "init");
    }

    public View getViewAtIndex(int tag) {
        return frameView.getChildAt(tag);
    }
    
    
    ImageView getImageView(){
        return  (ImageView) getViewAtIndex(TAG_IMAGE);
    }

    
    SurfaceView getCameraView(){
        return  (SurfaceView) getViewAtIndex(TAG_CAMERA);
    }

}
