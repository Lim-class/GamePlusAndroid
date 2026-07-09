package com.example.gameplus

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class FlappyBirdActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nasconde la barra superiore per il gioco
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val gameView = FlappyBirdView(this)
        setContentView(gameView)
    }
}