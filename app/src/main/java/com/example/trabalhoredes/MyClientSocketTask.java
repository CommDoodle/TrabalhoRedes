package com.example.trabalhoredes;

/**
 * Created by daniel on 13/12/2016.
 */

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by daniel on 11/12/2016.
 */

public class MyClientSocketTask extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    TextView textResp;

    Socket socket;

    MyClientSocketTask(Socket socket, String addr, int port) throws IOException {
        dstAddress = addr;
        dstPort = port;
        this.socket = socket;

    }

    @Override
    protected Void doInBackground(Void... arg0) {

        try {
            socket = new Socket(dstAddress, dstPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        textResp.setText(response);
        super.onPostExecute(result);
    }

}
