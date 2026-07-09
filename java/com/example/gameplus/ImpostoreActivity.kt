package com.example.gameplus

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ImpostoreActivity : AppCompatActivity() {

    private val listaParole = arrayOf(
        "Hamburger", "Gelato", "Cioccolato", "Lasagna", "Caffè", "Panettone", "Kebab", "Mortadella", "Nutella", "Popcorn",
        "Spaghetti", "Biscotto", "Formaggio", "Torta", "Arancia", "Banana", "Vino", "Birra", "Miele", "Patatine",
        "Roma", "Milano", "Londra", "New York", "Giappone", "Egitto", "Spiaggia", "Montagna", "Ospedale", "Aeroporto",
        "Stazione", "Biblioteca", "Colosseo", "Deserto", "Giungla", "Piscina", "Bosco", "Castello", "Museo", "Stadio",
        "Cane", "Leone", "Elefante", "Giraffa", "Squalo", "Delfino", "Pinguino", "Tigre", "Scimmia", "Farfalla",
        "Pappagallo", "Zanzara", "Serpente", "Cavallo", "Pecora", "Gallina", "Aquila", "Balena", "Criceto", "Lupo",
        "Zaino", "Martello", "Ombrello", "Chitarra", "Specchio", "Occhiali", "Orologio", "Portafoglio", "Lampada", "Cuscino",
        "Forbici", "Divano", "Quadro", "Computer", "Frigorifero", "Bicicletta", "Candela", "Palla", "Pennello", "Radio",
        "Dottore", "Poliziotto", "Cuoco", "Pompiere", "Calciatore", "Pittore", "Meccanico", "Pilota", "Insegnante", "Avvocato",
        "Cantante", "Fotografo", "Giardiniere", "Attore", "Scrittore", "Ballerino", "Sarto", "Mago", "Marinaio", "Fidanzato",
        "Amore", "Sogno", "Vittoria", "Natale", "Musica", "Soldi", "Guerra", "Silenzio", "Fuoco", "Pioggia"
    )

    private lateinit var parolaScelta: String
    private lateinit var nomeImpostore: String
    private var nomiInGioco = ArrayList<String>()

    // Companion object serve per le variabili "statiche"
    companion object {
        private val punteggiGlobali = HashMap<String, Int>()
    }

    private var indexCorrente = 0
    private var parolaRivelata = false
    private var primoGiroRound = true

    private lateinit var txtGiocatore: TextView
    private lateinit var txtParola: TextView
    private lateinit var txtClassifica: TextView
    private lateinit var editNomi: EditText
    private lateinit var layoutSetup: LinearLayout
    private lateinit var layoutGioco: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_impostore)

        txtGiocatore = findViewById(R.id.txt_giocatore_num)
        txtParola = findViewById(R.id.txt_parola_segreta)
        txtClassifica = findViewById(R.id.txt_classifica)
        editNomi = findViewById(R.id.edit_nomi_giocatori)
        layoutSetup = findViewById(R.id.layout_setup_nomi)
        layoutGioco = findViewById(R.id.layout_gioco)

        val btnProssimo = findViewById<Button>(R.id.btn_prossimo)
        val btnConferma = findViewById<Button>(R.id.btn_conferma_nomi)

        btnConferma.setOnClickListener { setupPartita() }
        btnProssimo.setOnClickListener { gestisciTurno() }
        txtParola.setOnClickListener { rivelaParola() }
    }

    private fun setupPartita() {
        val input = editNomi.text.toString()
        if (input.trim().isEmpty()) {
            Toast.makeText(this, "Inserisci almeno 3 nomi!", Toast.LENGTH_SHORT).show()
            return
        }

        val split = input.split(",")
        punteggiGlobali.clear()
        for (s in split) {
            val nome = s.trim()
            if (nome.isNotEmpty()) punteggiGlobali[nome] = 0
        }

        if (punteggiGlobali.size < 3) {
            Toast.makeText(this, "Servono almeno 3 giocatori!", Toast.LENGTH_SHORT).show()
            return
        }

        mostraRegoleEInizia()
    }

    private fun mostraRegoleEInizia() {
        AlertDialog.Builder(this)
            .setTitle("Regole dell'Impostore")
            .setMessage("OBIETTIVO:\nI Civili devono trovare l'Impostore. L'Impostore deve sopravvivere.\n\nPUNTEGGI:\n" +
                    "✅ +1 punto ai Civili superstiti se vincono.\n" +
                    "❌ -1 punto a chi viene eliminato per errore.\n" +
                    "🏆 +3 punti all'Impostore se vince (rimangono in 2).\n\n" +
                    "ATTENZIONE: Vedrete la parola solo al primo turno!")
            .setPositiveButton("INIZIA") { _, _ -> nuovoRound() }
            .setCancelable(false)
            .show()
    }

    private fun nuovoRound() {
        nomiInGioco = ArrayList(punteggiGlobali.keys)

        parolaScelta = listaParole.random()
        nomeImpostore = nomiInGioco.random()

        primoGiroRound = true
        indexCorrente = 0
        parolaRivelata = false

        layoutSetup.visibility = View.GONE
        layoutGioco.visibility = View.VISIBLE

        aggiornaUI()
        aggiornaClassificaUI()
    }

    private fun rivelaParola() {
        if (parolaRivelata || !primoGiroRound) return

        val giocatoreAttuale = nomiInGioco[indexCorrente]
        if (giocatoreAttuale == nomeImpostore) {
            txtParola.text = "SEI L'IMPOSTORE!"
            txtParola.setTextColor(Color.RED)
        } else {
            txtParola.text = parolaScelta
            txtParola.setTextColor(Color.GREEN)
        }
        parolaRivelata = true
    }

    private fun gestisciTurno() {
        if (!parolaRivelata && primoGiroRound) {
            Toast.makeText(this, "Tocca il riquadro per vedere la parola!", Toast.LENGTH_SHORT).show()
            return
        }

        indexCorrente++

        if (indexCorrente < nomiInGioco.size) {
            parolaRivelata = false
            aggiornaUI()
        } else {
            primoGiroRound = false
            avviaVotazione()
        }
    }

    private fun avviaVotazione() {
        txtParola.text = "DISCUTETE E VOTATE!"
        txtParola.setTextColor(Color.YELLOW)
        txtGiocatore.text = "Fase di Votazione"

        val vivi = nomiInGioco.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Chi volete eliminare?")
            .setItems(vivi) { _, which -> processaVoto(vivi[which]) }
            .setCancelable(false)
            .show()
    }

    private fun processaVoto(votato: String) {
        if (votato == nomeImpostore) {
            // Vittoria Civili
            for (n in nomiInGioco) {
                if (n != nomeImpostore) aggiungiPunto(n, 1)
            }
            fineRoundDialog("CIVILI VINCITORI! L'impostore era $nomeImpostore")
        } else {
            // Errore
            aggiungiPunto(votato, -1)
            nomiInGioco.remove(votato)

            if (nomiInGioco.size <= 2) {
                // Vittoria Impostore
                aggiungiPunto(nomeImpostore, 3)
                fineRoundDialog("L'IMPOSTORE HA VINTO! $nomeImpostore vi ha ingannati.")
            } else {
                Toast.makeText(this, "$votato era innocente ed è fuori. Continuate a discutere!", Toast.LENGTH_LONG).show()
                aggiornaClassificaUI()
                avviaVotazione()
            }
        }
    }

    private fun aggiungiPunto(nome: String, punti: Int) {
        if (punteggiGlobali.containsKey(nome)) {
            punteggiGlobali[nome] = punteggiGlobali[nome]!! + punti
        }
    }

    private fun fineRoundDialog(messaggio: String) {
        aggiornaClassificaUI()
        AlertDialog.Builder(this)
            .setTitle("Round Concluso")
            .setMessage("$messaggio\n\nCosa volete fare?")
            .setPositiveButton("Prossimo Round") { _, _ -> nuovoRound() }
            .setNegativeButton("Reset Partita") { _, _ ->
                layoutGioco.visibility = View.GONE
                layoutSetup.visibility = View.VISIBLE
            }
            .setCancelable(false)
            .show()
    }

    private fun aggiornaUI() {
        txtGiocatore.text = "Giocatore: ${nomiInGioco[indexCorrente]}"
        txtParola.text = "TOCCA PER VEDERE"
        txtParola.setTextColor(Color.WHITE)
    }

    private fun aggiornaClassificaUI() {
        val sb = java.lang.StringBuilder("CLASSIFICA PUNTI:\n")
        for ((key, value) in punteggiGlobali) {
            sb.append("• ").append(key).append(": ").append(value).append("\n")
        }
        txtClassifica.text = sb.toString()
    }
}