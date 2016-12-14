package com.example.trabalhoredes;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by daniel on 13/12/2016.
 */


public class Servidor {

    //Referência para logging
    private static final String refLog = "Servidor: ";

    //Configurações de Rede
    private int portaLocal = 8080;

    //Configurações de NSD
    private String nomeServico = "ServidorCommunityDoodle";
    public String tipoServico = "_http._tcp.";

    //Gerenciador de registro
    Context context;
    NsdManager nsdManager;

    //Listeners de registro
    NsdManager.RegistrationListener registrationListener;

    public Servidor(Context context)
    {
        this.context = context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        Log.d(refLog, "Criado instancia de Servidor");
    }

    public void rodar() throws IOException {
        initializeRegistrationListener();
        registrarServico(portaLocal);
    }


    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Está sendo setado pois se na rede já existir serviço com
                // mesmo nome, o Android renomeia para "nome (1)", "nome (2)" etc
                nomeServico = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            }

        };
        Log.d(refLog, "Iniciador registrationListener para Servidor");
    }

    public void registrarServico(int porta) {
        // Cria um novo objeto NsdServiceInfo
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(nomeServico);
        serviceInfo.setServiceType(tipoServico);
        serviceInfo.setPort(portaLocal);

        //Registra na rede
        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        Log.d(refLog, "Registrado serviço");
    }

}