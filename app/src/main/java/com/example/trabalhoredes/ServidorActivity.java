package com.example.trabalhoredes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ServidorActivity extends Activity {

    TextView info, infoip, msg;
    String serverLog = "";
    ServerSocket serverSocket;
    Bitmap bitmapClient;
    Bitmap bitmapServer;
    private DrawingView drawView;
    private ImageButton currPaint;
    int len = 0;


    // Input output
    DataInputStream dataInputStream = null;
    DataOutputStream dataOutputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servidor_inicio);

        //Comunicacao
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);

        infoip.setText(getIpAddress());

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();


        //Desenho
        drawView = (DrawingView) findViewById(R.id.drawingServidor);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colorsServidor);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        bitmapServer = drawView.getBitmap();


    }

    public void paintClicked(View view){
        //use chosen color
        if(view!=currPaint){
            //update color
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();

            drawView.setColor(color);
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            currPaint=(ImageButton)view;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketServerThread extends Thread {

        static final int SocketServerPORT = 8080;
        int count = 0;

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(SocketServerPORT);
                ServidorActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        info.setText("Porta: " + serverSocket.getLocalPort() + " ");
                    }
                });

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(socket.getInputStream());
                    dataOutputStream = new DataOutputStream(socket.getOutputStream());

                    // Envia bitmap do servidor
                    //TODO

                    // Atualiza bitmap local usando bitmap do cliente
                    byte[] data;
                    len = dataInputStream.readInt();
                    data = new byte[len];
                    if (len > 0) {
                        dataInputStream.readFully(data, 0, data.length);
                        bitmapClient = BitmapFactory.decodeByteArray(data, 0, data.length);
                    }


                    //Inutilidade
                    count++;
                    serverLog += "#" + count + socket.getInetAddress() + ":" + socket.getPort() + "\n";

                    ServidorActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            msg.setText(serverLog);
                            //Aqui que está o erro. Culpa desse excesso de multi threading
                            // que é um assunto que a gnt nem viu ainda na vida.
                            synchronized (drawView)
                            {
                                drawView.atualizaBitmap(bitmapClient);

                                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                drawView.getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                                byte[] array = bos.toByteArray();

                                try {
                                    dataOutputStream.writeInt(array.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    dataOutputStream.write(array, 0, array.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });

                }
            } catch (IOException e) {
                e.printStackTrace();
                final String errMsg = e.toString();
                ServidorActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        msg.setText(errMsg);
                    }
                });

            }
        }

    }

    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "IP: "
                                + inetAddress.getHostAddress() + "\n";
                    }

                }

            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }

        return ip;
    }
}