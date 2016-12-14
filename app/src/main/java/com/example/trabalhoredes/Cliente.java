package com.example.trabalhoredes;

/**
 * Created by daniel on 13/12/2016.
 */


import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Cliente {

    //Referência para logging
    private static final String refLog = "Cliente: ";

    //Configurações de Rede
    private String ipLocal;
    private int portaLocal;
    private String ipServidor;
    private int portaServidor;

    //Lista de servidores disponíveis
    List<String> conexoesDisponiveis = new ArrayList<String>();

    //Configurações de NSD
    private String nomeServico = "ClienteCommunityDoodle";
    public String tipoServico = "_http._tcp.";

    //Gerenciador de registro
    Context context;
    NsdManager nsdManager;

    //Listeners de registro
    ResolveListener resolveListener;
    DiscoveryListener discoveryListener;

    public Cliente(Context context, NsdManager nsdManager, String ipLocal)
    {
        this.ipLocal = ipLocal;
        this.context = context;
        this.nsdManager = nsdManager;
        Log.d(refLog, "Criado instancia de Cliente");
    }

    public void rodar() throws IOException
    {
        //initialize Resolve Listener
        initializeResolveListener();
        //Initialize Discovery Listener
        initializeDiscoveryListener();
    }

    public void initializeResolveListener() {
        resolveListener = new ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails.  Use the error code to debug.
                Log.d(refLog, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(refLog, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(nomeServico)) {
                    //Nunca vai cair aqui meu filho
                    Log.d(refLog, "Same IP.");
                    return;
                }
                int port = serviceInfo.getPort();
                InetAddress host = serviceInfo.getHost();
                Log.d(refLog, "Resolvido. "+ host.getHostAddress()+":"+port);
                conexoesDisponiveis.add(host.getHostAddress()+":"+port);
            }
        };
        Log.d(refLog, "Resolver Listener instanciado");
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(refLog, "Iniciado busca de servicos");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(refLog, "Sucesso! Servico encontrado " + service);
                if (!service.getServiceType().equals(tipoServico)) {
                    Log.d(refLog, "Tipo de servico não conhecido: " + service.getServiceType());
                } else if (service.getServiceName().equals(nomeServico)) {
                    // Nunca vai cair aqui pois nessa versão cliente não está registrado
                    // como serviço
                    Log.d(refLog, "Servico da mesma máquina:" + nomeServico);
                } else if (service.getServiceName().contains("ServidorCommunityDoodle")){
                    nsdManager.resolveService(service, resolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.d(refLog, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.d(refLog, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(refLog, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.d(refLog, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
        Log.d(refLog, "Discovery Listener instanciado");
    }

    public void descobrirServidores() {
        nsdManager.discoverServices(tipoServico, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
        Log.d(refLog, "Fim de execução de descobrirServidores()");
    }

    public void pararBusca() {
        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    public List<String> getListaServidores() {
        return conexoesDisponiveis;
    }

}