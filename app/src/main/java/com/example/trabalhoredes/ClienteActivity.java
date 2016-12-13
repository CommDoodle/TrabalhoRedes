package com.example.trabalhoredes;

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

import java.io.IOException;
import java.net.Socket;

public class ClienteActivity extends Activity {

    TextView textResponse;
    EditText editTextAddress, editTextPort;
    Button buttonConnect, buttonClear;

    private DrawingView drawView;
    private ImageButton currPaint;
    MyClientTask myClientTask;

    Socket socket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_inicio);

        editTextAddress = (EditText) findViewById(R.id.address);
        editTextAddress.setText("192.168.43.1");
        editTextPort = (EditText) findViewById(R.id.port);
        editTextPort.setText("8080");
        buttonConnect = (Button) findViewById(R.id.connect);
        textResponse = (TextView) findViewById(R.id.response);
        buttonConnect.setOnClickListener(buttonConnectOnClickListener);

        drawView = (DrawingView) findViewById(R.id.drawing);
        LinearLayout paintLayout = (LinearLayout) findViewById(R.id.paint_colors);
        currPaint = (ImageButton)paintLayout.getChildAt(0);
        currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        drawView.setOnTouchListener(enviarBitmap);
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

            String tMsg = "Enviar Desenho";
            //TODO Criar socket em nova thread
            MyClientSocketTask mcst = new MyClientSocketTask();
            mcst.execute(editTextAddress.getText().toString(), editTextPort.getText().toString());
        }
    };

    View.OnTouchListener enviarBitmap = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            String tMsg = "Enviar Desenho";

            myClientTask = new MyClientTask(socket, tMsg, textResponse, drawView.getBitmap());
            myClientTask.execute();
            myClientTask = null;
            return false;
        }
    };

    public class MyClientSocketTask extends AsyncTask<String, Void, Socket> {

        String refLog = "ClientSocketTask";

        @Override
        protected Socket doInBackground(String... params) {

            try {
                return new Socket(params[0], Integer.parseInt(params[1]));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Socket sckt) {
            socket = sckt;
            Log.d(refLog, "socket instanciado");
        }

    }




}
