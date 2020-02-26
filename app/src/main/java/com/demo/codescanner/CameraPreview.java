package com.demo.codescanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;


    public CameraPreview(Context context) {
        this(context, null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setCamera(Camera camera){


        if(camera ==null) {
            mCamera = null;
            return;
        }

        if(mCamera == null) {
            mCamera = camera;
            mSurfaceHolder = getHolder();
            Log.i("Camera","getHolder:"+mSurfaceHolder);
            mSurfaceHolder.addCallback(this);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }else {

            mCamera = camera;

        }




    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mSurfaceHolder = getHolder();
        try {
            Log.i("Camera","surfaceCreated:"+mCamera);
            if(mCamera!=null) {
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int high) {
        if (mSurfaceHolder.getSurface() == null){
            return;
        }

//        try {
//            mCamera.stopPreview();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }

    public void startPreview(){

        if(mCamera == null)
            return;

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        if(mCamera!=null) {
            mCamera.stopPreview();
            mCamera.release();
        }
        if(mCamera != null){
            mCamera = null;
        }
    }
}