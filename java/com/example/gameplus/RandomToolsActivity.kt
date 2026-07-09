package com.example.gameplus

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class RandomToolsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_tools)

        val btnMoneta = findViewById<Button>(R.id.btn_lancia_moneta)
        val txtMoneta = findViewById<TextView>(R.id.txt_risultato_moneta)

        // Logica Moneta
        btnMoneta.setOnClickListener {
            val testa = Random.nextBoolean()
            txtMoneta.text = if (testa) "TESTA" else "CROCE"
        }
    }
}