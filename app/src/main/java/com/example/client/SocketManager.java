package com.example.client;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketManager {

    private static String ip;
    private static int port;
    public boolean socketStatus = false;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
//    private byte byteBuffer[] = new byte[1024];

    public String getIp() {
        return this.ip;
    }

    public int getPort() {
        return this.port;
    }

    public void connect(String ip, int port, Handler pdHandler) {
        this.ip = ip;
        this.port = port;

        Thread connect_Thread = new Thread() {
            @Override
            public void run() {
                super.run();
                if (!socketStatus) {
                    try {
                        socket = new Socket(ip, 8000);
                        if (socket == null) {
                        } else {
                            socketStatus = true;
                        }
                        outputStream = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Calculation.calculate(1);
                pdHandler.sendEmptyMessage(0);
            }
        };
        connect_Thread.start();
    }

    public void connect_hold(String ip, int port) {
        Thread connect_Thread = new Thread() {
            @Override
            public void run() {
                super.run();
                if (!socketStatus) {
                    try {
                        socket = new Socket(ip, 8000);
                        if (socket == null) {
                        } else {
                            socketStatus = true;
                        }
                        outputStream = new DataOutputStream(socket.getOutputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        connect_Thread.start();
    }

    public void send_Msg(final String data) {
//        if (data == null) {
//            Log.e("SocketManager", "无可发送数据");
//        } else {
//            data = data + '\0';
//        }
        Thread sendThread = new Thread() {
            @Override
            public void run() {
                super.run();
                if(socketStatus){
                    try {
                        outputStream.write(data.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        sendThread.start();
    }

    public void send_Video(ByteArrayOutputStream data) {

        Thread sendThread = new Thread() {
            @Override
            public void run() {
                super.run();
                if (socketStatus) {
                    try{
//                        ByteArrayInputStream inputStream = new ByteArrayInputStream(data.toByteArray());
//                        int len;
//                        while ((len = inputStream.read(byteBuffer)) != -1) {
//                            outputStream.write(byteBuffer, 0, len);
//                        }
                        outputStream.write(data.toByteArray());
                        outputStream.flush();
//                        outputStream.close();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        sendThread.start();
    }

    public boolean startPing(String ip) {
        Log.e("Ping", "startPing...");
        boolean success=false;
        Process p =null;

        try {
            p = Runtime.getRuntime().exec("ping -c 1 -i 0.2 -W 1 " +ip);
            int status = p.waitFor();
            if (status == 0) {
                success=true;
            } else {
                success=false;
            }
        } catch (IOException e) {
            success=false;
        } catch (InterruptedException e) {
            success=false;
        }finally{
            p.destroy();
        }

        return success;
    }


}
