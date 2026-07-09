package com.example.gameplus

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ChiVuolEssereRiccoActivity : AppCompatActivity() {

    private lateinit var editEtaPartenza: EditText
    private lateinit var spinnerPaese: Spinner
    private lateinit var txtDescrizionePaese: TextView
    private lateinit var groupObiettivo: RadioGroup
    private var paeseSelezionato = "Italia"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chi_vuol_essere_ricco)

        editEtaPartenza = findViewById(R.id.edit_eta_partenza)
        spinnerPaese = findViewById(R.id.spinner_paese)
        txtDescrizionePaese = findViewById(R.id.txt_descrizione_paese)
        groupObiettivo = findViewById(R.id.group_objective)

        setupSpinner()

        findViewById<Button>(R.id.btn_inizia_scalata).setOnClickListener { avviaGioco() }
    }

    private fun setupSpinner() {
        val paesi = arrayOf("Italia", "Germania", "Delaware (USA)", "Mercato Emergente")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paesi)
        spinnerPaese.adapter = adapter

        spinnerPaese.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                paeseSelezionato = parent.getItemAtPosition(position).toString()
                aggiornaDescrizionePaese(paeseSelezionato)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun aggiornaDescrizionePaese(paese: String) {
        val desc = when (paese) {
            "Italia" -> "IRPEF Progressiva. BTP tassati al 12.5%."
            "Germania" -> "Tasse alte ma servizi top. Capital gain 25%."
            "Delaware (USA)" -> "No tasse statali, ma costo della vita estremo."
            else -> "Mercato selvaggio. Alto rischio, tasse casuali."
        }
        txtDescrizionePaese.text = desc
    }

    private fun avviaGioco() {
        val etaS = editEtaPartenza.text.toString()
        if (etaS.isEmpty()) {
            Toast.makeText(this, "Inserisci la tua età!", Toast.LENGTH_SHORT).show()
            return
        }

        val obiettivo: Long = when (groupObiettivo.checkedRadioButtonId) {
            R.id.radio_1mln -> 1_000_000L
            R.id.radio_1mld -> 1_000_000_000L
            R.id.radio_1bil -> 1_000_000_000_000L
            else -> {
                Toast.makeText(this, "Seleziona un obiettivo!", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("ETA_PARTENZA", etaS.toInt())
            putExtra("PAESE", paeseSelezionato)
            putExtra("OBIETTIVO", obiettivo)
        }

        startActivity(intent)
    }
}