package com.camera_deep_ar.handlers;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.Buffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
 
import ai.deepar.ar.ARErrorType;
import ai.deepar.ar.AREventListener;

public class HandleDeepArEvents implements AREventListener {

    final HandleDeepAr handle;

    public HandleDeepArEvents(HandleDeepAr handle) {
        this.handle = handle;
    }

    @Override
    public void screenshotTaken(Bitmap bitmap) {
       handle.onPhotoTaken(bitmap);
    }

    @Override
    public void videoRecordingStarted() {

    }

    @Override
    public void videoRecordingFinished() {
        handle.onVideoRecordingFinished();
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
        //NOTE: to jumpstart masks
        //TODO: check if it works with camera as well
        Log.d("MASK", "Image Loaded");
//        handle.switchEffect();
//        handle.deepAR.switchEffect("mask", handle.getFilterPath(masks.get(0)));
//        handle.switchEffect("mask","none");
    }

    @Override
    public void faceVisibilityChanged(boolean b) {
        Log.d("DAMON", "FACE VISIBILITY IS BEING CHECKED " + b);
    }

    @Override
    public void imageVisibilityChanged(String s, boolean b) {
        Log.d("DAMON", "IMAGE VISIBILITY IS BEING CHECKED " + b);
    }

    @Override
    public void frameAvailable(Image frame) {
        if (frame != null) {
            handle.setImageFrame(frame);
        }
    }



    @Override
    public void error(ARErrorType arErrorType, String s) {

    }

    @Override
    public void effectSwitched(String s) {
        Map<String, Object> argument = new HashMap<>();
        argument.put("path",s); 
        handle.pluginView.sendResponse("onSwitchEffect",argument);
    }
}
