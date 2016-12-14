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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ServidorActivity extends Activity {


    //Logging
    public String TAG = "ServidorActivity: ";

    TextView info, infoip, logExterno;
    String serverLog = "";
    ServerSocket serverSocket;
    Bitmap bitmapServer;
    private DrawingView drawView;
    private ImageButton currPaint;
    int len = 0;

    //Lista das conexões com clientes
    ListaSockets minhasConexoes = new ListaSockets();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servidor_inicio);

        //Comunicacao
        info = (TextView) findViewById(R.id.info);
        infoip = (TextView) findViewById(R.id.infoip);
        logExterno = (TextView) findViewById(R.id.msg);
        infoip.setText(getIpAddress());

        //Desenho
        drawView = (DrawingView) findViewById(R.id.drawingServidor);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colorsServidor);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        drawView.setOnTouchListener(enviarBitmap);

        //Lista negra de dispositivos
        List<String> listaNegra = new ArrayList<String>();
        listaNegra.add("XT1022");

        Log.d(TAG, "Seu dispositivo: "+ android.os.Build.MODEL);

        //Gera NSD
        if (!listaNegra.contains("XT1022"))
        {
            Servidor servidor = new Servidor(this);
            try {
                servidor.rodar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            int count = 0;
            serverSocket = new ServerSocket(8080);
            info.setText("Porta: " + serverSocket.getLocalPort() + " ");
            Log.d(TAG, "Criado serverSocket. IP:"+infoip.getText()+" Porta:"+serverSocket.getLocalPort());
            (new RodaServerThread(serverSocket)).start();
            Log.d(TAG, "Criado thread para rodar servidor");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    View.OnTouchListener enviarBitmap = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            new EnviaParaTodosThread(minhasConexoes).start();
            Log.d(TAG, "Criado thread para envios múltiplos devido a desenho no servidor");
            return false;
        }
    };

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
                    minhasConexoes.add(socket);

                    DataOutputStream dosTemp = new DataOutputStream(socket.getOutputStream());
                    Log.d(TAG, "Enviando bitmap do servidor para recem conectado: " + socket.getInetAddress() + ":" + socket.getPort());
                    // Vou enviar bitmap do servidor
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    drawView.getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] array = bos.toByteArray();
                    dosTemp.writeInt(array.length);
                    dosTemp.write(array, 0, array.length);
                    dosTemp.flush();

                    MyRunnableTextView runnable = new MyRunnableTextView();
                    runnable.setData(serverLog);
                    runOnUiThread(runnable);

                    new SocketServerThread(socket).start();
                    Log.d(TAG, "Criado thread para o Cliente #" + count);
                    count++;
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        }

        private class MyRunnableTextView implements Runnable {
            private String string;
            public void setData(String _string) {
                this.string = _string;
            }

            public void run() {
                logExterno.setText(string);
            }
        }
    }

    private class SocketServerThread extends Thread {
        public int count = 1;
        public String TAG = "SocketServerThread";
        Socket socket = null;
        public SocketServerThread(Socket socket) { this.socket = socket;}
        @Override
        public void run() {
            try {
                Log.d(TAG, "Dentro da thread que trata Cliente " + socket.getInetAddress() + ":" + socket.getPort());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                while (true && socket.isConnected()) {

                    try {
                        len = dataInputStream.readInt();
                        if (len > 0) {
                            // Vou receber bitmap do cliente
                            Log.d(TAG, "Opa! Recebi pela #"+count+" um bitmap de " + socket.getInetAddress() + ":" + socket.getPort());
                            Log.d(TAG, "Bitmap size: "+len);
                            count++;
                            byte[] data = new byte[len];
                            dataInputStream.readFully(data, 0, data.length);
                            Bitmap bitmapClient = BitmapFactory.decodeByteArray(data, 0, data.length);
                            MyRunnableBitmap runnable = new MyRunnableBitmap();
                            runnable.setData(bitmapClient);
                            runOnUiThread(runnable);

                            Log.d(TAG, "Vou enviar meu bitmap atualizado para: " + socket.getInetAddress() + ":" + socket.getPort());
                            // Vou enviar bitmap do servidor
                            //ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            //drawView.getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                            //byte[] array = bos.toByteArray();
                            //dataOutputStream.writeInt(array.length);
                            //dataOutputStream.write(array, 0, array.length);
                            //dataOutputStream.flush();
                            new EnviaParaTodosThread(minhasConexoes).start();
                            Log.d(TAG, "Criado thread para envios múltiplos devido recebimento de cliente # " + socket.getInetAddress() + ":" + socket.getPort());

                        }
                    } catch (EOFException e) {}
                    catch (IOException ef) {}
                }
                if (!socket.isConnected()) {
                    dataInputStream.close();
                    dataInputStream = null;
                    dataOutputStream.close();
                    dataOutputStream = null;
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Travou: ",e.toString());
            }
        }

        private class MyRunnableBitmap implements Runnable {
            private Bitmap bitmap;
            public void setData(Bitmap _bitmap) {
                this.bitmap = _bitmap;
            }

            public void run() {
                drawView.atualizaBitmap(bitmap);
            }
        }
    }

    private class EnviaParaTodosThread extends Thread {
        public int count = 1;
        public String TAG = "EnviaParaTodosThread";
        ListaSockets minhasConexoes;
        public EnviaParaTodosThread(ListaSockets minhasConexoes) { this.minhasConexoes = minhasConexoes;}
        @Override
        public void run() {
            try {
                Log.d(TAG, "Dentro da thread que envia para todos");
                for (int i = 0; i < minhasConexoes.getLista().size(); i++) {
                    Socket socket = minhasConexoes.get(i);
                    if (socket != null) {
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        if (socket.isConnected()) {
                            Log.d(TAG, "Envios múltiplos: #"+count+" para: " + socket.getInetAddress() + ":" + socket.getPort());
                            // Vou enviar bitmap do servidor
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            drawView.getBitmap().compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                            byte[] array = bos.toByteArray();
                            try {
                                dataOutputStream.writeInt(array.length);
                                dataOutputStream.write(array, 0, array.length);
                                dataOutputStream.flush();
                            } catch (java.net.SocketException e)
                            {
                                Log.d(TAG, "Não enviado para: " + socket.getInetAddress() + ":" + socket.getPort() + ". Comunicação quebrada.");
                            }
                        }
                        else {
                            dataInputStream.close();
                            dataInputStream = null;
                            dataOutputStream.close();
                            dataOutputStream = null;
                            socket.close();
                            socket = null;
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Socket #"+count +" está morto aqui (null).");
                    }
                    count++;
                }
                for (int j = minhasConexoes.getLista().size()-1; j > -1; j--)
                    if (minhasConexoes.get(j) == null) minhasConexoes.remove(j);
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