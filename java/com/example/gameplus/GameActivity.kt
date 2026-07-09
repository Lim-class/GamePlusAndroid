package com.example.gameplus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var engine: GameEngine

    private lateinit var txtLiquidita: TextView
    private lateinit var txtValorePortafoglio: TextView
    private lateinit var txtTassePagate: TextView
    private lateinit var txtEta: TextView
    private lateinit var txtStipendio: TextView
    private lateinit var txtAssetPrezzo: TextView
    private lateinit var txtAssetPosseduti: TextView
    private lateinit var txtMaxAcquistabili: TextView
    private lateinit var lblLiquidita: TextView
    private lateinit var editQuantitaAsset: EditText
    private lateinit var spinnerStrumenti: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        inizializzaEngine()
        collegaUI()
        setupListeners()
        aggiornaDashboard()
    }

    private fun inizializzaEngine() {
        val etaPartenza = intent.getIntExtra("ETA_PARTENZA", 18)
        val paeseSelezionato = intent.getStringExtra("PAESE") ?: "Italia"
        val obiettivoFinanziario = intent.getLongExtra("OBIETTIVO", 1000000L)

        engine = GameEngine(etaPartenza, paeseSelezionato, obiettivoFinanziario)
    }

    private fun collegaUI() {
        lblLiquidita = findViewById(R.id.lbl_liquidita)
        txtLiquidita = findViewById(R.id.txt_liquidita)
        txtValorePortafoglio = findViewById(R.id.txt_valore_portafoglio)
        txtTassePagate = findViewById(R.id.txt_tasse_pagate)
        txtEta = findViewById(R.id.txt_eta)
        txtStipendio = findViewById(R.id.txt_stipendio)
        txtAssetPrezzo = findViewById(R.id.txt_asset_prezzo)
        txtAssetPosseduti = findViewById(R.id.txt_asset_posseduti)
        txtMaxAcquistabili = findViewById(R.id.txt_max_acquistabili)
        editQuantitaAsset = findViewById(R.id.edit_quantita_asset)
        spinnerStrumenti = findViewById(R.id.spinner_strumenti_finanziari)

        // Estrae comodamente solo il nome di ogni asset per passarlo allo spinner
        val nomiAssets = engine.marketManager.assets.map { it.nome }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nomiAssets)
        spinnerStrumenti.adapter = adapter
    }

    private fun setupListeners() {
        spinnerStrumenti.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                aggiornaDettaglioAsset()
            }
            override fun onNothingSelected(p: AdapterView<*>) {}
        }

        findViewById<Button>(R.id.btn_cerca_lavoro).setOnClickListener { gestisciTurno(engine.lavoraSemestre()) }
        findViewById<Button>(R.id.btn_startup).setOnClickListener { gestisciTurno(engine.tentaStartup()) }
        findViewById<Button>(R.id.btn_dettaglio_portafoglio).setOnClickListener { mostraPortafoglio() }
        findViewById<Button>(R.id.btn_investi_asset).setOnClickListener { eseguiAcquisto() }
        findViewById<Button>(R.id.btn_vendi_asset).setOnClickListener { eseguiVendita() }
    }

    private fun eseguiAcquisto() {
        val qS = editQuantitaAsset.text.toString()
        if (qS.isEmpty()) return

        // Prova a convertire, se fallisce restituisce null evitando eccezioni fatali
        val q = qS.toLongOrNull()
        if (q != null) {
            val index = spinnerStrumenti.selectedItemPosition
            if (engine.compraAsset(index, q)) {
                editQuantitaAsset.setText("")
                Toast.makeText(this, "Acquisto effettuato!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Liquidità insufficiente!", Toast.LENGTH_SHORT).show()
            }
            aggiornaDashboard()
            aggiornaDettaglioAsset()
        } else {
            Toast.makeText(this, "Valore non valido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun eseguiVendita() {
        val qS = editQuantitaAsset.text.toString()
        if (qS.isEmpty()) return

        val q = qS.toLongOrNull()
        if (q != null) {
            val index = spinnerStrumenti.selectedItemPosition
            val tasse = engine.vendiAsset(index, q)

            if (tasse >= 0) {
                editQuantitaAsset.setText("")
                Toast.makeText(this, "Vendita eseguita. Tasse trattenute: ${formatCurrency(tasse)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Non ne possiedi abbastanza!", Toast.LENGTH_SHORT).show()
            }
            aggiornaDashboard()
            aggiornaDettaglioAsset()
        } else {
            Toast.makeText(this, "Valore non valido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun gestisciTurno(messaggioEsito: String) {
        Toast.makeText(this, messaggioEsito, Toast.LENGTH_SHORT).show()
        aggiornaDashboard()
        aggiornaDettaglioAsset()
        controllaFineGioco()
    }

    private fun aggiornaDettaglioAsset() {
        val pos = spinnerStrumenti.selectedItemPosition
        if (pos < 0) return

        val a = engine.marketManager.getAsset(pos)
        txtAssetPrezzo.text = "Prezzo: ${formatCurrency(a.prezzoCorrente)}"
        txtAssetPosseduti.text = "Tuoi: ${a.quantitaPosseduta}"

        val maxBuy = if (engine.liquidita > 0) (engine.liquidita / a.prezzoCorrente).toLong() else 0L
        txtMaxAcquistabili.text = "Max: $maxBuy"
    }

    private fun aggiornaDashboard() {
        val liquidita = engine.liquidita
        if (liquidita >= 0) {
            lblLiquidita.text = "DISPONIBILITÀ"
            txtLiquidita.setTextColor(Color.parseColor("#52BE80")) // VERDE
        } else {
            lblLiquidita.text = "DEBITO BANCA"
            txtLiquidita.setTextColor(Color.parseColor("#C0392B")) // ROSSO
        }

        txtLiquidita.text = formatCurrency(liquidita)
        txtValorePortafoglio.text = formatCurrency(engine.marketManager.getValoreTotalePortafoglio())
        txtTassePagate.text = formatCurrency(engine.tasseUltimoSemestre)
        txtEta.text = "Età: ${(engine.mesiTotaliDiVita / 12).toInt()}"
        txtStipendio.text = "${engine.titoloLavoro}: ${formatCurrency(engine.stipendioAnnuo)}"
    }

    private fun controllaFineGioco() {
        when (engine.controllaStatoGioco()) {
            StatoGioco.VITTORIA -> mostraFine("VITTORIA!", "Obiettivo raggiunto! Patrimonio: ${formatCurrency(engine.patrimonioTotale)}")
            StatoGioco.VECCHIAIA -> mostraFine("FINE GIOCO", "Sei arrivato a 100 anni.")
            StatoGioco.BANCAROTTA -> mostraFine("BANCAROTTA", "Sei fallito.")
            StatoGioco.IN_CORSO -> {} // Non facciamo nulla
        }
    }

    private fun mostraPortafoglio() {
        val sb = StringBuilder()
        for (a in engine.marketManager.assets) {
            if (a.quantitaPosseduta > 0) {
                sb.append("${a.nome} (${a.tipo})\n")
                    .append("Qtà: ${a.quantitaPosseduta}\n")
                    .append("Valore: ${formatCurrency(a.getValoreTotale())}\n\n")
            }
        }
        if (sb.isEmpty()) sb.append("Portafoglio vuoto.")

        AlertDialog.Builder(this)
            .setTitle("I tuoi Asset")
            .setMessage(sb.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun mostraFine(t: String, m: String) {
        AlertDialog.Builder(this)
            .setTitle(t)
            .setMessage(m)
            .setCancelable(false)
            .setPositiveButton("Menu") { _, _ ->
                startActivity(Intent(this, ChiVuolEssereRiccoActivity::class.java))
                finish()
            }.show()
    }

    private fun formatCurrency(valore: Double): String {
        return String.format("%,.0f €", valore)
    }
}