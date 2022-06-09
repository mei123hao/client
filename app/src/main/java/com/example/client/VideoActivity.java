package com.example.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class VideoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{

    private ImageView cameraFramePreview;
    private TextureView cameraStreamPreview;
    private Camera mCamera;
    private Button turnCameraPos;
    private TextView ip_Port_View;
    private SocketManager socketManager = new SocketManager();

    private int cameraPosition = 0;   //0 后置 1 前置
    private int numberOfCameras;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,//写权限
            Manifest.permission.CAMERA//照相权限
    };

    private String ip;
    private int port;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        turnCameraPos = (Button) findViewById(R.id.turn_camera_Pos);
        ip_Port_View = (TextView) findViewById(R.id.video_ip_port_view);
        //帧数据预览
        cameraFramePreview = (ImageView) findViewById(R.id.iv_pic_back);
        cameraFramePreview.setRotation(90);

        //摄像头视频流预览
        cameraStreamPreview = (TextureView) findViewById(R.id.texture_view_back);
        cameraStreamPreview.setRotation(90); // 设置预览角度，并不改变获取到的原始数据方向(与Camera.setDisplayOrientation(0)

        Intent video_intent = getIntent();
        Bundle bundle = video_intent.getBundleExtra("connection_msg");
        ip = bundle.getString("IP");
        port = bundle.getInt("PORT");
        ip_Port_View.setText(ip+":"+port);
        socketManager.connect_hold(ip, port);  //建立连接

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            int i = ContextCompat.checkSelfPermission(this, PERMISSIONS_STORAGE[0]);
            //如果权限申请失败，则重新申请权限
            if (i != PackageManager.PERMISSION_GRANTED) {
                //重新申请权限函数
                startRequestPermission();
                Log.e("这里", "权限请求成功");
            }
        }

        initData();   //初始化摄像头,查看摄像头个数

        cameraFramePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                transmitVideoStream();
            }
        });
        cameraStreamPreview.setSurfaceTextureListener(this);

    }

    private void startRequestPermission(){
        //321为请求码
        ActivityCompat.requestPermissions(this,PERMISSIONS_STORAGE,321);
    }
    private void initData() {
        numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        if(numberOfCameras<1){
            Toast.makeText(this, "没有相机", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toast.makeText(this, "相机数目"+numberOfCameras+"个", Toast.LENGTH_SHORT).show();
    }

    private void transmitVideoStream() {
        if (mCamera!=null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                //预览
                //通过Android Camera拍摄预览中设置setPreviewCallback实现onPreviewFrame接口，实时截取每一帧视频流数据
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size=camera.getParameters().getPreviewSize();
                    try {
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null) {
                            ByteArrayOutputStream stream =new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0,0,size.width,size.height),80,stream);
                            stream.flush();  //不用等缓存满了再发送的意思
                            socketManager.send_Video(stream);
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //获取数据并可视化为ImageView
//    private void addCallBack() {
//        if (mCamera!=null) {
//            mCamera.setPreviewCallback(new Camera.PreviewCallback(){
//                //预览
//                //通过Android Camera拍摄预览中设置setPreviewCallback实现onPreviewFrame接口，实时截取每一帧视频流数据
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    Camera.Size size=camera.getParameters().getPreviewSize();
//                    try{
//                        YuvImage image=new YuvImage(data, ImageFormat.NV21,size.width,size.height,null);
//                        if(image!=null){
//                            ByteArrayOutputStream stream =new ByteArrayOutputStream();
//                            //将摄像头预览回调的每一帧Nv21数据通过jpeg压缩
//                            image.compressToJpeg(new Rect(0,0,size.width,size.height),80,stream);
//                            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());//解码
//                            cameraFramePreview.setImageBitmap(bmp);
//                            stream.close();
//                        }
//
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
//    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();// 停掉原来摄像头的预览
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

        /**
         * 初始化展示及相机参数配置
         * **/
        mCamera = Camera.open(cameraPosition);   //默认打开后置
        // 设置相机预览宽高，此处设置为TextureView宽高
        Camera.Parameters params = mCamera.getParameters();
//            params.setPreviewSize(width, height);//不能设置显示的图像的大小。有些相机尺寸不对，则会闪退
        // 设置自动对焦模式
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//设置自动对焦
            mCamera.setParameters(params);//设置相机参数，包括前后摄像头，闪光灯模式、聚焦模式、预览和拍照尺寸等。
        }
        try {
            mCamera.setDisplayOrientation(0);// 设置预览角度，并不改变获取到的原始数据方向
            // 绑定相机和预览的View
            mCamera.setPreviewTexture(surfaceTexture);//绑定绘制预览图像的surface。
            //surface是指向屏幕窗口原始图像缓冲区（raw buffer）的一个句柄，
            // 通过它可以获得这块屏幕上对应的canvas，进而完成在屏幕上绘制View的工作。

            // 开始预览
            mCamera.startPreview();//开始预览，将camera底层硬件传来的预览帧数据显示在绑定的surface上。
        } catch (IOException e) {
            e.printStackTrace();
        }

        /**
         * 翻转摄像头
         * **/
        turnCameraPos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

                for (int i=0;i<numberOfCameras;i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraPosition == 1) {  //前置变后置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            releaseCamera();
                            mCamera = Camera.open(0);
                            try {
                                mCamera.setDisplayOrientation(0);
                                mCamera.setPreviewTexture(surfaceTexture);
                                mCamera.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            cameraPosition = 0;
                            break;
                        }
                    } else {    //后置变前置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            releaseCamera();
                            mCamera = Camera.open(1);
                            try {
                                mCamera.setDisplayOrientation(0);
                                mCamera.setPreviewTexture(surfaceTexture);
                                mCamera.startPreview();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            cameraPosition = 1;
                            break;
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        mCamera.stopPreview();
        releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        mCamera.stopPreview();
//        mCamera.release();
//    }

}
