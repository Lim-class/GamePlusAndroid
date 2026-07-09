package com.example.gameplus

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout

class MainActivity : AppCompatActivity() {

    private lateinit var btnTris: ImageButton
    private lateinit var btnSudoku: ImageButton
    private lateinit var btnImpostore: ImageButton
    private lateinit var btnDama: ImageButton
    private lateinit var btnObbligoVerita: ImageButton
    private lateinit var btnImpiccato: ImageButton
    private lateinit var btnTapChallenge: ImageButton
    private lateinit var btnFlappyBird: ImageButton
    private lateinit var btnIndovinaParola: ImageButton
    private lateinit var btnRandomTools: ImageButton
    private lateinit var btnScegliNumero: ImageButton
    private lateinit var btnCharades: ImageButton
    private lateinit var btnRicco: ImageButton
    private lateinit var btnFanta: ImageButton

    private lateinit var btnUser: Button
    private lateinit var searchBar: EditText
    private lateinit var mainGrid: GridLayout

    private lateinit var tuttiIBottoni: Array<ImageButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inizializzazione componenti UI
        searchBar = findViewById(R.id.search_bar)
        mainGrid = findViewById(R.id.main_grid)
        btnUser = findViewById(R.id.btn_user)

        btnTris = findViewById(R.id.btn_tris)
        btnSudoku = findViewById(R.id.btn_sudoku)
        btnImpostore = findViewById(R.id.btn_impostore)
        btnDama = findViewById(R.id.btn_dama)
        btnObbligoVerita = findViewById(R.id.btn_obbligo_verita)
        btnImpiccato = findViewById(R.id.btn_impiccato)
        btnTapChallenge = findViewById(R.id.btn_tap_challenge)
        btnFlappyBird = findViewById(R.id.btn_flappy_bird)
        btnIndovinaParola = findViewById(R.id.btn_indovina_parola)
        btnRandomTools = findViewById(R.id.btn_random_tools)
        btnScegliNumero = findViewById(R.id.btn_scegli_numero)
        btnCharades = findViewById(R.id.btn_charades)
        btnRicco = findViewById(R.id.btn_ricco)
        btnFanta = findViewById(R.id.btn_fanta)

        // Riempimento array per la ricerca
        tuttiIBottoni = arrayOf(
            btnTris, btnSudoku, btnImpostore, btnDama,
            btnObbligoVerita, btnImpiccato, btnTapChallenge, btnFlappyBird,
            btnIndovinaParola, btnRandomTools, btnScegliNumero, btnCharades, btnRicco, btnFanta
        )

        // Adatta la griglia in base alla larghezza schermo
        val screenWidthDp = resources.configuration.screenWidthDp
        mainGrid.columnCount = if (screenWidthDp >= 600) 4 else 2

        // --- Logica di Navigazione Giochi Standard ---
        btnTris.setOnClickListener { startActivity(Intent(this, TrisActivity::class.java)) }
        btnSudoku.setOnClickListener { startActivity(Intent(this, SudokuActivity::class.java)) }
        btnImpostore.setOnClickListener { startActivity(Intent(this, ImpostoreActivity::class.java)) }
        btnDama.setOnClickListener { startActivity(Intent(this, DamaActivity::class.java)) }
        btnObbligoVerita.setOnClickListener { startActivity(Intent(this, ObbligoVeritaActivity::class.java)) }
        btnImpiccato.setOnClickListener { startActivity(Intent(this, ImpiccatoActivity::class.java)) }
        btnTapChallenge.setOnClickListener { startActivity(Intent(this, TapChallengeActivity::class.java)) }
        btnFlappyBird.setOnClickListener { startActivity(Intent(this, FlappyBirdActivity::class.java)) }
        btnRandomTools.setOnClickListener { startActivity(Intent(this, RandomToolsActivity::class.java)) }
        btnScegliNumero.setOnClickListener { startActivity(Intent(this, ScegliNumeroActivity::class.java)) }
        btnRicco.setOnClickListener { startActivity(Intent(this, ChiVuolEssereRiccoActivity::class.java)) }

        // --- GIOCHI DI PAROLE UNIFICATI (Usano GenericWordGameActivity) ---
        btnIndovinaParola.setOnClickListener {
            val paroleIndovina = arrayOf(
                "PIZZA", "CALCIO", "ELEFANTE", "CHITARRA", "CINEMA",
                "TELEFONO", "SPAGHETTI", "BATTERIA", "DENTISTA",
                "SCUOLA", "GIUNGLA", "AEREO", "BICICLETTA", "GATTO"
            )
            val i = Intent(this, GenericWordGameActivity::class.java).apply {
                putExtra("ARRAY_PAROLE", paroleIndovina)
                putExtra("TITOLO_GIOCO", "Indovina la Parola")
            }
            startActivity(i)
        }

        btnCharades.setOnClickListener {
            val azioniCharades = arrayOf(
                "CUCINARE UNA PIZZA", "CAMBIARE UNA LAMPADINA", "SCIARE",
                "LAVARSI I DENTI", "GIOCARE A TENNIS", "GUIDARE UN BUS",
                "PESCARE", "DIPINGERE UN QUADRO", "FARE IL CAFFÈ",
                "SUONARE IL VIOLINO", "SCATTARE UN SELFIE"
            )
            val i = Intent(this, GenericWordGameActivity::class.java).apply {
                putExtra("ARRAY_PAROLE", azioniCharades)
                putExtra("TITOLO_GIOCO", "Mimo (Charades)")
            }
            startActivity(i)
        }

        // --- Navigazione FantaPlus ---
        btnFanta.setOnClickListener {
            val prefs = getSharedPreferences("SpottioPrefs", MODE_PRIVATE)
            val usernameSalvato = prefs.getString("username_attivo", "Guest")

            if (usernameSalvato == "Guest") {
                Toast.makeText(this, "Devi effettuare il login per accedere a FantaPlus!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, FantaLoginActivity::class.java))
            } else {
                startActivity(Intent(this, HomeFantaActivity::class.java))
            }
        }

        // --- Logica Tasto Utente (Profilo / Login) ---
        btnUser.setOnClickListener { startActivity(Intent(this@MainActivity, FantaLoginActivity::class.java)) }

        // --- Logica della Barra di Ricerca ---
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.lowercase() ?: ""
                
                // Se l'array non è stato inizializzato interrompi
                if (!::tuttiIBottoni.isInitialized) return

                for (btn in tuttiIBottoni) {
                    val contentDesc = btn.contentDescription?.toString()?.lowercase() ?: ""

                    // Mostra o nasconde i bottoni in base alla ricerca
                    btn.visibility = if (contentDesc.contains(query)) View.VISIBLE else View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        // Recupero il nome dell'utente loggato dalle SharedPreferences
        val prefs = getSharedPreferences("SpottioPrefs", MODE_PRIVATE)
        val usernameSalvato = prefs.getString("username_attivo", "Guest")

        // Imposta il nome sul pulsante in alto a destra
        // Non serve il null check perché stiamo usando lateinit var
        btnUser.text = usernameSalvato
    }
}
