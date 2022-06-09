package com.example.client;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button send_Msg;
    private Button send_Video_Stream;
    private Button connect;
    private EditText ip_Edit;

    private SocketManager socketManager = new SocketManager();

    private ProgressDialog pd;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (socketManager.startPing(ip_Edit.getText().toString())) {
                pd.dismiss();
                Toast.makeText(MainActivity.this, "连接成功",Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "连接异常",Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        send_Msg = (Button) findViewById(R.id.send_msg);
        send_Video_Stream = (Button) findViewById(R.id.send_video_stream);
        connect = (Button) findViewById(R.id.connect);
        ip_Edit = (EditText) findViewById(R.id.ip_Adr_edit);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd = ProgressDialog.show(MainActivity.this, "连接", "连接中...");
                socketManager.connect(ip_Edit.getText().toString(), 8000, handler);
            }
        });


        send_Msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MessageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("IP", socketManager.getIp());
                bundle.putInt("PORT", socketManager.getPort());
                intent.putExtra("connection_msg", bundle);
                startActivity(intent);
            }
        });

        send_Video_Stream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(MainActivity.this, "攻城狮还在开发中!",Toast.LENGTH_LONG).show();
                Intent video_intent = new Intent();
                video_intent.setClass(MainActivity.this, VideoActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("IP", socketManager.getIp());
                bundle.putInt("PORT", socketManager.getPort());
                video_intent.putExtra("connection_msg", bundle);
                startActivity(video_intent);
            }
        });

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    //退出应用提示
    private static Boolean isQuit = false;
    private Timer timer = new Timer();
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isQuit) {
                isQuit = true;
                Toast.makeText(getBaseContext(), R.string.back_more_quit,Toast.LENGTH_LONG).show();
                TimerTask task = null;
                task = new TimerTask() {
                    @Override
                    public void run() {
                        isQuit = false;
                    }
                };
                timer.schedule(task, 2000);
            } else {
                finish();
                System.exit(0);
            }
        }
        return false;
    }


}
