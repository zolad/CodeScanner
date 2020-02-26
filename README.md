# CodeScanner
usage:

 private int[] mCodeType = new int[]{QRCODE,CODE39, CODE93, CODE128};
 GraphicDecoder mGraphicDecoder = new YUVFrameDecoder(this，mCodeType);

 if(mGraphicDecoder!=null)
        mGraphicDecoder.startDecode();


 mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    //Log.d("Camera", "onPreviewFrame():" + bytes.length);

                    mGraphicDecoder.decode(bytes, frameSizeW,
                            framewSizeH, null, false);


                }
            });


 @Override
    public void decodeComplete(String result, int type, int quality, int requestCode, byte[] rawResult) {

        Log.d(TAG,"result "+result+" type"+type);
        ToastHelper.showToast(this, "[type:" + type + " ,result:" + result + "]", ToastHelper.LENGTH_SHORT);


    }
