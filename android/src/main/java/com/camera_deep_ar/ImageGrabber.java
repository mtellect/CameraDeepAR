package com.camera_deep_ar;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import ai.deepar.ar.DeepAR;

public class ImageGrabber {

    private static final String TAG = "ImageGrabber";
    private DeepAR frameReceiver;
    private WeakReference<ContextWrapper> mContext;

    ByteBuffer nv21bb;
    int width;
    int height;

    public Bitmap lastImage;
    boolean lastRotate;

    public ImageGrabber() {

    }

    //    public  ImageGrabber(ContextWrapper context) {
//        this.mContext = new WeakReference<ContextWrapper>(context);
//    }
    public void setContext(ContextWrapper context) {
        this.mContext = new WeakReference<ContextWrapper>(context);
    }

    public synchronized void setFrameReceiver(DeepAR frameReceiver) {
        this.frameReceiver = frameReceiver;
    }

    public void refreshBitmap() {
        refreshBitmap(false);
//        if (lastImage != null) {
//            uploadBitmapToDeepAR(lastImage, lastRotate);
//        }
    }

    void refreshBitmap(boolean scale) {
        if (lastImage != null) {
            uploadBitmapToDeepAR(lastImage, lastRotate, scale);
        }
    }

    public void loadDefaultBitmap() {
        Bitmap defaultImage = ((BitmapDrawable) mContext.get().getResources()
                .getDrawable(R.drawable.default_face)).getBitmap();
        lastImage = defaultImage;
        lastRotate = true;
        uploadBitmapToDeepAR(defaultImage, false);
    }

    public void loadBitmapFromGallery(Bitmap bitmap) {
        lastImage = bitmap;
        if (lastImage != null) {
            lastRotate = false;
            uploadBitmapToDeepAR(bitmap, false);
        }
    }

    private void uploadBitmapToDeepAR(Bitmap selectedImage, boolean rotate) {
        uploadBitmapToDeepAR(selectedImage, rotate, true);
    }


    private void uploadBitmapToDeepAR(Bitmap selectedImage, boolean rotate, boolean scale) {
        Bitmap resizedBitmap = null;

        if (scale) {
            double scaleX_Y = (double) selectedImage.getWidth() / (double) selectedImage.getHeight();
            double scaleY_X = (double) selectedImage.getHeight() / (double) selectedImage.getWidth();
            double scalerRatio = scaleX_Y;
            if (scalerRatio > scaleY_X) scalerRatio = scaleY_X;
            int newHeight = (int) (1280 * scalerRatio);
            int newWidth = (int) (720 * scalerRatio);
            if (newHeight % 2 == 1) newHeight++;
            if (newWidth % 2 == 1) newWidth++;
            if (newHeight > 1280) newHeight = 1280;
            if (newWidth > 720) newWidth = 720;

            resizedBitmap = scaleCenterCrop(selectedImage, newWidth, newHeight);
        } else {
            int width = selectedImage.getWidth();
            int height = selectedImage.getHeight();

            if (width % 2 == 1) width++;
            if (height % 2 == 1) height++;

            if (width != selectedImage.getWidth() || height != selectedImage.getHeight()) {
                resizedBitmap = scaleCenterCrop(selectedImage, width, height);
            } else {
                resizedBitmap = selectedImage.copy(selectedImage.getConfig(), true);
            }
        }

        width = resizedBitmap.getWidth();
        height = resizedBitmap.getHeight();

        Log.d("DAMON", "HEIGHT: " + height + " WIDTH: " + width);
        try {
//                YuvImage image=generateYuvImage(ImageFormat.NV21,resizedBitmap,0);
//            byte[] nv21Bytes= image.getYuvData();
            byte[] nv21Bytes = getNV21(width, height, resizedBitmap);
            Log.d("DAMON", "TRYING: " + nv21Bytes.toString());
            nv21bb = ByteBuffer.allocateDirect(nv21Bytes.length);
            nv21bb.order(ByteOrder.nativeOrder());
            nv21bb.put(nv21Bytes);
            nv21bb.position(0);
            Log.d("DAMON", "FRAME HERE: " + nv21Bytes.toString());
            frameReceiver.receiveFrame(nv21bb, width, height, 0, false);


//            //    DeepARImageFormat imageFormat = DeepARImageFormat.YUV_420_888;
//            SystemClock.sleep(50);
//            frameReceiver.receiveFrame(nv21bb, width, height, 0, false);
//            SystemClock.sleep(50);
//            frameReceiver.receiveFrame(nv21bb, width, height, 0, false);

            // NOTE Due  to DeepAR optimization to work on a stream of frames from video we need to feed at least
            // 2 frames to jumpstart tracking and rendering process

            // NOTE Due to initial rotation of portrait image by 90 degrees, we need to tell DeepAR to rotate
            // the final output by another 270 degrees to output a portrait image
//    SystemClock.sleep(100);
//    frameReceiver.receiveFrame(nv21bb, width, height, 0, false);
//    SystemClock.sleep(100);
//    frameReceiver.receiveFrame(nv21bb, width, height, 0, false);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "ArrayIndexOutOfBoundsException >> ", e);
        } catch (NullPointerException e) {
            Log.e(TAG, "NullPointerException >> ", e);
        }
    }


    // https://stackoverflow.com/questions/5960247/convert-bitmap-array-to-yuv-ycbcr-nv21
    byte[] getNV21(int inputWidth, int inputHeight, Bitmap scaled) {

        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
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
                B = (argb[index] & 0xff);

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : (Math.min(Y, 255)));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : (Math.min(V, 255)));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : (Math.min(U, 255)));
                }

                index++;
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


    public static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void saveBitmapToGallery(Bitmap bitmap) {
        String filename = "rotated" + System.currentTimeMillis() + ".png";
        MediaStore.Images.Media.insertImage(mContext.get().getContentResolver(), bitmap, filename, "Description.");
    }

    // Generate YuvImage based on the content in bitmap. If paddings > 0, the
    // strides of YuvImage are calculated by adding paddings to bitmap.getWidth().
    private YuvImage generateYuvImage(int format, Bitmap bitmap, int paddings) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int stride = width + paddings;
        YuvImage image = null;
        int[] argb = new int[stride * height];
        bitmap.getPixels(argb, 0, stride, 0, 0, width, height);
        byte[] yuv = convertArgbsToYuvs(argb, stride, height, format);
        int[] strides = null;
        if (format == ImageFormat.NV21) {
            strides = new int[]{stride, stride};
        } else if (format == ImageFormat.YUY2) {
            strides = new int[]{stride * 2};
        }
        image = new YuvImage(yuv, format, width, height, strides);
        return image;
    }

    private byte[] convertArgbsToYuvs(int[] argb, int width, int height,
                                      int format) {
        byte[] yuv = new byte[width * height *
                ImageFormat.getBitsPerPixel(format)];
        if (format == ImageFormat.NV21) {
            int vuStart = width * height;
            byte[] yuvColor = new byte[3];
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; ++col) {
                    int idx = row * width + col;
                    argb2yuv(argb[idx], yuvColor);
                    yuv[idx] = yuvColor[0];
                    if ((row & 1) == 0 && (col & 1) == 0) {
                        int offset = row / 2 * width + col / 2 * 2;
                        yuv[vuStart + offset] = yuvColor[2];
                        yuv[vuStart + offset + 1] = yuvColor[1];
                    }
                }
            }
        } else if (format == ImageFormat.YUY2) {
            byte[] yuvColor0 = new byte[3];
            byte[] yuvColor1 = new byte[3];
            for (int row = 0; row < height; ++row) {
                for (int col = 0; col < width; col += 2) {
                    int idx = row * width + col;
                    argb2yuv(argb[idx], yuvColor0);
                    argb2yuv(argb[idx + 1], yuvColor1);
                    int offset = idx / 2 * 4;
                    yuv[offset] = yuvColor0[0];
                    yuv[offset + 1] = yuvColor0[1];
                    yuv[offset + 2] = yuvColor1[0];
                    yuv[offset + 3] = yuvColor0[2];
                }
            }
        }
        return yuv;
    }

    private static final int CSHIFT = 16;
    private static final int CYR = 19595;
    private static final int CYG = 38470;
    private static final int CYB = 7471;
    private static final int CUR = -11059;
    private static final int CUG = -21709;
    private static final int CUB = 32768;
    private static final int CVR = 32768;
    private static final int CVG = -27439;
    private static final int CVB = -5329;

    private void argb2yuv(int argb, byte[] yuv) {
        int r = Color.red(argb);
        int g = Color.green(argb);
        int b = Color.blue(argb);
        yuv[0] = (byte) ((CYR * r + CYG * g + CYB * b) >> CSHIFT);
        yuv[1] = (byte) (((CUR * r + CUG * g + CUB * b) >> CSHIFT) + 128);
        yuv[2] = (byte) (((CVR * r + CVG * g + CVB * b) >> CSHIFT) + 128);
    }


}