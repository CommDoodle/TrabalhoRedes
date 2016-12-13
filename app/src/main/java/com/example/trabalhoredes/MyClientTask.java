package com.example.trabalhoredes;

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

public class MyClientTask extends AsyncTask<Void, Void, Void> {

    String dstAddress;
    int dstPort;
    String response = "";
    String msgToServer;
    TextView textResp;
    Bitmap bitmapCliente;

    Socket socket;

    MyClientTask(Socket socket, String addr, int port, String msgTo, TextView textResponse, Bitmap bitCliente) {
        dstAddress = addr;
        dstPort = port;
        msgToServer = msgTo;
        textResp = textResponse;
        bitmapCliente = bitCliente;
        this.socket = socket;

    }

    @Override
    protected Void doInBackground(Void... arg0) {



            DataOutputStream dataOutputStream = null;
            DataInputStream dataInputStream = null;

            try {

                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                // Envia bitmap de cliente
                if (msgToServer != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmapCliente.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                    byte[] array = bos.toByteArray();

                    OutputStream out = socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(out);
                    dos.writeInt(array.length);
                    dos.write(array, 0, array.length);
                }

                // Recebe bitmap de servidor
                //TODO

                //Inutilidade
                response = "OK!";

            } catch (UnknownHostException e) {
                e.printStackTrace();
                response = "UnknownHostException: " + e.toString();
            } catch (IOException e) {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            } finally {
                if (socket != null) {
                /*try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                }

                if (dataOutputStream != null) {
                /*try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                }

                if (dataInputStream != null) {
                /*try {
                    dataInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                }
            }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        textResp.setText(response);
        super.onPostExecute(result);
    }

}
