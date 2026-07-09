package com.example.gameplus

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TapChallengeActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnTap: Button
    private lateinit var btnRestart: Button

    private var score = 0
    private var gameActive = false
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tap_challenge)

        tvTimer = findViewById(R.id.tv_timer)
        tvScore = findViewById(R.id.tv_score)
        btnTap = findViewById(R.id.btn_tap)
        btnRestart = findViewById(R.id.btn_restart)

        btnTap.setOnClickListener {
            if (!gameActive) {
                startGame()
            }
            if (gameActive) {
                score++
                tvScore.text = "Punti: $score"
            }
        }

        btnRestart.setOnClickListener { resetGame() }
    }

    private fun startGame() {
        gameActive = true
        score = 0
        tvScore.text = "Punti: 0"
        btnRestart.visibility = View.GONE

        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Tempo: ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                gameActive = false
                tvTimer.text = "Tempo scaduto!"
                btnTap.isEnabled = false
                btnRestart.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun resetGame() {
        btnTap.isEnabled = true
        tvTimer.text = "Tempo: 10s"
        tvScore.text = "Punti: 0"
        btnRestart.visibility = View.GONE
    }
}