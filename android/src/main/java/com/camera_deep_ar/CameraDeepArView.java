package com.camera_deep_ar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

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
import com.camera_deep_ar.LoadImageHandlerThread;


public class CameraDeepArView implements PlatformView,
        SurfaceHolder.Callback, AREventListener,
        MethodChannel.MethodCallHandler,
        PluginRegistry.RequestPermissionsResultListener{

    private final Activity activity;
    private final Context context;
    private View view;
    private final MethodChannel methodChannel;
    private boolean disposed = false;
    private float mDist;
    private SurfaceView imgSurface;
    private String androidLicenceKey;

    private CameraGrabber cameraGrabber;
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

    private LoadImageHandlerThread imageGrabber;
    private ImageView offscreenView;
    private int RESULT_LOAD_IMG = 123;
    private int width = 720;
    private int height = 1280;

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
        offscreenView = mActivity.findViewById(R.id.surface);

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
        }

//        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//        photoPickerIntent.setType("image/*");
//        mActivity.startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);

        mContext
        mActivity.on(
                new registerActivityLifecycleCallbacks<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                    }
                });

        methodChannel.setMethodCallHandler(this);
        checkPermissions();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RESULT_LOAD_IMG) {
            Log.d("Image Intent", data.toString() + "");
        }
    }

    private  void checkPermissions(){
        initializeDeepAR();
        setupCamera();
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

                // AssetManager assetManager = context.getAssets();
                FlutterLoader loader = FlutterInjector.instance().flutterLoader();
                String pathJava = loader.getLookupKeyForAsset(String.valueOf(path));

                try {
                    deepAR.switchEffect(String.valueOf(mode), context.getAssets().open(pathJava));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            result.success("Custom Effect Changed");
        } else if ("changeParameterFloat".equals(methodCall.method)){
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object changeParameter = params.get("changeParameter");
                Object component = params.get("component");
                Object parameter = params.get("parameter");
                Object floatParam = params.get("floatValue");

                deepAR.changeParameterFloat(changeParameter.toString(), component.toString(), parameter.toString(), ((Double) floatParam).floatValue());
            }
        } else if ("changeImage".equals(methodCall.method)){
            Log.d("Damon - changeImage", "Being Involked");
            File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File imgsrc = new File(externalStoragePublicDirectory.toString()+ "/image.jpg");
            imageGrabber.loadBitmapFromGallery(Uri.fromFile(imgsrc));
            Log.d("Damon - changeImage", "Path is " + imgsrc.toString());
            Log.d("Damon - changeImage", "Ended Involked");
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

        imageGrabber = new LoadImageHandlerThread(new ContextWrapper(this.context));
        imageGrabber.start();
        imageGrabber.setImageReceiver(deepAR);
        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File imgsrc = new File(externalStoragePublicDirectory.toString()+ "/image31.jpg");
        imageGrabber.loadBitmapFromGallery(Uri.fromFile(imgsrc));
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
        deepAR.setRenderSurface(holder.getSurface(), imgSurface.getWidth(), imgSurface.getWidth());
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
//        deepAR.startVideoRecording("");
    }

    @Override
    public void videoRecordingFinished() {
//        deepAR.stopVideoRecording();
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
        Log.d("DAMON - initialized", "Function running");
        if (imageGrabber != null && deepAR != null) {
            imageGrabber.setImageReceiver(deepAR);
            // Load default image
            Message msg = Message.obtain(imageGrabber.getHandler());
            msg.what = LoadImageHandlerThread.LOAD_DEFAULT_IMAGE_TASK;
            msg.sendToTarget();
        }
        //jumpstart masks
        deepAR.switchEffect("mask", getFilterPath(masks.get(1)));
        refreshImage();

    }

    void refreshImage() {
        Message msg = Message.obtain(imageGrabber.getHandler());
        msg.what = LoadImageHandlerThread.REFRESH_IMAGE_TASK;
        msg.sendToTarget();
    }

    @Override
    public void faceVisibilityChanged(boolean b) {

    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {

    }

    @Override
    public void frameAvailable(Image frame) {
        if (frame != null) {
            Log.d("Frame", "Load Image from Assets Task, obj: " + frame.toString());
            final Image.Plane[] planes = frame.getPlanes();
            final Buffer buffer = planes[0].getBuffer().rewind();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            offscreenView.setImageBitmap(bitmap);
        }
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


// START OF MY CODE

//        this.imageView = new ImageView(mContext);
//        File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//        File imgsrc = new File(externalStoragePublicDirectory.toString()+ "/image.jpg");
//        Bitmap myBitmap = BitmapFactory.decodeFile(imgsrc.getAbsolutePath());
//        imageView.setImageBitmap(myBitmap);
//        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        // imageView.setVisibility(View.VISIBLE);
//        imageView.setMinimumWidth(100);
//        imageView.setMinimumHeight(100);
//        imageView.setBackgroundColor(Color.rgb(255, 255, 255));
//
//        Canvas canvas = new Canvas();
//        canvas.drawBitmap(myBitmap, 10, 10, null);
//        imgSurface.draw(canvas);
//        view.draw(canvas);

//Creating image buffer to send to DeepAR?

//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//        byte[] data = baos.toByteArray();
//        ByteBuffer buffer = ByteBuffer.wrap(data);
//        deepAR.receiveFrame(buffer, imageView.getMaxWidth(), imageView.getMaxHeight(), 1, false);

// END OF MY CODE