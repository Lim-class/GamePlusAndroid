package com.example.gameplus

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ImpiccatoActivity : AppCompatActivity() {

    private lateinit var parolaSegreta: String
    private lateinit var parolaVisualizzata: CharArray
    private var errori = 0
    private val MAX_ERRORI = 6
    private val dizionario = arrayOf("ANDROID", "JAVA", "PROGRAMMAZIONE", "STUDIO", "GIOCO", "COMPUTER", "SMARTPHONE")

    private lateinit var txtParola: TextView
    private lateinit var txtErrori: TextView
    private lateinit var editLettera: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impiccato)

        txtParola = findViewById(R.id.txt_parola_nascosta)
        txtErrori = findViewById(R.id.txt_errori)
        editLettera = findViewById(R.id.edit_lettera)
        val btnTenta = findViewById<Button>(R.id.btn_tenta)

        // All'avvio chiediamo la modalità
        scegliModalita()

        btnTenta.setOnClickListener {
            val input = editLettera.text.toString().uppercase().trim()
            if (input.isNotEmpty()) {
                controllaLettera(input[0])
                editLettera.setText("")
            }
        }
    }

    private fun scegliModalita() {
        val opzioni = arrayOf("1 Giocatore (PC)", "2 Giocatori (Locale)")

        AlertDialog.Builder(this)
            .setTitle("Seleziona Modalità")
            .setCancelable(false)
            .setItems(opzioni) { _, which ->
                if (which == 0) {
                    nuovaPartitaSingola()
                } else {
                    chiediParolaGiocatore()
                }
            }
            .show()
    }

    private fun nuovaPartitaSingola() {
        parolaSegreta = dizionario.random() // <-- Kotlin power!
        inizializzaGioco(parolaSegreta)
    }

    private fun chiediParolaGiocatore() {
        val inputParola = EditText(this).apply {
            hint = "Inserisci la parola da indovinare"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(this)
            .setTitle("Giocatore 1")
            .setMessage("Inserisci una parola per il tuo amico:")
            .setView(inputParola)
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                val p = inputParola.text.toString().uppercase().trim()
                if (p.isNotEmpty()) {
                    parolaSegreta = p
                    inizializzaGioco(parolaSegreta)
                } else {
                    nuovaPartitaSingola()
                }
            }
            .show()
    }

    private fun inizializzaGioco(parola: String) {
        // Inizializza il CharArray mappando ogni carattere (spazio o trattino)
        parolaVisualizzata = CharArray(parola.length) { i ->
            if (parola[i] == ' ') ' ' else '_'
        }
        errori = 0
        aggiornaUI()
    }

    private fun controllaLettera(c: Char) {
        var trovata = false
        for (i in parolaSegreta.indices) {
            if (parolaSegreta[i] == c) {
                parolaVisualizzata[i] = c
                trovata = true
            }
        }

        if (!trovata) errori++

        aggiornaUI()
        verificaFineGioco()
    }

    private fun aggiornaUI() {
        // Unisce i caratteri con uno spazio in mezzo per renderlo leggibile
        txtParola.text = parolaVisualizzata.joinToString(" ").trim()

        val viteRimaste = MAX_ERRORI - errori
        val cuori = buildString {
            append("Vite: ")
            for (i in 0 until MAX_ERRORI) {
                if (i < viteRimaste) append("❤️ ") else append("🖤 ")
            }
        }
        txtErrori.text = cuori
    }

    private fun verificaFineGioco() {
        val statoAttuale = String(parolaVisualizzata)
        if (statoAttuale == parolaSegreta) {
            mostraDialogoFinale("🎉 VITTORIA!", "Ottimo lavoro! La parola era: $parolaSegreta")
        } else if (errori >= MAX_ERRORI) {
            mostraDialogoFinale("💀 GAME OVER", "Peccato! La parola era: $parolaSegreta")
        }
    }

    private fun mostraDialogoFinale(titolo: String, messaggio: String) {
        AlertDialog.Builder(this)
            .setTitle(titolo)
            .setMessage(messaggio)
            .setCancelable(false)
            .setPositiveButton("MENU PRINCIPALE") { _, _ -> scegliModalita() }
            .show()
    }
}