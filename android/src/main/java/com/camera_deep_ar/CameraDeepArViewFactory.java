package com.camera_deep_ar;

import android.app.Activity;
import android.content.Context;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class CameraDeepArViewFactory extends PlatformViewFactory {
//  private final Activity mActivity;
//  private final BinaryMessenger mBinaryMessenger;

  private final ActivityPluginBinding activityBinding;
  private final FlutterPlugin.FlutterPluginBinding pluginBinding;

  public CameraDeepArViewFactory(ActivityPluginBinding activityBinding, FlutterPlugin.FlutterPluginBinding pluginBinding) {
    super(StandardMessageCodec.INSTANCE);
//    this.mActivity = activity;
//    this.mBinaryMessenger = binaryMessenger;
    this.activityBinding = activityBinding;
    this.pluginBinding = pluginBinding;
  }

  @Override
  public PlatformView create(Context context, int id, Object args) {
    return new CameraDeepArView(activityBinding, pluginBinding, context, id, args);
  }
}