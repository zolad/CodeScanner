package com.zolad.codescanner.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;



public interface GraphicDecoder {

    String TAG = "CodeScanner";
    /**
     * Symbol detected but not decoded.
     */
    public static final int PARTIAL = 1;
    /**
     * EAN-8.
     */
    public static final int EAN8 = 8;
    /**
     * UPC-E.
     */
    public static final int UPCE = 9;
    /**
     * ISBN-10 (from EAN-13).
     */
    public static final int ISBN10 = 10;
    /**
     * UPC-A.
     */
    public static final int UPCA = 12;
    /**
     * EAN-13.
     */
    public static final int EAN13 = 13;
    /**
     * ISBN-13 (from EAN-13).
     */
    public static final int ISBN13 = 14;
    /**
     * Interleaved 2 of 5.
     */
    public static final int I25 = 25;
    /**
     * DataBar (RSS-14).
     */
    public static final int DATABAR = 34;
    /**
     * DataBar Expanded.
     */
    public static final int DATABAR_EXP = 35;
    /**
     * Codabar.
     */
    public static final int CODABAR = 38;
    /**
     * Code 39.
     */
    public static final int CODE39 = 39;
    /**
     * PDF417.
     */
    public static final int PDF417 = 57;
    /**
     * QR Code.
     */
    public static final int QRCODE = 64;
    /**
     * Code 93.
     */
    public static final int CODE93 = 93;
    /**
     * Code 128.
     */
    public static final int CODE128 = 128;
    /**
     * 延时解码
     */
    int HANDLER_DECODE_DELAY = 60001;

    /**
     * 解码完成
     */
    int HANDLER_DECODE_COMPLETE = 60002;


    int HANDLER_CODE_EXIST = 60010;


    /**
     * 指定解码类型
     */
    void setCodeTypes(int[] codeType);

    /**
     * 设置解码监听
     */
    void setDecodeListener(DecodeListener listener);

    /**
     * 停止解码，会清空任务队列，并取消延时解码
     */
    void stopDecode();

    /**
     * 开始解码
     */
    void startDecode();

    /**
     * 延迟一段时间后开始解码，单位毫秒
     */
    void startDecodeDelay(int delay);

    /**
     * 传入本地图片的Uri进行解码
     * 注意：会清空任务队列中的所有任务
     */
    void decodeForResult(Context context, Uri uri, int requestCode);

    /**
     * 传入Bitmap对象进行解码
     * 注意：1.会清空任务队列中的所有任务 2.立即回收bitmap对象会报错 3.解码结束会自动回收该对象
     * TODO ？？ 有没有可能会内存泄漏呢
     */
    void decodeForResult(Bitmap bitmap, RectF rectClipRatio, int requestCode);

    /**
     * 传入图像的像素数组及图像宽高进行解码
     * 注意：会清空任务队列中的所有任务
     */
    void decodeForResult(int[] pixels, int width, int height, RectF rectClipRatio, int requestCode);

    /**
     * 传入图片的YUV数组及图像宽高进行解码
     */
    void decode(byte[] frameData, int width, int height, RectF rectClipRatio, boolean isSavePic);

    void detach();

    interface DecodeListener {
        /**
         * 解码完成后会进行回调
         */
        void decodeComplete(String result, int type, int quality, int requestCode, byte[] rawResult);

        //void codeExist(boolean exist);
    }

}
