package com.camera_deep_ar.handlers;

import android.view.SurfaceHolder;

public class HandleDeepArSurface implements SurfaceHolder.Callback{

    final HandleDeepAr handle;

    public HandleDeepArSurface(HandleDeepAr handle) {
        this.handle = handle;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//       handle.init();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // If we are using on screen rendering we have to set surface view where DeepAR will render
//        handle.deepAR.setRenderSurface(holder.getSurface(), handle.imgSurface.getWidth(), handle.imgSurface.getHeight());
        handle.deepAR.setRenderSurface(holder.getSurface(), width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        handle.deepAR.setRenderSurface(null, 0, 0);
    }
}
