package com.example.gameplus

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ObbligoVeritaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_obbligo_verita)

        val btnClassic = findViewById<Button>(R.id.btn_mode_classic)
        val btnParty = findViewById<Button>(R.id.btn_mode_party)
        val btnHard = findViewById<Button>(R.id.btn_mode_hard)

        btnClassic.setOnClickListener { avviaGioco("Classico") }
        btnParty.setOnClickListener { avviaGioco("Party") }
        btnHard.setOnClickListener {
            startActivity(Intent(this, LoginEstremoActivity::class.java))
        }
    }

    private fun avviaGioco(modalita: String) {
        val intent = Intent(this, GamePlayActivity::class.java).apply {
            putExtra("MODALITA", modalita) // Passa la modalità scelta
        }
        startActivity(intent)
    }
}