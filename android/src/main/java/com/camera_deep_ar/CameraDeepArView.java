package com.camera_deep_ar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;

import com.camera_deep_ar.handlers.HandleDeepAr;
import com.camera_deep_ar.handlers.HandleDeepArSurface;
import com.camera_deep_ar.handlers.HandlePluginMethod;

import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.lifecycle.FlutterLifecycleAdapter;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.platform.PlatformView;


public class CameraDeepArView implements PlatformView,
        LifecycleObserver, EventChannel.StreamHandler {

    private static final String TAG = "CameraDeepArView";
    public static final int TAG_CAMERA = 0;
    public static final int TAG_IMAGE = 1;

    public Handler handler= new Handler(Looper.getMainLooper());
    public HandleDeepAr handleDeepAr;
    private final ActivityPluginBinding activityBinding;
    private final FlutterPlugin.FlutterPluginBinding pluginBinding;

    private final Activity activity;
    public final Context context;
    public final MethodChannel methodChannel;
    public final EventChannel eventChannel;
    EventChannel.EventSink eventSink;
    private final boolean disposed = false;
    private String androidLicenceKey;
    private int displayMode=0;

    private int defaultCameraDevice = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int cameraDevice = defaultCameraDevice;



    public int activeFilterType = 0;

    private final int RESULT_LOAD_IMG = 123;
    private final int width = 720;
    private final int height = 1280;

    FrameLayout frameView;


    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    public void onStartListener(LifecycleOwner owner,Lifecycle.Event event){
        Log.e(TAG,"onStartListener "+event.name());

        if(event == Lifecycle.Event.ON_PAUSE){
            if(null!=handleDeepAr)  handleDeepAr.onPause();
        }

        if(event == Lifecycle.Event.ON_RESUME){
          if(null!=handleDeepAr)  handleDeepAr.onResume();
        }

        if(event == Lifecycle.Event.ON_DESTROY){
            if(null!=handleDeepAr)   handleDeepAr.onDestroy();
        }
    }



    public CameraDeepArView(ActivityPluginBinding activityBinding, FlutterPlugin.FlutterPluginBinding pluginBinding, Context viewContext, int id, Object args) {
        FlutterLifecycleAdapter.getActivityLifecycle(activityBinding).addObserver(this);
        this.activityBinding=activityBinding;
        this.pluginBinding=pluginBinding;
        this.activity=activityBinding.getActivity();
        this.context=viewContext;
        frameView =  createFrameViewInstance(context);

        ///creating view for camera
        SurfaceView cameraView = new SurfaceView(context);
        cameraView.setVisibility(View.GONE);
        cameraView.setLayoutParams(frameView.getLayoutParams());
        frameView.addView(cameraView,TAG_CAMERA);

        ///creating view for image
        ImageView imageView = new ImageView(context);
        imageView.setVisibility(View.GONE);
//        imageView.setBackgroundColor(0XFFD81B60);
        imageView.setLayoutParams(frameView.getLayoutParams());
        frameView.addView(imageView,TAG_IMAGE);

        View view1 = activity.getLayoutInflater().inflate(R.layout.activity_camera, null);

        methodChannel = new MethodChannel(pluginBinding.getBinaryMessenger(), "plugins.flutter.io/deep_ar_camera/" + id);
        eventChannel = new EventChannel(pluginBinding.getBinaryMessenger(), "plugins.flutter.io/deep_ar_camera/events");

//        SurfaceView imgSurface = view1.findViewById(R.id.surface);
//        imgSurface.setFocusable(true);
//        imgSurface.setFocusableInTouchMode(true);
//        // Surface might already be initialized, so we force the call to onSurfaceChanged
//        imgSurface.setVisibility(View.GONE);
//        imgSurface.setVisibility(View.VISIBLE);
//        Log.d("DAMON", "THE IMAGE SURFACE W: " + imgSurface.getWidth() + " H: " + imgSurface.getHeight());

        ImageView offscreenView = activity.findViewById(R.id.surface);


        Map<String, Object> params = (Map<String, Object>) args;
        androidLicenceKey = String.valueOf(params.get("androidLicenceKey"));
        Object cameraEffect = params.get("cameraEffect");
        Object direction = params.get("cameraDirection");
        Object cameraMode = params.get("cameraMode");
         displayMode = (int) params.get("displayMode");
         startView();
         //checkPermissions();
    }

    private FrameLayout createFrameViewInstance(Context context) {
        FrameLayout framelayout = new FrameLayout(context);
        FrameLayout.LayoutParams flp= new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        framelayout.setLayoutParams(flp);
        return framelayout;
    }

    private void checkPermissions(){
        Log.d("Setup", "Check Permissions Function");
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED 
                || ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d("Setup", "Permissions not granted");
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
             builder.setTitle("Permissions Needed!");
            builder.setMessage("Permissions are required to use this plugin");
            builder.setCancelable(true);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            //TODO Permission has already been granted
            startView();
        }
    }

    
   public void startView(){
        
       handleDeepAr = new HandleDeepAr(this,context,activity,androidLicenceKey,frameView);
//        imgSurface.getHolder().addCallback(new HandleDeepArSurface(handleDeepAr));
//        imgSurface.setOnClickListener(view -> handleDeepAr.onClick());
       handleDeepAr.setDisplayMode(displayMode);
       methodChannel.setMethodCallHandler(new HandlePluginMethod(this));
       eventChannel.setStreamHandler(this);

   }

    @Override
    public View getView() {
//        return view;
        return frameView;
    }

    @Override
    public void dispose() {
    if(null!=handleDeepAr)handleDeepAr.onDestroy();
   if(null!=eventSink) eventSink.endOfStream();
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink eventSink) {
        Log.e(TAG,"onListen");
        this.eventSink=eventSink;
//        Map<String, Object> argument = new HashMap<>();
//        argument.put("isReady",true);
//        sendResponse("onCameraReady",argument);
    }

    @Override
    public void onCancel(Object arguments) {

    }

    public void sendResponse(String key, Map<String, Object> data) {
//        methodChannel.invokeMethod(key,data);
        Log.e(TAG,"sendResponse key "+key+" "+data.toString());
        Map<String, Object> argument = new HashMap<>();
        argument.put("methodName",key);
        argument.put("data",data);
        handler.post(()->eventSink.success(argument));
    }
}