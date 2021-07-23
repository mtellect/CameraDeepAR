package com.camera_deep_ar;

import android.annotation.SuppressLint;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.deepar.ar.DeepAR;
import ai.deepar.ar.DeepARImageFormat;

public class LoadImageHandler {

    private DeepAR imageReceiver;
    private WeakReference<ContextWrapper> mContext;

    ByteBuffer nv21bb;
    int width;
    int height;

    Bitmap lastImage;
    boolean lastRotate;

    public LoadImageHandler(ContextWrapper context) {
        this.mContext = new WeakReference<ContextWrapper>(context);
    }

    synchronized void setImageReceiver(DeepAR receiver) {
        this.imageReceiver = receiver;
    }

    void refreshBitmap() {
        if (lastImage != null) {
            uploadBitmapToDeepAR(lastImage, lastRotate);
        }
    }

    void loadBitmapFromGallery(Bitmap bitmap){
        lastImage = bitmap;
        if (lastImage != null) {
            lastRotate = false;
            uploadBitmapToDeepAR(bitmap, false);
        }
    }

    private void uploadBitmapToDeepAR(Bitmap selectedImage, boolean rotate) {

        double scaleX_Y = (double) selectedImage.getWidth() / (double) selectedImage.getHeight();
        double scaleY_X = (double) selectedImage.getHeight()/ (double) selectedImage.getWidth();
        double scalerRatio = scaleX_Y;
        if(scalerRatio > scaleY_X) scalerRatio = scaleY_X;
        int newHeight = (int) (1280*scalerRatio);
        int newWidth = (int) (720*scalerRatio);

        if (newHeight > 1280) newHeight = 1280;
        if (newWidth > 720) newWidth = 720;

        final Bitmap resizedBitmap = scaleCenterCrop(selectedImage, newHeight, newWidth);
        width = resizedBitmap.getWidth();
        height = resizedBitmap.getHeight();

        

        byte[] nv21Bytes = getNV21(width, height, resizedBitmap);

        nv21bb = ByteBuffer.allocateDirect(nv21Bytes.length);
        nv21bb.order(ByteOrder.nativeOrder());
        nv21bb.put(nv21Bytes);
        nv21bb.position(0);

        // NOTE Due  to DeepAR optimization to work on a stream of frames from video we need to feed at least
        // 2 frames to jumpstart tracking and rendering process
        
        // NOTE Due to initial rotation of portrait image by 90 degrees, we need to tell DeepAR to rotate
        // the final output by another 270 degrees to output a portrait image
        SystemClock.sleep(100);
        imageReceiver.receiveFrame(nv21bb, width, height, 0, false, DeepARImageFormat.YUV_NV21, 1);
        SystemClock.sleep(100);
        imageReceiver.receiveFrame(nv21bb, width, height, 0, false, DeepARImageFormat.YUV_NV21, 1 );
    }

    // https://stackoverflow.com/questions/5960247/convert-bitmap-array-to-yuv-ycbcr-nv21
    byte [] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int [] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte [] yuv = new byte[inputWidth*inputHeight*3/2];
        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();

        return yuv;
    }

    void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ( (  66 * R + 129 * G +  25 * B + 128) >> 8) +  16;
                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte)((V<0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte)((U<0) ? 0 : ((U > 255) ? 255 : U));
                }

                index ++;
            }
        }
    }

    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }



    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void saveBitmapToGallery(Bitmap bitmap){
        String filename = "rotated"+System.currentTimeMillis()+".png";
        MediaStore.Images.Media.insertImage(mContext.get().getContentResolver(), bitmap, filename , "Description.");
    }



    //NOTE Rotation code

           
//        final Bitmap resizedBitmap;
//        final Bitmap rotatedBitmap;
//        if (rotate){
//            resizedBitmap = scaleCenterCrop(selectedImage, 1280, 720);
//            rotatedBitmap = rotateBitmap(resizedBitmap, 90);
//        }
//        else {
//            resizedBitmap = scaleCenterCrop(selectedImage, 720, 1280);
//            rotatedBitmap = rotateBitmap(resizedBitmap, 180);
//        }
//
//        width = rotatedBitmap.getWidth();
//        height = rotatedBitmap.getHeight();

}