package com.zolad.codescanner.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {

    /**
     * 保存方法
     */
    public static void saveBitmap(Bitmap bmp) {
        //Log.d("ericrece", "保存图片");

        //  SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        //  java.util.Date date=new java.util.Date();
        // String str=sdf.format(date)+".jpg";
        String str = "checkpicc.jpg";

        File f = new File(Environment.getExternalStorageDirectory() + "/" +
                str);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
           // Log.d("ericrece", "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //Log.d("recedata", e.getMessage() + "");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch
           // Log.d("recedata", e.getMessage() + "");

            e.printStackTrace();
        }

    }

    /**
     * 保存方法
     */
    public static void saveBitmap2(Bitmap bmp) {
        //Log.d("ericrece2", "保存图片2");

        //  SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
        //  java.util.Date date=new java.util.Date();
        // String str=sdf.format(date)+".jpg";
        String str = "checkpicc2.jpg";

        File f = new File(Environment.getExternalStorageDirectory() + "/" +
                str);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            //Log.d("ericrece2", "已经保存2");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //Log.d("recedata", e.getMessage() + "");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch
            //Log.d("recedata", e.getMessage() + "");

            e.printStackTrace();
        }

    }


    /**
     * 保存方法
     */
    public static void saveBitmap3(Bitmap bmp) {
       // Log.d("ericrece3", "保存图片3");

         SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
          Date date=new Date();
         String str=sdf.format(date)+".jpg";
       // String str = "checkpicc2.jpg";

        File f = new File(Environment.getExternalStorageDirectory() + "/" +
                str);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
           // Log.d("ericrece2", "已经保存2");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
           // Log.d("recedata", e.getMessage() + "");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch
           // Log.d("recedata", e.getMessage() + "");

            e.printStackTrace();
        }

    }

    /**
     * 保存方法
     */
    public static void saveBitmap4(Bitmap bmp,Date date) {
        //Log.d("ericrece4", "保存图片4");

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");

        String str=sdf.format(date)+".jpg";
        // String str = "checkpicc2.jpg";

        File f = new File(Environment.getExternalStorageDirectory() + "/" +
                str);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            //Log.d("ericrece2", "已经保存2");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            //Log.d("recedata", e.getMessage() + "");
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch
            //Log.d("recedata", e.getMessage() + "");

            e.printStackTrace();
        }

    }

    public static Bitmap getBitmap(Context context, Uri uri) {
        if (uri == null) return null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static  int[] getBitmapPixels(Bitmap bitmap) {
        if (bitmap == null) return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        bitmap.recycle();
        return pixels;
    }

    public static byte[] getYUVFrameData(int[] pixels, int width, int height)
    {
        if (pixels == null) return null;

        int index = 0;
        int yIndex = 0;
        int R, G, B, Y, U, V;
        byte[] frameData = new byte[width * height];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                R = (pixels[index] & 0xff0000) >> 16;
                G = (pixels[index] & 0xff00) >> 8;
                B = (pixels[index] & 0xff);

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
//                U = ( ( -38 * R -  74 * G + 112 * B + 128) >> 8) + 128;
//                V = ( ( 112 * R -  94 * G -  18 * B + 128) >> 8) + 128;

                frameData[yIndex++] = (byte) (Math.max(0, Math.min(Y, 255)));
//                if (j % 2 == 0 && index % 2 == 0) {
//                    yuv420sp[uvIndex++] = (byte)(Math.max(0, Math.min(U, 255)));
//                    yuv420sp[uvIndex++] = (byte)(Math.max(0, Math.min(V, 255)));
//                }
                index++;
            }
        }
        return frameData;
    }




    /*
     * 将RGB数组转化为像素数组
     */
    public static  int[] convertByteToColor2(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }


        // 理论上data的长度应该是3的倍数，这里做个兼容
        // int arg = 0;

        // int[] color = new int[size];

        int[] color = new int[size / 3];


        for (int i = 0; i < color.length; ++i) {

            color[i] = (data[i * 3] << 16 & 0x00FF0000) |
                    (data[i * 3 + 1] << 8 & 0x0000FF00) |
                    (data[i * 3 + 2] & 0x000000FF) |
                    0xFF000000;
        }


        return color;
    }

    /*
     * 将RGB数组转化为像素数组
     */
    public static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }


        // 理论上data的长度应该是3的倍数，这里做个兼容
        // int arg = 0;

        int[] color = new int[size];


        for (int i = 0; i < color.length; ++i) {

            color[i] = (data[i] << 16 & 0x00FF0000) |
                    (data[i] << 8 & 0x0000FF00) |
                    (data[i] & 0x000000FF) |
                    0xFF000000;

        }



        return color;
    }

}
