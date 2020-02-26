package com.zolad.codescanner.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;


import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class YUVFrameDecoder implements GraphicDecoder {



    //   private Image mZBarImage;
    //   private ImageScanner mImageScanner;

    private final Object decodeLock = new Object();//互斥锁

   // private Handler mHandler;

    private DecodeListener mDecodeListener;

    private ThreadPoolExecutor mExecutorService;
    private ArrayBlockingQueue<Runnable> mArrayBlockingQueue;

    private ArrayList<Scanner> mScannerQueue;


    public static final int QUEUESIZE = 2;

    public static final int MAX_QUEUE = 1;
    public static final int COREPOOLSIZE = 1;
    public static final int MAXIMUMPOOLSIZE = 1;
    public static final long KEEPALIVETIME = 5000L;


    private volatile boolean isDecodeEnabled;//解码开关，默认为true
    private int[] mSymbolTypeArray;

    /**
     * @param listener 解码监听
     */
    public YUVFrameDecoder(DecodeListener listener) {
        this(listener, null);
    }

    /**
     * @param listener        解码监听
     * @param symbolTypeArray 指定条码类型进行识别，支持的格式EAN8、ISBN10、UPCA、EAN13、ISBN13、I25、UPCE、DATABAR、DATABAR_EXP、CODABAR、CODE39、PDF417、QRCODE、CODE93、CODE128，可根据实际需要进行配置。
     */
    public YUVFrameDecoder(DecodeListener listener, final int[] symbolTypeArray) {
        this.isDecodeEnabled = true;
        this.mDecodeListener = listener;

     //   this.mHandler = new Handler(this);
        mArrayBlockingQueue = new ArrayBlockingQueue<>(MAX_QUEUE);
        mExecutorService = new ThreadPoolExecutor(COREPOOLSIZE, MAXIMUMPOOLSIZE,
                KEEPALIVETIME, TimeUnit.MILLISECONDS, mArrayBlockingQueue);
        //ImageScanner的构造方法中含有System.loadLibrary()，要避免在主线程中进行IO操作
        mSymbolTypeArray = symbolTypeArray;
        mScannerQueue = new ArrayList<>(QUEUESIZE);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                initZBar(symbolTypeArray);
            }
        });
    }

    /**
     * 初始化ImageScanner&Image
     */
    private void initZBar(final int[] symbolTypeArray) {

        mScannerQueue.clear();
        Scanner scanner;
        for (int i = 0; i < QUEUESIZE; i++) {

            scanner = new Scanner();
            scanner.ImageScanner = new ImageScanner();
            setCodeTypes(scanner.ImageScanner, symbolTypeArray);
            scanner.ZBarImage = new Image("Y800");
            //scanner.ZBarImage.setSize(processWidth,processHeight);

            mScannerQueue.add(scanner);
        }


    }


    public void setCodeTypes(ImageScanner scanner, int[] symbolTypeArray) {
        if (scanner == null) {
            return;
        }
        scanner.setConfig(0, Config.X_DENSITY, 1);
        scanner.setConfig(0, Config.Y_DENSITY, 1);
        scanner.setConfig(0, Config.ENABLE, 0);//Disable all the types
        if (symbolTypeArray == null) {
            symbolTypeArray = new int[]{QRCODE, CODE39, CODE93, CODE128};
            //EAN8, ISBN10, UPCA, EAN13, ISBN13, CODEI25//, UPCE, DATABAR, PARTIAL
            //       , DATABAR_EXP, CODABAR, CODE39, PDF417,
        }
        for (int symbolType : symbolTypeArray) {
            scanner.setConfig(symbolType, Config.ENABLE, 1);//enable codeType
        }
    }

    @Override
    public void setCodeTypes(int[] codeType) {

    }

    @Override
    public void setDecodeListener(DecodeListener listener) {
        this.mDecodeListener = listener;
    }

    @Override
    public void stopDecode() {
        if (mArrayBlockingQueue != null) {
            mArrayBlockingQueue.clear();
        }

        if (mExecutorService != null)
            mExecutorService.shutdown();

//        if (mHandler != null) {
//            mHandler.removeMessages(HANDLER_DECODE_DELAY);
//        }
        this.isDecodeEnabled = false;
    }

    @Override
    public void startDecode() {
        this.isDecodeEnabled = true;
    }

    @Override
    public void startDecodeDelay(int delay) {
//        if (mHandler != null) {
//            mHandler.sendMessageDelayed(mHandler.obtainMessage(HANDLER_DECODE_DELAY), delay);
//        }
        this.isDecodeEnabled = true;
    }

    @Override
    public synchronized void decodeForResult(Context context, Uri uri, int requestCode) {
        if (isDecodeEnabled && mExecutorService != null && mArrayBlockingQueue != null) {
            mArrayBlockingQueue.clear();

            if (mExecutorService.isShutdown())
                createPool();
            mExecutorService.execute(new DecodeRunnable(context.getApplicationContext(), uri, requestCode));
        }
    }

    @Override
    public synchronized void decodeForResult(Bitmap bitmap, RectF clipRectRatio, int requestCode) {
        if (isDecodeEnabled && mExecutorService != null && mArrayBlockingQueue != null) {
            mArrayBlockingQueue.clear();

            if (mExecutorService.isShutdown())
                createPool();
            mExecutorService.execute(new DecodeRunnable(bitmap, clipRectRatio, requestCode));
        }
    }

    @Override
    public synchronized void decodeForResult(int[] pixels, int width, int height, RectF clipRectRatio, int requestCode) {
        if (isDecodeEnabled && mExecutorService != null && mArrayBlockingQueue != null) {
            mArrayBlockingQueue.clear();
            if (mExecutorService.isShutdown())
                createPool();

            mExecutorService.execute(new DecodeRunnable(pixels, width, height, clipRectRatio, requestCode));
        }
    }

    @Override
    public synchronized void decode(byte[] frameData, int width, int height, RectF clipRectRatio, boolean isSavePic) {

      // Log.e("decode","开始任务");
        if (isDecodeEnabled && mExecutorService != null && mArrayBlockingQueue != null && mArrayBlockingQueue.size() < MAX_QUEUE) {

           // Log.e("decode","开始识别");
            if (mExecutorService.isShutdown())
                createPool();

            mExecutorService.execute(new DecodeRunnable(frameData, width, height, clipRectRatio, isSavePic));
        }


    }


    public void createPool() {
        mExecutorService = new ThreadPoolExecutor(COREPOOLSIZE, MAXIMUMPOOLSIZE,
                KEEPALIVETIME, TimeUnit.MILLISECONDS, mArrayBlockingQueue);

    }

    @Override
    public void detach() {
        Log.d(TAG, getClass().getName() + ".detach()");
        synchronized (YUVFrameDecoder.this) {
            if (mExecutorService != null) {
                mExecutorService.shutdown();
                mExecutorService = null;
            }
            if (mArrayBlockingQueue != null) {
                mArrayBlockingQueue.clear();
                mArrayBlockingQueue = null;
            }
        }
        synchronized (decodeLock) {
//            if (mHandler != null) {
//                mHandler.removeCallbacksAndMessages(null);
//                mHandler = null;
//            }

//            Iterator<Scanner> iter = mScannerQueue.iterator();
//            while (iter.hasNext()) {
//
//                Scanner zScanner = iter.next();
//                if (zScanner.ZBarImage != null) {
//                    zScanner.ZBarImage.destroy();
//                    zScanner.ZBarImage = null;
//                }
//
//
//                if (zScanner.ImageScanner != null) {
//                    zScanner.ImageScanner.destroy();
//                    zScanner.ImageScanner = null;
//
//                }
//
//                iter.remove();
//
//            }

        }
    }


//    public boolean handleMessage(Message msg) {
//        switch (msg.what) {
//            case HANDLER_DECODE_DELAY: {//开启解码
//               // startDecode();
//                break;
//            }
//            case HANDLER_DECODE_COMPLETE: {//解码成功
//                if (mDecodeListener != null && isDecodeEnabled) {
//                    Bundle bundle = msg.peekData();
//                    if (bundle != null) {
//                        mDecodeListener.decodeComplete(bundle.getString("result"), bundle.getInt("type"),
//                                bundle.getInt("quality"), bundle.getInt("requestCode"), bundle.getByteArray("rawResult"));
//                    }
//                }
//                break;
//            }
//
//            case HANDLER_CODE_EXIST: {
//                if (mDecodeListener != null) {
//
//                }
//
//            }
//        }
//
//
//        return true;
//    }


    private class DecodeRunnable implements Runnable {

        private Uri mUri;
        private Context mContext;
        private int mRequestCode;
        private boolean isSavePic = false;
        private Bitmap mBitmap;
        private int[] mPixels;
        private byte[] mYUVFrameData;

        private int mWidth;
        private int mHeight;
        private RectF mClipRectRatio;

        DecodeRunnable(Context context, Uri uri, int requestCode) {
            this.mRequestCode = requestCode;
            this.mContext = context;
            this.mUri = uri;
        }

        DecodeRunnable(Bitmap bitmap, RectF clipRectRatio, int requestCode) {
            this.mRequestCode = requestCode;
            this.mBitmap = bitmap;
            this.mWidth = bitmap.getWidth();
            this.mHeight = bitmap.getHeight();
            this.mClipRectRatio = clipRectRatio;
        }

        DecodeRunnable(int[] pixels, int width, int height, RectF clipRectRatio, int requestCode) {
            this.mRequestCode = requestCode;
            this.mPixels = pixels;
            this.mWidth = width;
            this.mHeight = height;
            this.mClipRectRatio = clipRectRatio;

        }

        DecodeRunnable(byte[] frameData, int width, int height, RectF clipRectRatio, boolean isSavePic) {
            this.mYUVFrameData = frameData;
            this.mWidth = width;
            this.mHeight = height;
            this.mClipRectRatio = clipRectRatio;
            this.isSavePic = isSavePic;
        }

        @Override
        public void run() {

            if (mYUVFrameData == null) {
                // return;
                if (mPixels == null) {
                    if (mBitmap == null) {
                        mBitmap = ImageUtils.getBitmap(mContext, mUri);
                    }
                    if (mBitmap != null) {
                        mWidth = mBitmap.getWidth();
                        mHeight = mBitmap.getHeight();
                    }
                    mPixels = ImageUtils.getBitmapPixels(mBitmap);
                }
                mYUVFrameData = ImageUtils.getYUVFrameData(mPixels, mWidth, mHeight);
            }
            Log.d("start", "mWidth:" + mWidth + ", mHeight:" + mHeight);
            //2.解析图像
            long begin = System.currentTimeMillis();


            SymbolSet symbolSet = null;
            Scanner scanner = null;

            try {
                scanner = mScannerQueue.get(0);


            } catch (Exception e) {
                e.printStackTrace();


                return;
            }


            if (scanner == null)
                return;

//            if (isSavePic) {
//                YuvImage image = new YuvImage(mYUVFrameData, ImageFormat.NV21, mWidth, mHeight, null);
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                image.compressToJpeg(new Rect(0, 0, mWidth, mHeight), 70, stream);
//                Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
//                ImageUtils.saveBitmap2(bmp);
//                try {
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }


            try {


            scanner.ImageScanner.reCreate();
            setCodeTypes(scanner.ImageScanner, mSymbolTypeArray);

            scanner.ZBarImage.reCreate();

            scanner.ZBarImage.setFormat("Y800");
            scanner.ZBarImage.setSize(mWidth, mHeight);

            symbolSet = decodeImage(scanner.ZBarImage, scanner.ImageScanner, mYUVFrameData, mWidth, mHeight, mClipRectRatio);
            analysisResult(symbolSet, mRequestCode);


            }catch (Exception e){

            }

            long end = System.currentTimeMillis();


            Log.d("decode", "  time  :" + (end - begin) + (symbolSet != null ? " resultNum" + symbolSet.size() : ""));


        }


        /**
         * 使用zbar解析图像，返回一个Symbol集合
         *
         * @param frameData     图像的byte数组
         * @param width         图像的宽
         * @param height        图像的高
         * @param clipRectRatio 图像区域的剪裁比例
         */
        private SymbolSet decodeImage(Image mZBarImage, ImageScanner mImageScanner, byte[] frameData, int width, int height, RectF clipRectRatio) {
            if (mZBarImage == null || mImageScanner == null || frameData == null) return null;


            mZBarImage.setData(frameData);


            if (mImageScanner.scanImage(mZBarImage) != 0) {
                return mImageScanner.getResults();
            }
            return null;
        }

        /**
         * 从Symbol集合中获取结果
         */
        private void analysisResult(SymbolSet symbolSet, int requestCode) {
            if (symbolSet != null) {
                for (Symbol symbol : symbolSet) {
                    byte[] result = symbol.getDataBytes();
                    if (result != null && result.length > 0) {

                        int type = symbol.getType();
                        int quality = symbol.getQuality();
                        String resultss = symbol.getData();

                        if (resultss == null || resultss.length() <= 0) {
                            resultss = "nullstr";
                        }


                        if (mArrayBlockingQueue != null)
                            mArrayBlockingQueue.clear();
                        decodeComplete(symbol.getDataBytes(), resultss, type, quality, requestCode);
                        return;
                    } else {


                    }
                }
            }
        }

        private void decodeComplete(byte[] rawResult, String result, int type, int quality, int requestCode) {
           // if (mHandler != null) {
           //     Message message = mHandler.obtainMessage(HANDLER_DECODE_COMPLETE);
//            Message message = new Message();
//                Bundle bundle = message.getData();
//                bundle.putByteArray("rawResult", rawResult);
//                bundle.putString("result", result);
//                bundle.putInt("type", type);
//                bundle.putInt("quality", quality);
//                bundle.putInt("requestCode", requestCode);
//                message.setData(bundle);
//                //mHandler.sendMessage(message);
//                handleMessage(message);
           // }

            if (mDecodeListener != null && isDecodeEnabled) {
              //  Bundle bundle = msg.peekData();
               // if (bundle != null) {
                    mDecodeListener.decodeComplete(result, type,
                            quality, requestCode, rawResult);
             //   }
            }


        }


        private void sendIsCodeExist(int exist) {
        //    if (mHandler != null) {
          //      Message message = mHandler.obtainMessage(HANDLER_CODE_EXIST);
//            Message message = new Message();
//
//            // Bundle bundle = message.getData();
//                //  bundle.putB("exist", exist);
//                message.arg1 = exist;
//                // message.setData(bundle);
//               // mHandler.sendMessage(message);
//            handleMessage(message);

            //  }
        }


    }


}
