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
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;
//import ai.deepar.ar.DeepAR;


/** CameraDeepArPlugin */
public class CameraDeepArPlugin implements FlutterPlugin ,ActivityAware{

  private PluginRegistry.Registrar mPluginRegistrar;
    private FlutterPluginBinding pluginBinding;

  private CameraDeepArPlugin(Registrar registrar) {
    this.mPluginRegistrar=registrar;
  }

  public CameraDeepArPlugin() {}

  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null)return;
    final CameraDeepArPlugin plugin =new CameraDeepArPlugin(registrar);
      //registrar.activity().getApplication().registerActivityLifecycleCallbacks(plugin);
      final CameraDeepArViewFactory factory = new CameraDeepArViewFactory(registrar.activity(), registrar.messenger());
    registrar
            .platformViewRegistry()
            .registerViewFactory(
                    "plugins.flutter.io/deep_ar_camera", factory);
  }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
//        lifecycle = FlutterLifecycleAdapter.getActivityLifecycle(binding);
//        lifecycle.addObserver(this);
        final CameraDeepArViewFactory factory = new CameraDeepArViewFactory(binding.getActivity(), pluginBinding.getBinaryMessenger());

        pluginBinding
                .getPlatformViewRegistry()
                .registerViewFactory(
                        "plugins.flutter.io/deep_ar_camera", factory);

    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

    }

    @Override
    public void onDetachedFromActivity() {

    }

    private void checkForPermission(final MethodChannel.Result result) {
    Dexter.withContext(mPluginRegistrar.activity())
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
}
