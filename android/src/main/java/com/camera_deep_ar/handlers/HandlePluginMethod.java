package com.camera_deep_ar.handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.camera_deep_ar.CameraDeepArView;
import com.camera_deep_ar.CameraGrabber;
import com.camera_deep_ar.ImageGrabber;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ai.deepar.ar.DeepAR;
import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class HandlePluginMethod implements MethodChannel.MethodCallHandler {
    private static final String TAG ="HandlePluginMethod";
    final private CameraDeepArView pluginView;
    final private HandleDeepAr handle;

    public HandlePluginMethod(CameraDeepArView pluginView) {
        this.pluginView=pluginView;
        this.handle=pluginView.handleDeepAr;

    }
    

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
Log.e(TAG,"onMethodCall "+methodCall.method);
        if ("isCameraReady".equals(methodCall.method)) {
            Map<String, Object> argument = new HashMap<>();
            argument.put("isReady",true);
            pluginView.sendResponse("onCameraReady",argument);
            result.success("Android is ready");
        }
        else  if ("setCameraMode".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object mode = params.get("mode");
                int index=Integer.parseInt(String.valueOf(mode));
                handle.setCameraMode(index);
            }
            result.success("CameraMode Changed");
        }
        else  if ("setDisplayMode".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object mode = params.get("mode");
                int index=Integer.parseInt(String.valueOf(mode));
                handle.setDisplayMode(index);
            }
            result.success("DisplayMode Changed");
        }
        else  if ("switchCameraDirection".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object direction = params.get("direction");
                int index=Integer.parseInt(String.valueOf(direction));
                handle.changeCameraDevice(index);
            }
            result.success("CameraDirection Changed");
        }

        else  if ("zoomTo".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object index = params.get("zoom");
                int zoom=Integer.parseInt(String.valueOf(index));
                handle.zoomTo(zoom);
            }
            result.success("ZoomTo Changed");
        }
        else  if ("startVideoRecording".equals(methodCall.method)) {
             handle.startVideoRecording();
            result.success("VideoRecording Started");
        }
        else  if ("stopVideoRecording".equals(methodCall.method)) {
            handle.stopVideoRecording();
            result.success("VideoRecording Stopped");
        }
        else  if ("snapPhoto".equals(methodCall.method)) {
            handle.snapPhoto();
            result.success("Photo Snapped");
        }
        else  if ("dispose".equals(methodCall.method)) {
            handle.dispose();
            result.success("Disposed");
        }else  if ("switchEffect".equals(methodCall.method)) {
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object mode = params.get("mode");
                Object path = params.get("path");
                handle.switchEffect(String.valueOf(mode), String.valueOf(path));
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
                handle.changeParameterFloat(String.valueOf(changeParameter), String.valueOf(component), String.valueOf(parameter), (Float) floatParam);
            }
        } else if ("changeImage".equals(methodCall.method)){
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object filePath = params.get("filePath");
                Log.d("File path is ", filePath.toString());
                handle.changeImage(String.valueOf(filePath));
            }
            result.success("changedImage");
        }
        else if ("changeParameterTexture".equals(methodCall.method)){
            if (methodCall.arguments instanceof HashMap) {
                @SuppressWarnings({"unchecked"})
                Map<String, Object> params = (Map<String, Object>) methodCall.arguments;
                Object changeParameter = params.get("changeParameter");
                Object component = params.get("component");
                Object parameter = params.get("parameter");
                Object texturePath = params.get("texturePath");
                //BitmapFactory.Options options = new BitmapFactory.Options();
                //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                FlutterLoader loader = FlutterInjector.instance().flutterLoader();
                String pathJava = loader.getLookupKeyForAsset(String.valueOf(texturePath));
                handle.changeParameterTexture(changeParameter.toString(), component.toString(), parameter.toString(), pathJava);

            }
        }


    }

}
