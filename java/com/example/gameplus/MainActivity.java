package com.example.gameplus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnTris, btnSudoku, btnImpostore, btnDama, btnObbligoVerita,
            btnImpiccato, btnTapChallenge, btnFlappyBird,
            btnIndovinaParola, btnRandomTools, btnScegliNumero, btnCharades, btnRicco, btnFanta;

    private Button btnUser;
    private EditText searchBar;
    private GridLayout mainGrid;

    private ImageButton[] tuttiIBottoni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inizializzazione componenti UI
        searchBar = <EditText>findViewById(R.id.search_bar);
        mainGrid = <GridLayout>findViewById(R.id.main_grid);
        btnUser = <Button>findViewById(R.id.btn_user);

        btnTris = <ImageButton>findViewById(R.id.btn_tris);
        btnSudoku = <ImageButton>findViewById(R.id.btn_sudoku);
        btnImpostore = <ImageButton>findViewById(R.id.btn_impostore);
        btnDama = <ImageButton>findViewById(R.id.btn_dama);
        btnObbligoVerita = <ImageButton>findViewById(R.id.btn_obbligo_verita);
        btnImpiccato = <ImageButton>findViewById(R.id.btn_impiccato);
        btnTapChallenge = <ImageButton>findViewById(R.id.btn_tap_challenge);
        btnFlappyBird = <ImageButton>findViewById(R.id.btn_flappy_bird);
        btnIndovinaParola = <ImageButton>findViewById(R.id.btn_indovina_parola);
        btnRandomTools = <ImageButton>findViewById(R.id.btn_random_tools);
        btnScegliNumero = <ImageButton>findViewById(R.id.btn_scegli_numero);
        btnCharades = <ImageButton>findViewById(R.id.btn_charades);
        btnRicco = <ImageButton>findViewById(R.id.btn_ricco);
        btnFanta = <ImageButton>findViewById(R.id.btn_fanta);

        // Riempimento array per la ricerca
        tuttiIBottoni = new ImageButton[]{
                btnTris, btnSudoku, btnImpostore, btnDama,
                btnObbligoVerita, btnImpiccato, btnTapChallenge, btnFlappyBird,
                btnIndovinaParola, btnRandomTools, btnScegliNumero, btnCharades, btnRicco, btnFanta
        };

        // Adatta la griglia in base alla larghezza schermo
        int screenWidthDp = getResources().getConfiguration().screenWidthDp;
        mainGrid.setColumnCount(screenWidthDp >= 600 ? 4 : 2);

        // --- Logica di Navigazione Giochi Standard ---
        btnTris.setOnClickListener(v -> startActivity(new Intent(this, TrisActivity.class)));
        btnSudoku.setOnClickListener(v -> startActivity(new Intent(this, SudokuActivity.class)));
        btnImpostore.setOnClickListener(v -> startActivity(new Intent(this, ImpostoreActivity.class)));
        btnDama.setOnClickListener(v -> startActivity(new Intent(this, DamaActivity.class)));
        btnObbligoVerita.setOnClickListener(v -> startActivity(new Intent(this, ObbligoVeritaActivity.class)));
        btnImpiccato.setOnClickListener(v -> startActivity(new Intent(this, ImpiccatoActivity.class)));
        btnTapChallenge.setOnClickListener(v -> startActivity(new Intent(this, TapChallengeActivity.class)));
        btnFlappyBird.setOnClickListener(v -> startActivity(new Intent(this, FlappyBirdActivity.class)));
        btnRandomTools.setOnClickListener(v -> startActivity(new Intent(this, RandomToolsActivity.class)));
        btnScegliNumero.setOnClickListener(v -> startActivity(new Intent(this, ScegliNumeroActivity.class)));
        btnRicco.setOnClickListener(v -> startActivity(new Intent(this, ChiVuolEssereRiccoActivity.class)));

        // --- GIOCHI DI PAROLE UNIFICATI (Usano GenericWordGameActivity) ---
        btnIndovinaParola.setOnClickListener(v -> {
            String[] paroleIndovina = {
                    "PIZZA", "CALCIO", "ELEFANTE", "CHITARRA", "CINEMA",
                    "TELEFONO", "SPAGHETTI", "BATTERIA", "DENTISTA",
                    "SCUOLA", "GIUNGLA", "AEREO", "BICICLETTA", "GATTO"
            };
            Intent i = new Intent(this, GenericWordGameActivity.class);
            i.putExtra("ARRAY_PAROLE", paroleIndovina);
            i.putExtra("TITOLO_GIOCO", "Indovina la Parola");
            startActivity(i);
        });

        btnCharades.setOnClickListener(v -> {
            String[] azioniCharades = {
                    "CUCINARE UNA PIZZA", "CAMBIARE UNA LAMPADINA", "SCIARE",
                    "LAVARSI I DENTI", "GIOCARE A TENNIS", "GUIDARE UN BUS",
                    "PESCARE", "DIPINGERE UN QUADRO", "FARE IL CAFFÈ",
                    "SUONARE IL VIOLINO", "SCATTARE UN SELFIE"
            };
            Intent i = new Intent(this, GenericWordGameActivity.class);
            i.putExtra("ARRAY_PAROLE", azioniCharades);
            i.putExtra("TITOLO_GIOCO", "Mimo (Charades)");
            startActivity(i);
        });

        // --- Navigazione FantaPlus ---
        btnFanta.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("SpottioPrefs", MODE_PRIVATE);
            String usernameSalvato = prefs.getString("username_attivo", "Guest");

            if ("Guest".equals(usernameSalvato)) {
                Toast.makeText(this, "Devi effettuare il login per accedere a FantaPlus!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, FantaLoginActivity.class));
            } else {
                startActivity(new Intent(this, HomeFantaActivity.class));
            }
        });

        // --- Logica Tasto Utente (Profilo / Login) ---
        btnUser.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FantaLoginActivity.class)));

        // --- Logica della Barra di Ricerca ---
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase();
                if (tuttiIBottoni == null) return;

                for (ImageButton btn : tuttiIBottoni) {
                    if (btn == null) continue;
                    String contentDesc = btn.getContentDescription() != null ? btn.getContentDescription().toString().toLowerCase() : "";

                    // Mostra o nasconde i bottoni in base alla ricerca
                    btn.setVisibility(contentDesc.contains(query) ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recupero il nome dell'utente loggato dalle SharedPreferences
        SharedPreferences prefs = getSharedPreferences("SpottioPrefs", MODE_PRIVATE);
        String usernameSalvato = prefs.getString("username_attivo", "Guest");

        // Imposta il nome sul pulsante in alto a destra
        if (btnUser != null) {
            btnUser.setText(usernameSalvato);
        }
    }
}