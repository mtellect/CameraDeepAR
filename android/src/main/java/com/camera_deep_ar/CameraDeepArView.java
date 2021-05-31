package com.camera_deep_ar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.deepar.ar.ARErrorType;
import ai.deepar.ar.AREventListener;
import ai.deepar.ar.CameraResolutionPreset;
import ai.deepar.ar.DeepAR;
import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.platform.PlatformView;

public class CameraDeepArView implements PlatformView,
        SurfaceHolder.Callback, AREventListener,
        MethodChannel.MethodCallHandler,
        PluginRegistry.RequestPermissionsResultListener{

    private final Activity activity;
    private final Context context;
    private final Registrar registrar;
    private View view;
    private final MethodChannel methodChannel;
    private boolean disposed = false;
    private float mDist;
    private SurfaceView imgSurface;
    private  String androidLicenceKey;


    private CameraGrabber cameraGrabber;

//    @Override
//    public void onFlutterViewAttached(@NonNull View flutterView) {
//
//    }
//
//    @Override
//    public void onFlutterViewDetached() {
//
//    }

    private int defaultCameraDevice = Camera.CameraInfo.CAMERA_FACING_FRONT;


    private int cameraDevice = defaultCameraDevice;
    private DeepAR deepAR;

    private int currentMask=0;
    private int currentEffect=0;
    private int currentFilter=0;

    private int screenOrientation;

    ArrayList<String> masks;
    ArrayList<String> effects;
    ArrayList<String> filters;

    private int activeFilterType = 0;
    private File videoFile;


    public CameraDeepArView(Activity mActivity, BinaryMessenger mBinaryMessenger, Context mContext, int id, Object args) {
        this.activity=mActivity;
        this.context=mContext;
       //view = View.inflate(context,R.layout.activity_camera, null);
        view = activity.getLayoutInflater().inflate(R.layout.activity_camera, null);

        methodChannel =
                new MethodChannel(mBinaryMessenger, "plugins.flutter.io/deep_ar_camera/" + id);

         imgSurface = view.findViewById(R.id.surface);
        imgSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deepAR.onClick();
            }
        });

        imgSurface.setFocusable(true);
        imgSurface.setFocusableInTouchMode(true);
        imgSurface.getHolder().addCallback(this);
        // Surface might already be initialized, so we force the call to onSurfaceChanged
        imgSurface.setVisibility(View.GONE);
        imgSurface.setVisibility(View.VISIBLE);

        if (args instanceof HashMap) {
            @SuppressWarnings({"unchecked"})
            Map<String, Object> params = (Map<String, Object>) args;
            Object licenceKey = params.get("androidLicenceKey");
            Object cameraEffect = params.get("cameraEffect");
            Object direction = params.get("direction");
            Object cameraMode = params.get("cameraMode");
            if(null!=licenceKey)androidLicenceKey=licenceKey.toString();
            if(null!=cameraEffect)activeFilterType=Integer.parseInt(String.valueOf(cameraEffect));
            if(null!=direction){
               int index=Integer.parseInt(String.valueOf(direction));
                defaultCameraDevice = index==0?Camera.CameraInfo.CAMERA_FACING_BACK:Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraDevice = defaultCameraDevice;
            }
           /* if(null!=cameraMode){
                int index=Integer.parseInt(String.valueOf(direction));
                defaultCameraDevice = index==0?Camera.CameraInfo.CAMERA_FACING_FRONT:Camera.CameraInfo.CAMERA_FACING_FRONT;
                cameraDevice = defaultCameraDevice;
            }*/
  }

        methodChannel.setMethodCallHandler(this);
//        activity.addRequestPermissionsResultListener(this);
        checkPermissions();
    }

    private  void checkPermissions(){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{ Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO },
                    1);
        } else {
            // Permission has already been granted
            initializeDeepAR();
            setupCamera();
        }
    }

   private void initializeDeepAR(){
       deepAR = new DeepAR(activity);
       deepAR.setLicenseKey(androidLicenceKey);
       deepAR.initialize(activity, this);
       initializeFilters();
    }

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {

        //File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/DeepAR_" + now + ".jpg");


        if ("isCameraReady".equals(methodCall.method)) {
            Map<String, Object> argument = new HashMap<>();
            argument.put("isReady",true);
            methodChannel.invokeMethod("onCameraReady",argument);
            result.success("Android is ready");
        }
        else  if ("setCameraMode".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object direction = params.get("cameraMode");
                if(null!=direction) activeFilterType = Integer.parseInt(String.valueOf(direction));
            }
            result.success("Mask Changed");
        }
        else  if ("switchCameraDirection".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object direction = params.get("direction");
                if(null!=direction){
                    int index=Integer.parseInt(String.valueOf(direction));
                    defaultCameraDevice = index==0?Camera.CameraInfo.CAMERA_FACING_BACK:Camera.CameraInfo.CAMERA_FACING_FRONT;
                    cameraDevice = defaultCameraDevice;
                    cameraGrabber.changeCameraDevice(cameraDevice);
                }
                 }
            result.success("Mask Changed");
        }

        else  if ("zoomTo".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object index = params.get("zoom");
                int zoom=Integer.parseInt(String.valueOf(index));
                Camera.Parameters camParams = cameraGrabber.getCamera().getParameters();
                cameraGrabber.getCamera().cancelAutoFocus();
                camParams.setZoom(zoom);
                cameraGrabber.getCamera().setParameters(camParams);
                 }
            result.success("ZoomTo Changed");
        }

        else  if ("changeMask".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object mask = params.get("mask");
                currentMask=Integer.parseInt(String.valueOf(mask));
                deepAR.switchEffect("mask", getFilterPath(masks.get(currentMask)));
            }
            result.success("Mask Changed");
        }else  if ("changeEffect".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object effect = params.get("effect");
                currentEffect=Integer.parseInt(String.valueOf(effect));
                deepAR.switchEffect("effect", getFilterPath(effects.get(currentEffect)));
            }
            result.success("Effect Changed");
        }
        else  if ("changeFilter".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object filter = params.get("filter");
                currentFilter=Integer.parseInt(String.valueOf(filter));
                deepAR.switchEffect("filter", getFilterPath(filters.get(currentFilter)));
            }
            result.success("Filter Changed");
        }
        else  if ("startVideoRecording".equals(methodCall.method)) {
            CharSequence now = DateFormat.format("yyyy_MM_dd_hh_mm_ss", new Date());
             videoFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/DeepAR_" + now + ".mp4");
            deepAR.startVideoRecording(videoFile.getPath());
            result.success("Video Recording Started");
        }
        else  if ("stopVideoRecording".equals(methodCall.method)) {
              deepAR.stopVideoRecording();
            result.success("Video Recording Stopped");
        }
        else  if ("snapPhoto".equals(methodCall.method)) {
              deepAR.takeScreenshot();
            result.success("Photo Snapped");
        }
        else  if ("dispose".equals(methodCall.method)) {
            disposed = true;
            methodChannel.setMethodCallHandler(null);
            deepAR.setAREventListener(null);
            deepAR.release();
            deepAR = null;
            result.success("Disposed");
        }else  if ("switchEffect".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object mode = params.get("mode");
                Object path = params.get("path");

                 FlutterLoader loader = FlutterInjector.instance().flutterLoader();
                 String pathJava = loader.getLookupKeyForAsset(String.valueOf(path));
                 //deepAR.changeParameterFloat("","","",0.0);
                 deepAR.switchEffect(String.valueOf(mode), pathJava);

//                AssetManager assetManager = context.getAssets();
//                String key = this.registrar.lookupKeyForAsset(String.valueOf(path));
//                //AssetFileDescriptor fd = assetManager.openFd(key);
//                Log.d("CAMERA_DEEPAR",key);
//                deepAR.switchEffect(key, pathJava);
            }
            result.success("Custom Effect Changed");
        } else if ("changeParameterFloat".equals(methodCall.method)){
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object changeParameter = params.get("changeParameter");
                Object component = params.get("component");
                Object parameter = params.get("parameter");
                Object floatValue = params.get("floatValue");

                deepAR.changeParameterFloat(changeParameter, component, parameter, (float)floatValue);
            }
        }

    }

    private void initializeFilters() {
        masks = new ArrayList<>();
        masks.add("none");
        masks.add("aviators");
        masks.add("bigmouth");
        masks.add("dalmatian");
        masks.add("flowers");
        masks.add("koala");
        masks.add("lion");
        masks.add("smallface");
        masks.add("teddycigar");
        masks.add("kanye");
        masks.add("tripleface");
        masks.add("sleepingmask");
        masks.add("fatify");
        masks.add("obama");
        masks.add("mudmask");
        masks.add("pug");
        masks.add("slash");
        masks.add("twistedface");
        masks.add("grumpycat");

        effects = new ArrayList<>();
        effects.add("none");
        effects.add("fire");
        effects.add("rain");
        effects.add("heart");
        effects.add("blizzard");

        filters = new ArrayList<>();
        filters.add("none");
        filters.add("filmcolorperfection");
        filters.add("tv80");
        filters.add("drawingmanga");
        filters.add("sepia");
        filters.add("bleachbypass");
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
            switch(rotation) {
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
            switch(rotation) {
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

    private void setupCamera() {
        cameraGrabber = new CameraGrabber(cameraDevice);
        screenOrientation = getScreenOrientation();

        switch (screenOrientation) {
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

        // Available 1080p, 720p and 480p resolutions
        cameraGrabber.setResolutionPreset(CameraResolutionPreset.P1280x720);

        //final Activity context = this;
        cameraGrabber.initCamera(new CameraGrabberListener() {
            @Override
            public void onCameraInitialized() {
                cameraGrabber.setFrameReceiver(deepAR);
                cameraGrabber.startPreview();
            }

            @Override
            public void onCameraError(String errorMsg) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
         imgSurface.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Get the pointer ID
                Camera.Parameters params = cameraGrabber.getCamera().getParameters();
                int action = event.getAction();
                if (event.getPointerCount() > 1) {
                    // handle multi-touch events
//                    if (action == MotionEvent.ACTION_POINTER_DOWN) {
//                        mDist = getFingerSpacing(event);
//                    } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
//                        cameraGrabber.getCamera().cancelAutoFocus();
//                        handleZoom(event, params);
//                    }
                } else {
                    // handle single touch events
                    if (action == MotionEvent.ACTION_UP) {
                        handleFocus(event);
                    }
                }
                return true;
            }
        });
    }



    private String getFilterPath(String filterName) {
        if (filterName.equals("none")) {
            return null;
        }
        return "file:///android_asset/" + filterName;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If we are using on screen rendering we have to set surface view where DeepAR will render
        deepAR.setRenderSurface(holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (deepAR != null) {
            deepAR.setRenderSurface(null, 0, 0);
        }
    }


    @Override
    public View getView() {
        return view;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        methodChannel.setMethodCallHandler(null);
        deepAR.setAREventListener(null);
        deepAR.release();
        deepAR = null;

    }

    @Override
    public void screenshotTaken(Bitmap bitmap) {
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
            argument.put("path",imageFile.toString());
            methodChannel.invokeMethod("onSnapPhotoCompleted",argument);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void videoRecordingStarted() {
//deepAR.startVideoRecording("");
    }

    @Override
    public void videoRecordingFinished() {
//deepAR.stopVideoRecording();
        Map<String, Object> argument = new HashMap<>();
        argument.put("path",videoFile.toString());
        methodChannel.invokeMethod("onVideoRecordingComplete",argument);
    }

    @Override
    public void videoRecordingFailed() {

    }

    @Override
    public void videoRecordingPrepared() {

    }

    @Override
    public void shutdownFinished() {

    }

    @Override
    public void initialized() {

    }

    @Override
    public void faceVisibilityChanged(boolean b) {

    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {

    }

    @Override
    public void frameAvailable(Image image) {

    }

    @Override
    public void error(ARErrorType arErrorType, String s) {

    }

    @Override
    public void effectSwitched(String s) {

    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);

        if (Math.abs(newDist - mDist) < 2) return;

        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        cameraGrabber.getCamera().setParameters(params);
    }

    private float getFingerSpacing(MotionEvent event) {
        // ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    private void handleFocus(MotionEvent event) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position

        int xxw = imgSurface.getHeight();
        int xxh = imgSurface.getWidth();
        float x = event.getY(pointerIndex);
        float y = xxh - event.getX(pointerIndex);

        //cancel previous actions
        cameraGrabber.getCamera().cancelAutoFocus();

        Rect touchRect = new Rect(
                (int) (x - 100),
                (int) (y - 100),
                (int) (x + 100),
                (int) (y + 100));

        int aboutToBeLeft = touchRect.left;
        int aboutToBeTop = touchRect.top;
        int aboutToBeRight = touchRect.right;
        int aboutToBeBottom = touchRect.bottom;

        if (aboutToBeLeft < 0) {
            aboutToBeLeft = 0;
            aboutToBeRight = 200;
        }
        if (aboutToBeTop < 0) {
            aboutToBeTop = 0;
            aboutToBeBottom = 200;
        }
        if (aboutToBeRight > xxw) {
            aboutToBeRight = xxw;
            aboutToBeLeft = xxw - 200;
        }
        if (aboutToBeBottom > xxh) {
            aboutToBeBottom = xxh;
            aboutToBeTop = xxh - 200;
        }

        aboutToBeLeft = aboutToBeLeft * 2000 / xxw - 1000;
        aboutToBeTop = aboutToBeTop * 2000 / xxh - 1000;
        aboutToBeRight = aboutToBeRight * 2000 / xxw - 1000;
        aboutToBeBottom = aboutToBeBottom * 2000 / xxh - 1000;

        Rect focusRect = new Rect(
                aboutToBeLeft,
                aboutToBeTop,
                aboutToBeRight,
                aboutToBeBottom);

        Camera.Parameters parameters = null;

        try {
            parameters = cameraGrabber.getCamera().getParameters();
        } catch (Exception e) {
            Log.e("Error", "Error getting parameter:" + e);
        }

        // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
        if (parameters != null) {
            List<Camera.Area> mylist2 = new ArrayList<>();

            mylist2.add(new Camera.Area(focusRect, 1000));

            List<String> supportedFocusMode = parameters.getSupportedFocusModes();
            String focusMode = Camera.Parameters.FOCUS_MODE_AUTO;
            if (!supportedFocusMode.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                if (supportedFocusMode.size() > 0) {
                    focusMode = supportedFocusMode.get(0);
                }
            }
            parameters.setFocusMode(focusMode);
            if (focusMode.equals(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusAreas(mylist2);

            try {
                cameraGrabber.getCamera().setParameters(parameters);
                cameraGrabber.getCamera().autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                    }
                });
            } catch (Exception e) {
                Log.e("error", "lalalalalala=> " + e);
            }
        }
    }


    @Override
    public boolean onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
                initializeDeepAR();
            }
        }
        return false;
    }



}
