package com.example.trabalhoredes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.id.button3;

public class MainActivity extends AppCompatActivity {

    Button buttonConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> listaNegra = new ArrayList<String>();

        Log.d("MainActivity", "Seu dispositivo: "+ android.os.Build.MODEL);

        if (false)
        {
            buttonConnect = (Button) findViewById(R.id.button3);
            buttonConnect.setEnabled(true);
        }
    }

    // Vai para a activity que configura server de partida
    public void criaPartida(View view) {
        Intent intent = new Intent(this, ServidorActivity.class);
        startActivity(intent);
    }

    // Vai para a activity que configura cliente de partida
    public void conectaPartida(View view) {
        Intent intent = new Intent(this, ClienteActivity.class);
        startActivity(intent);
    }

    // Vai para a activity que busca servidores
    public void buscaServidores(View view) {
        Intent intent = new Intent(this, BuscaServidoresActivity.class);
        startActivity(intent);
    }
}
