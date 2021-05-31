package com.camera_deep_ar;

import android.app.Activity;
import android.content.Context;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;

public class CameraDeepArViewFactory extends PlatformViewFactory {
  private final Activity mActivity;
  private final BinaryMessenger mBinaryMessenger;

  private final Registrar mRegistrar;

  public CameraDeepArViewFactory(Activity activity, BinaryMessenger binaryMessenger, Registrar registrar) {
    super(StandardMessageCodec.INSTANCE);
    this.mActivity =  activity;
    this.mBinaryMessenger=binaryMessenger;
    this.mRegistrar=registrar;
  }

  @Override
  public PlatformView create(Context context, int id, Object args) {
    return new CameraDeepArView(mActivity,mBinaryMessenger, mRegistrar,context,id,args);
  }
}