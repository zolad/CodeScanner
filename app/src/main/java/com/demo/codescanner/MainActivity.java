package com.demo.codescanner;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.zolad.codescanner.core.GraphicDecoder;
import com.zolad.codescanner.core.YUVFrameDecoder;

import java.util.List;

import static com.zolad.codescanner.core.GraphicDecoder.CODE128;
import static com.zolad.codescanner.core.GraphicDecoder.CODE39;
import static com.zolad.codescanner.core.GraphicDecoder.CODE93;
import static com.zolad.codescanner.core.GraphicDecoder.QRCODE;

public class MainActivity extends AppCompatActivity implements GraphicDecoder.DecodeListener {

    public String TAG = "MainActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mCameraframelayout;
    public int  screenWidth;
    public int  screenHeight;
    public int frameSizeW,framewSizeH;
    public AutoFocusManager autoFocusManager;
    private GraphicDecoder mGraphicDecoder;
    private int[] mCodeType = new int[]{QRCODE,CODE39, CODE93, CODE128};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getScreenSize();
        setContentView(R.layout.activity_main);
        mCameraframelayout = (FrameLayout) findViewById(R.id.camera_framelayout);
        mPreview = (CameraPreview) findViewById(R.id.camera_preview);

        mPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(mGraphicDecoder!=null)
//                    mGraphicDecoder.stopDecode();
//
//                if (mGraphicDecoder != null) {
//                    mGraphicDecoder.detach();
//                    mGraphicDecoder = null;
//                }
            }
        });

        if(mCameraframelayout.getHeight()!=0)
        screenHeight = mCameraframelayout.getHeight();

        mGraphicDecoder = new YUVFrameDecoder(this,mCodeType);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //权限不足
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);

            return;
        }

        openCamera();

    }


    @Override
    protected void onPause() {


        if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }

        if(mGraphicDecoder!=null)
            mGraphicDecoder.stopDecode();


        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if(mPreview!=null)
            mPreview.setCamera(null);

        super.onPause();


    }


    @Override
    protected void onDestroy() {
        if (mGraphicDecoder != null) {
            mGraphicDecoder.detach();
            mGraphicDecoder = null;
        }
        super.onDestroy();
    }

    public void openCamera(){
        if (checkCameraHardware(this)) {

            Toast.makeText(this,"camera count"+ Camera.getNumberOfCameras(),Toast.LENGTH_LONG).show();

            try {
                mCamera = Camera.open();
                Camera.Parameters parameters = mCamera.getParameters();
           //     Log.e("testtest",parameters.flatten());
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                int[] a = new int[sizes.size()];
                int[] b = new int[sizes.size()];
                for (int i = 0; i < sizes.size(); i++) {
                    int supportH = sizes.get(i).height;
                    int supportW = sizes.get(i).width;
                    a[i] = Math.abs(supportW - screenHeight);
                    b[i] = Math.abs(supportH - screenWidth);
                    Log.d(TAG,"supportW:"+supportW+"supportH:"+supportH);
                }
                int minW=0,minA=a[0];
                for( int i=0; i<a.length; i++){
                    if(a[i]<=minA){
                        minW=i;
                        minA=a[i];
                    }
                }
                int minH=0,minB=b[0];
                for( int i=0; i<b.length; i++){
                    if(b[i]<minB){
                        minH=i;
                        minB=b[i];
                    }
                }
                Log.d(TAG,"result="+sizes.get(minW).width+"x"+sizes.get(minH).height);
              //  List<Integer> list = parameters.getSupportedPreviewFrameRates();

               // frameSizeW = parameters.getPreviewSize().width;
              //  framewSizeH = parameters.getPreviewSize().height;
                frameSizeW = sizes.get(minW).width;
                framewSizeH = sizes.get(minH).height;

                parameters.setPreviewSize(frameSizeW,framewSizeH);
                mCamera.setParameters(parameters);





                mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.d("Camera", "onPreviewFrame():" + bytes.length);

                        if(mGraphicDecoder!=null)
                          mGraphicDecoder.decode(bytes, frameSizeW,
                                framewSizeH, null, false);


                    }
                });


                mPreview.setCamera(mCamera);
                mPreview.startPreview();

                if(mGraphicDecoder!=null)
                    mGraphicDecoder.startDecode();

                autoFocusManager = new AutoFocusManager(this, mCamera);

            } catch (Exception e) {
                Log.e(TAG, "openCamera Error"+e.getMessage());
                e.printStackTrace();
            }


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = true;

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                granted = false;
            }
        }

        if(granted){

            openCamera();
        }



    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    public void getScreenSize() {
        WindowManager wm = (WindowManager) getSystemService(
                Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        screenWidth = display.getWidth();
        screenHeight = display.getHeight();
    }

    @Override
    public void decodeComplete(String result, int type, int quality, int requestCode, byte[] rawResult) {

        Log.d(TAG,"result "+result+" type"+type);
        ToastHelper.showToast(this, "[type:" + type + " ,result:" + result + "]", ToastHelper.LENGTH_SHORT);


    }
}
