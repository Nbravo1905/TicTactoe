package com.tictactoe.ngame.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tictactoe.ngame.R;

public class BuscarPartidaActivity extends AppCompatActivity {

    private TextView textCargando;
    private ProgressBar cargandoPartida;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_partida);

        textCargando = findViewById(R.id.textCargando);
        cargandoPartida = findViewById(R.id.cargandoPartida);

        cargandoPartida.setIndeterminate(true);
        textCargando.setText("Cargando ...");
    }
}
