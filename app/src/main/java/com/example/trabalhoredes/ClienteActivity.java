package com.example.trabalhoredes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClienteActivity extends Activity {
    private String TAG = "ClienteActivity";

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect;

    String ip = "";
    int port;

    private DrawingView drawView;
    private ImageButton currPaint;

    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    public int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_inicio);


        buttonConnect = (Button) findViewById(R.id.connect);
        textResponse = (TextView) findViewById(R.id.response);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));


        Bundle b = getIntent().getExtras();

        //Conexão sem precisar saber maluco!!
        if (b != null)
        {
            if (b.getBoolean("direto"))
            {
                ip = b.getString("ip");
                port = b.getInt("port");
                LinearLayout cabecalho = (LinearLayout) findViewById(R.id.cabecalhoConexao);
                cabecalho.setVisibility(LinearLayout.GONE);
                MyClientSocketTask mcst = new MyClientSocketTask();
                mcst.execute(ip, Integer.toString(port));
                drawView.setOnTouchListener(enviarBitmap);
            }
        }
        else {
            editTextAddress = (EditText) findViewById(R.id.address);
            editTextAddress.setText("192.168.1.37");
            editTextPort = (EditText) findViewById(R.id.port);
            editTextPort.setText("8080");
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

    OnClickListener buttonConnectOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            MyClientSocketTask mcst = new MyClientSocketTask();
            mcst.execute(editTextAddress.getText().toString(), editTextPort.getText().toString());
            drawView.setOnTouchListener(enviarBitmap);
        }
    };

    View.OnTouchListener enviarBitmap = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            new MyClientTask().execute(drawView.getBitmap());
            return false;
        }
    };

    public class MyClientSocketTask extends AsyncTask<String, Void, Socket> {

        String refLog = "ClientSocketTask";

        @Override
        protected Socket doInBackground(String... params) {

            try {
                Socket socket = new Socket(params[0], Integer.parseInt(params[1]));
                (new SocketListen(socket)).start();
                Log.d(TAG, "Criado thread para o Cliente escutar recebimentos de servidor");
                return socket;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Socket sckt) {
            socket = sckt;
            try {
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(refLog, "socket instanciado");
        }

    }


    public class MyClientTask extends AsyncTask<Bitmap, Void, String> {

        @Override
        protected String doInBackground(Bitmap... params) {
            try {
                // Envia bitmap de cliente
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                params[0].compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] array = bos.toByteArray();
                dataOutputStream.writeInt(array.length);
                dataOutputStream.write(array, 0, array.length);
                dataOutputStream.flush();
                count++;
                return ("Bitmap #"+count+" sent. Size: " + array.length);

            } catch (UnknownHostException e) {
                e.printStackTrace();
                return "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "IOException: " + e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            ((TextView) findViewById(R.id.response)).setText(result);
        }

    }

    private class SocketListen extends Thread {
        public int count = 1;
        public String TAG = "SocketListen";
        Socket socket = null;

        public SocketListen(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                Log.d(TAG, "Dentro da thread que pega recebimentos do servidor" + socket.getInetAddress() + ":" + socket.getPort());
                while (true && socket.isConnected()) {

                    try {
                        int len = dataInputStream.readInt();
                        if (len > 0) {
                            // Vou receber bitmap do servidor
                            Log.d(TAG, "Opa! Recebi pela #" + count + " um bitmap do servidor " + socket.getInetAddress() + ":" + socket.getPort());
                            Log.d(TAG, "Bitmap size: " + len);
                            count++;
                            byte[] data = new byte[len];
                            dataInputStream.readFully(data, 0, data.length);
                            Bitmap bitmapClient = BitmapFactory.decodeByteArray(data, 0, data.length);
                            MyRunnableBitmap runnable = new MyRunnableBitmap();
                            runnable.setData(bitmapClient);
                            runOnUiThread(runnable);

                        }
                    } catch (EOFException e) {
                    } catch (IOException ef) {
                    }
                }
                if (false) {
                    dataInputStream.close();
                    dataInputStream = null;
                    dataOutputStream.close();
                    dataOutputStream = null;
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Travou: ", e.toString());
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
}
