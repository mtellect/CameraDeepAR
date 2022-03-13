package com.camera_deep_ar;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.NonNull;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;



import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugins.GeneratedPluginRegistrant;
//import ai.deepar.ar.DeepAR;


/** CameraDeepArPlugin */
public class CameraDeepArPlugin implements FlutterPlugin ,ActivityAware{

  private PluginRegistry.Registrar mPluginRegistrar;
    private FlutterPluginBinding pluginBinding;

    public CameraDeepArPlugin() {}

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                    (call, result) -> {
                       // Your existing code
              }
        );
   }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        initPlugin(binding,pluginBinding);
    }


    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivity() {

    }

    private void checkForPermission(ActivityPluginBinding binding,final MethodChannel.Result result) {
    Dexter.withContext(binding.getActivity())
            .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(new MultiplePermissionsListener() {
              @Override
              public void onPermissionsChecked(MultiplePermissionsReport report) {
                result.success(report.areAllPermissionsGranted());
              }

              @Override
              public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken token) {
                token.continuePermissionRequest();
              }
            })
            .check();
  }


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        pluginBinding = binding;
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        pluginBinding = null;
    }


    private void initPlugin(ActivityPluginBinding activityPluginBinding, FlutterPluginBinding pluginBinding) {
        final CameraDeepArViewFactory factory = new CameraDeepArViewFactory(
                activityPluginBinding,pluginBinding);
        pluginBinding
                .getPlatformViewRegistry()
                .registerViewFactory(
                        "plugins.flutter.io/deep_ar_camera", factory);
    }

}
