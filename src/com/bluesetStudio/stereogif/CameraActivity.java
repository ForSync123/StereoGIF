package com.bluesetStudio.stereogif;

import com.bluesetStudio.stereogif.util.SystemUiHider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class CameraActivity extends Activity {
    
    
    
    static final int REQUEST_TAKE_PHOTO = 1;
    
    static final int REQUEST_IMAGE_CAPTURE = 1;
    
    private static String mTAG = "CameraActivity"; 
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    
    /*
     * Varibles declair for full camera control. 
     */
    SurfaceView sView;  
    SurfaceHolder surfaceHodler;  
    int screenWidth, screenHeight;  
    Camera camera;  // ����ϵͳ���õ������  
    boolean isPreview = false; // �Ƿ����Ԥ����  
    //*** End. ***
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_camera);

        

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.button_capture).setOnClickListener(mDummyButtonOnClickListener);
        
        //dispatchTakePictureIntent();
        
        
        //Code for full control camera
        // ��ȡ���ڹ�����  
        WindowManager wm = getWindowManager();  
        Display display = wm.getDefaultDisplay();  
        DisplayMetrics metrics = new DisplayMetrics();  
        // ��ȡ��Ļ�Ŀ�͸�  
        display.getMetrics(metrics);
        screenWidth = metrics.widthPixels;  
        screenHeight = metrics.heightPixels;  
        sView = (SurfaceView) findViewById(R.id.sView);  
        // ����surface����Ҫ�Լ���ά��������  
        sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
        // ���SurfaceView��SurfaceHolder  
        surfaceHodler = sView.getHolder();  
        // ΪsrfaceHolder���һ���ص�������  
        surfaceHodler.addCallback(new Callback() {  
  
            @Override  
            public void surfaceDestroyed(SurfaceHolder arg0) {  
                // ���camera��Ϊnull���ͷ�����ͷ  
                if (camera != null) {  
                    if (isPreview)  
                        camera.stopPreview();  
                    camera.release();  
                    camera = null;  
                }  
            }  
  
            @Override  
            public void surfaceCreated(SurfaceHolder arg0) {  
                // ������ͷ  
                initCamera();  
  
            }  
  
            @Override  
            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,  
                    int arg3) {  
            }  
        });  
        //end;
        
        
    }
    
    
    //Code for full control of camera
    private void initCamera() {  
        if (!isPreview) {  
            // �˴�Ĭ�ϴ򿪺�������ͷ  
            // ͨ������������Դ�ǰ������ͷ  
            camera = Camera.open();  
            camera.setDisplayOrientation(90);  
        }  
        if (!isPreview && camera != null) {  
            Camera.Parameters parameters = camera.getParameters();  
            // ����Ԥ����Ƭ�Ĵ�С  
            parameters.setPreviewSize(screenWidth, screenHeight);  
            // ����Ԥ����Ƭʱÿ����ʾ����֡����Сֵ�����ֵ  
            parameters.setPreviewFpsRange(4, 10);  
            // ������Ƭ�ĸ�ʽ  
            parameters.setPictureFormat(ImageFormat.JPEG);
            // ����JPG��Ƭ������  
            parameters.set("jpeg-quality", 85);  
            // ������Ƭ�Ĵ�С  
            parameters.setPictureSize(screenWidth, screenHeight);  
            // ͨ��SurfaceView��ʾȡ������  
            try {  
                camera.setPreviewDisplay(surfaceHodler);  
            } catch (IOException e) {  
                // TODO Auto-generated catch block  
                e.printStackTrace();  
            }  
            // ��ʼԤ��  
            camera.startPreview();  
            isPreview = true;  
        }  
    }  
    
    public void capture(View source) {  
        if (camera != null) {  
            // ��������ͷ�Զ��Խ��������  
            camera.autoFocus(autoFocusCallback);  
        }  
    }  
    
    AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {  
        
        @Override  
        public void onAutoFocus(boolean arg0, Camera arg1) {  
            if (arg0) {  
                // takePicture()������Ҫ����������������  
                // ��һ�������������û����¿���ʱ�����ü�����  
                // �ڶ������������������ȡԭʼ��Ƭʱ�����ü�����  
                // ���������������������ȡJPG��Ƭʱ�����ü�����  
                camera.takePicture(new ShutterCallback() {  
  
                    @Override  
                    public void onShutter() {  
                        // ���¿���˲���ִ�д˴�����  
                    }  
                }, new PictureCallback() {  
  
                    @Override  
                    public void onPictureTaken(byte[] arg0, Camera arg1) {  
                        // �˴�������Ծ����Ƿ���Ҫ����ԭʼ��Ƭ��Ϣ  
                    }  
                }, myJpegCallback);  
            }  
  
        }  
    };
    public Bitmap caputBitmap;
    PictureCallback myJpegCallback = new PictureCallback() {  
        
        @Override  
        public void onPictureTaken(byte[] data, Camera camera) {  
            
            // �����������õ����ݴ���λͼ  
            final Bitmap caputBitmap = BitmapFactory.decodeByteArray(data, 0,  
                    data.length);  
            //ImageView previewView = (ImageView) findViewById(R.id.imageView_preview);
            //previewView.setImageBitmap(caputBitmap);
            //previewView.setVisibility(View.VISIBLE);
            
            final View saveDialog = getLayoutInflater().inflate(R.layout.dialog_photo_confirm, null);  
            ImageView show = (ImageView) saveDialog.findViewById(R.id.imageView_previewDiag);
            TextView textPathTextView = (TextView) saveDialog.findViewById(R.id.TextViewFileLocation);
            
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            textPathTextView.setText(Environment  
                    .getExternalStorageDirectory()  
                    + "/"  
                    + imageFileName  
                    + ".jpg");
            show.setImageBitmap(caputBitmap);
            
            new AlertDialog.Builder(CameraActivity.this)
                .setView(saveDialog)
                .setNegativeButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_save, 
                        new DialogInterface.OnClickListener() {
                            
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                String imageFileName = "JPEG_" + timeStamp + "_";
                                File file = new File(Environment  
                                        .getExternalStorageDirectory()  
                                        + "/"  
                                        + imageFileName  
                                        + ".jpg");  
                                
                                FileOutputStream fileOutStream=null;  
                                try {  
                                    fileOutStream=new FileOutputStream(file);  
                                    //��λͼ�����ָ�����ļ���  
                                    caputBitmap.compress(CompressFormat.JPEG, 100, fileOutStream);  
                                    fileOutStream.close();  
                                } catch (IOException io) {  
                                    io.printStackTrace();  
                                }  
                                
                            }
                        }).show();
            camera.stopPreview();  
            camera.startPreview();  
            isPreview=true;  
            /*
            // ���ز����ļ�  
             
            // ��ȡsaveDialog�Ի����ϵ�ImageView���  
            ImageView show = (ImageView) saveDialog.findViewById(R.id.show);  
            // ��ʾ�ո��ĵõ���Ƭ  
            show.setImageBitmap(bm);  
            // ʹ��AlertDialog���  
            new AlertDialog.Builder(CameraActivity.this)  
                    .setView(saveDialog)  
                    .setNegativeButton("ȡ��", null)  
                    .setPositiveButton("����",  
                            new DialogInterface.OnClickListener() {  
                                @Override  
                                public void onClick(DialogInterface arg0,  
                                        int arg1) {  
                                    // ����һ��λ��SD���ϵ��ļ�  
                                    File file = new File(Environment  
                                            .getExternalStorageDirectory()  
                                            + "/"  
                                            + potoName.getText().toString()  
                                            + ".jpg");  
                                    FileOutputStream  fileOutStream=null;  
                                    try {  
                                        fileOutStream=new FileOutputStream(file);  
                                        //��λͼ�����ָ�����ļ���  
                                        bm.compress(CompressFormat.JPEG, 100, fileOutStream);  
                                        fileOutStream.close();  
                                    } catch (IOException io) {  
                                        io.printStackTrace();  
                                    }  
  
                                }  
                            }).show();  
            //�������  
            camera.stopPreview();  
            camera.startPreview();  
            isPreview=true;  
            */
        }  
        
    };  
    //end;

 
    
    //OnClick DummyButton
    View.OnClickListener mDummyButtonOnClickListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            
            //showImage();
            capture(sView);
        }
    };

    

    
    
    


    
    
    
    

}
