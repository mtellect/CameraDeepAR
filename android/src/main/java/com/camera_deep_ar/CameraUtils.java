package com.camera_deep_ar;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CameraUtils {


    private static final String TAG = "CameraUtils";

    public static float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float handleZoom(MotionEvent event,CameraGrabber cameraGrabber,float mDist) {
      Camera.Parameters params=  cameraGrabber.getCamera().getParameters();
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);

        if (Math.abs(newDist - mDist) < 2) return mDist;

        if (newDist > mDist) {
            //zoom in
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            //zoom out
            if (zoom > 0)
                zoom--;
        }
        params.setZoom(zoom);
        cameraGrabber.getCamera().setParameters(params);
        return  newDist;
    }

    public static void handleFocus(MotionEvent event, CameraGrabber cameraGrabber, View view) {
        int pointerId = event.getPointerId(0);
        int pointerIndex = event.findPointerIndex(pointerId);
        // Get the pointer's current position

        int xxw = view.getHeight();
        int xxh = view.getWidth();
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
            Log.e(TAG, "Error getting parameter:" + e);
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

}
