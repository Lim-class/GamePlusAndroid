package com.example.gameplus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nasconde la barra di stato per il pieno schermo
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContentView(R.layout.activity_splash)

        // Nasconde l'ActionBar se presente
        supportActionBar?.hide()

        val logo = findViewById<ImageView>(R.id.img_logo)

        // Animazione di entrata
        logo.alpha = 0f
        logo.animate().alpha(1f).setDuration(1500).start()

        // Passaggio alla MainActivity dopo 3 secondi
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }, 3000)
    }
}