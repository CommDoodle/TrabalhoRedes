package com.example.trabalhoredes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
