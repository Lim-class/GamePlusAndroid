package com.example.gameplus

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

@SuppressLint("ViewConstructor")
class FlappyBirdView(context: Context) : View(context) {
    private var sfondo: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.sfondo)
    private var uccello: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.uccello)
    private var base: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.base)
    private var imgGameOver: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.gameover)
    private var imgTubo: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.tubo)

    private var birdX = 100f
    private var birdY = 500f
    private var velocity = 0f
    private val gravity = 2f
    private var isGameOver = false
    private var score = 0

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable { invalidate() }

    private val tubiSopra = ArrayList<Rect>()
    private val tubiSotto = ArrayList<Rect>()
    private val gap = 450
    private val velocitaTubi = 12

    private var highScore = 0
    private val prefs = context.getSharedPreferences("FlappyBirdPrefs", Context.MODE_PRIVATE)

    init {
        highScore = prefs.getInt("highscore", 0)
        inizializzaTubi()
    }

    private fun inizializzaTubi() {
        var x = 1000
        for (i in 0 until 3) {
            aggiungiTubo(x)
            x += 700 // Spazio orizzontale tra tubi
        }
    }

    private fun aggiungiTubo(x: Int) {
        val h = Random.nextInt(200, 700)
        tubiSopra.add(Rect(x, 0, x + 180, h))
        tubiSotto.add(Rect(x, h + gap, x + 180, 2500))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 1. Disegna Sfondo
        canvas.drawBitmap(sfondo, null, Rect(0, 0, width, height), null)

        if (!isGameOver) {
            // 2. Logica Gravità
            velocity += gravity
            birdY += velocity

            // Collisione con il suolo o soffitto
            if (birdY > height - 200 - uccello.height || birdY < 0) {
                isGameOver = true
            }

            // 3. Logica Tubi
            for (i in tubiSopra.indices) {
                val sopra = tubiSopra[i]
                val sotto = tubiSotto[i]

                sopra.left -= velocitaTubi
                sopra.right -= velocitaTubi
                sotto.left -= velocitaTubi
                sotto.right -= velocitaTubi

                if (imgTubo != null) {
                    // Tubo SOPRA (ruotato)
                    canvas.save()
                    canvas.rotate(180f, sopra.exactCenterX(), sopra.exactCenterY())
                    canvas.drawBitmap(imgTubo!!, null, sopra, null)
                    canvas.restore()

                    // Tubo SOTTO
                    canvas.drawBitmap(imgTubo!!, null, sotto, null)
                } else {
                    val pTubo = Paint().apply { color = Color.GREEN }
                    canvas.drawRect(sopra, pTubo)
                    canvas.drawRect(sotto, pTubo)
                }

                // Riciclo tubi
                if (sopra.right < 0) {
                    var xRecycle = 0
                    for (r in tubiSopra) {
                        if (r.left > xRecycle) xRecycle = r.left
                    }

                    val h = Random.nextInt(200, 700)
                    sopra.set(xRecycle + 700, 0, xRecycle + 700 + 180, h)
                    sotto.set(xRecycle + 700, h + gap, xRecycle + 700 + 180, 2500)

                    score++
                }

                // Collisioni
                val birdRect = Rect(birdX.toInt(), birdY.toInt(), birdX.toInt() + uccello.width, birdY.toInt() + uccello.height)
                if (Rect.intersects(birdRect, sopra) || Rect.intersects(birdRect, sotto)) {
                    isGameOver = true
                }
            }
            handler.postDelayed(runnable, 20)
        }

        // 4. Disegna Uccello e Base
        canvas.drawBitmap(uccello, birdX, birdY, null)
        canvas.drawBitmap(base, null, Rect(0, height - 200, width, height), null)

        // 5. Testo Punteggio
        val paintScore = Paint().apply {
            color = Color.WHITE
            textSize = 80f
        }
        canvas.drawText("Punti: $score", 50f, 150f, paintScore)

        // 6. Schermata Game Over
        if (isGameOver) {
            if (score > highScore) {
                highScore = score
                prefs.edit().putInt("highscore", highScore).apply()
            }

            val xGameOver = (width / 2f) - (imgGameOver.width / 2f)
            val yGameOver = (height / 2f) - (imgGameOver.height / 2f)
            canvas.drawBitmap(imgGameOver, xGameOver, yGameOver, null)

            val paintEnd = Paint().apply {
                color = Color.WHITE
                textSize = 60f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("SCORE: $score", width / 2f, yGameOver + imgGameOver.height + 80, paintEnd)
            paintEnd.color = Color.YELLOW
            canvas.drawText("BEST: $highScore", width / 2f, yGameOver + imgGameOver.height + 160, paintEnd)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (isGameOver) {
                birdY = 500f
                velocity = 0f
                score = 0
                isGameOver = false
                tubiSopra.clear()
                tubiSotto.clear()
                inizializzaTubi()
                invalidate()
            } else {
                velocity = -30f
            }
        }
        return true
    }
}