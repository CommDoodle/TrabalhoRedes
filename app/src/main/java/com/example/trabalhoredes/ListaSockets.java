package com.example.trabalhoredes;

import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by daniel on 14/12/2016.
 */

public class ListaSockets {
    ArrayList<Socket> minhasConexoes = new ArrayList<Socket>();

    public synchronized  ArrayList<Socket> getLista()
    {
        return this.minhasConexoes;
    }

    public synchronized Void setLista(ArrayList<Socket> lista)
    {
        this.minhasConexoes = lista;
        return null;
    }

    public synchronized Socket get(int i)
    {
        return this.minhasConexoes.get(i);
    }

    public synchronized Void remove(int i)
    {
        this.minhasConexoes.remove(i);
        return null;
    }

    public synchronized Void add(Socket socket)
    {
        this.minhasConexoes.add(socket);
        return null;
    }



}
