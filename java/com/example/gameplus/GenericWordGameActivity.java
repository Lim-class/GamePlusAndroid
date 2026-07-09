package com.example.gameplus;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenericWordGameActivity extends AppCompatActivity {

    private TextView tvParola;
    private List<String> mazzo;
    private int indiceCorrente = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usiamo un singolo layout per entrambi i giochi
        setContentView(R.layout.activity_indovina_parola);

        tvParola = findViewById(R.id.tvParolaDaIndovinare);
        Button btnProssima = findViewById(R.id.btnProssima);

        // Recuperiamo i dati dall'Intent (da MainActivity)
        String[] arrayParole = getIntent().getStringArrayExtra("ARRAY_PAROLE");
        String titoloGioco = getIntent().getStringExtra("TITOLO_GIOCO");

        if (getSupportActionBar() != null && titoloGioco != null) {
            getSupportActionBar().setTitle(titoloGioco);
        }

        if (arrayParole == null) arrayParole = new String[]{"ERRORE"};

        mazzo = new ArrayList<>(Arrays.asList(arrayParole));
        Collections.shuffle(mazzo);

        aggiornaParola();

        btnProssima.setOnClickListener(v -> {
            indiceCorrente++;
            if (indiceCorrente >= mazzo.size()) {
                indiceCorrente = 0;
                Collections.shuffle(mazzo); // Rimescola quando finisce
            }
            aggiornaParola();
        });
    }

    private void aggiornaParola() {
        tvParola.setText(mazzo.get(indiceCorrente));
    }
}