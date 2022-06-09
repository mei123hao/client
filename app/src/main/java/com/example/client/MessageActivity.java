package com.example.client;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MessageActivity extends AppCompatActivity {

    private TextView ip_View;
    private EditText editText_data;
    private Button send;
    private SocketManager socketManager = new SocketManager();

    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        ip_View = (TextView) findViewById(R.id.ip_Adr_view);
        editText_data = (EditText) findViewById(R.id.send_msg_Edit);

        send = (Button) findViewById(R.id.send);

        Intent msg_intent = getIntent();
        Bundle bundle = msg_intent.getBundleExtra("connection_msg");
        ip_View.setText(bundle.getString("IP")+":"+bundle.getInt("PORT"));
        socketManager.connect_hold(bundle.getString("IP"), bundle.getInt("PORT"));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限

            int i = ContextCompat.checkSelfPermission(this, PERMISSIONS[0]);
            //如果权限申请失败，则重新申请权限
            if (i != PackageManager.PERMISSION_GRANTED) {
                //重新申请权限函数
                startRequestPermission();
                Log.e("这里", "权限请求成功");
            }
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                socketManager.send_Msg(editText_data.getText().toString()+'\0');
            }
        });

    }

    private void startRequestPermission(){
        //321为请求码
        ActivityCompat.requestPermissions(this,PERMISSIONS,321);
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketManager.connect_hold(socketManager.getIp(), socketManager.getPort());
    }


    //    public void send() {
//        data = editText_data.getText().toString();
//        if (data == null) {
//            Toast.makeText(MessageActivity.this,"please input Sending Data",Toast.LENGTH_SHORT).show();
//        } else {
//            data = data + '\0';
//        }
//
//        Thread sendThread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                if(socketStatus){
//                    try {
//                        outputStream.write(data.getBytes());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        };
//        sendThread.start();
//    }



    /*当客户端界面返回时，关闭相应的socket资源*/
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        /*关闭相应的资源*/
//        try {
//            outputStream.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}