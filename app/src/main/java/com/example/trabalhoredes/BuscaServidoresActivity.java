package com.example.trabalhoredes;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class BuscaServidoresActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {

    Cliente cliente;
    Spinner spinnerServidores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_servidores);

        cliente = new Cliente(this, (NsdManager) this.getSystemService(Context.NSD_SERVICE), getIPAddress(true));
        try {
            cliente.rodar();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void descobrirServidores(View view) throws InterruptedException {
        cliente.descobrirServidores();
        Thread.sleep(10000);
        cliente.pararBusca();

        // Spinner Drop down elements
        List<String> servidores = cliente.getListaServidores();

        spinnerServidores = (Spinner) findViewById(R.id.spinnerServidores);
        spinnerServidores.setOnItemSelectedListener(this);
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, servidores);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        spinnerServidores.setAdapter(dataAdapter);


    }

    public void conectarServidor(View view) {
        Intent intent = new Intent(this, ClienteActivity.class);
        Bundle b = new Bundle();
        b.putString("ip", spinnerServidores.getSelectedItem().toString().split(":")[0]);
        b.putInt("port", Integer.parseInt(spinnerServidores.getSelectedItem().toString().split(":")[1]));
        b.putBoolean("direto", true);
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();

    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    // Pega o ip da máquina
    private String getDeviceIpAddress() {
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
