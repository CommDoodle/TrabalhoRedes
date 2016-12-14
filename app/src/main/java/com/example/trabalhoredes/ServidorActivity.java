package com.example.trabalhoredes;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ServidorActivity extends Activity {


    //Logging
    public String TAG = "ServidorActivity: ";

    TextView info, infoip, msg;
    String serverLog = "";
    ServerSocket serverSocket;
    Bitmap bitmapServer;
    private DrawingView drawView;
    private ImageButton currPaint;
    int len = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servidor_inicio);

        //Comunicacao
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        msg = (TextView) findViewById(R.id.msg);
        infoip.setText(getIpAddress());

        //Desenho
        drawView = (DrawingView) findViewById(R.id.drawingServidor);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colorsServidor);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

        try {
            int count = 0;
            serverSocket = new ServerSocket(8080);
            info.setText("Porta: " + serverSocket.getLocalPort() + " ");
            Log.d(TAG, "Criado serverSocket. IP:"+infoip+" Porta:"+serverSocket.getLocalPort());
            new RodaServerThread(serverSocket);
            Log.d(TAG, "Criado thread para rodar servidor");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private class RodaServerThread extends Thread {
        String TAG = "RodaServerThread:";
        ServerSocket serverSocket = null;
        public RodaServerThread(ServerSocket serverSocket) {
            this.serverSocket = serverSocket;
        }

        public void run()
        {
            int count = 0;
            while (true)
            {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    Log.d(TAG, "Cliente #" + count + socket.getInetAddress() + ":" + socket.getPort());
                    serverLog += "Cliente #" + count + socket.getInetAddress() + ":" + socket.getPort() + "\n";
                    msg.setText(serverLog);
                    new SocketServerThread(socket).start();
                    Log.d(TAG, "Criado thread para o Cliente #" + count);
                    count++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class SocketServerThread extends Thread {
        public String TAG = "SocketServerThread";
        Socket socket = null;
        public SocketServerThread(Socket socket) { this.socket = socket;}
        @Override
        public void run() {
            try {
                Log.d(TAG, "Dentro da thread que trata Cliente " + socket.getInetAddress() + ":" + socket.getPort());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                while (true) {
                    // Envia bitmap do servidor
                    //TODO
                    // Atualiza bitmap local usando bitmap do cliente
                    try {
                        len = dataInputStream.readInt();
                        if (len > 0) {
                            Log.d(TAG, "Opa! Recebi um bitmap de " + socket.getInetAddress() + ":" + socket.getPort());
                            byte[] data;
                            data = new byte[len];
                            dataInputStream.readFully(data, 0, data.length);
                            Bitmap bitmapClient = BitmapFactory.decodeByteArray(data, 0, data.length);
                            drawView.atualizaBitmap(bitmapClient);
                        }
                    } catch (EOFException e) {}
                    catch (IOException ef) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Travou: ",e.toString());
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