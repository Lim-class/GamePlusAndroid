package com.example.gameplus

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class TrisActivity : AppCompatActivity() {

    private val buttons = arrayOfNulls<Button>(9)
    private var board = arrayOfNulls<String>(9)
    private var playerTurn = true // true = X, false = O
    private var livelloDifficolta = "Facile"
    private var isVsPC = true
    private lateinit var statusText: TextView
    private var gameActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tris)

        statusText = findViewById(R.id.status_text)
        val spinner = findViewById<Spinner>(R.id.spinner_difficolta)
        val btnReset = findViewById<Button>(R.id.btn_reset)

        for (i in 0 until 9) {
            val buttonID = "btn_$i"
            val resID = resources.getIdentifier(buttonID, "id", packageName)
            buttons[i] = findViewById(resID)
            buttons[i]?.setOnClickListener { onCellClick(i) }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected == "Due Giocatori") {
                    isVsPC = false
                } else {
                    isVsPC = true
                    livelloDifficolta = selected
                }
                resetGame()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        btnReset.setOnClickListener { resetGame() }
    }

    private fun onCellClick(index: Int) {
        if (gameActive && board[index] == null) {
            if (isVsPC && !playerTurn) return

            val currentSign = if (playerTurn) "X" else "O"
            eseguiMossa(index, currentSign)

            if (gameActive) {
                playerTurn = !playerTurn

                if (isVsPC && !playerTurn) {
                    statusText.text = "Turno del Computer..."
                    Handler(Looper.getMainLooper()).postDelayed({ mossaComputer() }, 800)
                } else {
                    statusText.text = "Turno di ${if (playerTurn) "X" else "O"}"
                }
            }
        }
    }

    private fun eseguiMossa(index: Int, segno: String) {
        board[index] = segno
        buttons[index]?.text = segno
        buttons[index]?.setTextColor(if (segno == "X") Color.parseColor("#E94560") else Color.parseColor("#4CC9F0"))

        when {
            controllaVittoria(segno) -> {
                statusText.text = "Vittoria: $segno!"
                gameActive = false
            }
            isBoardFull() -> {
                statusText.text = "Pareggio!"
                gameActive = false
            }
        }
    }

    private fun mossaComputer() {
        if (!gameActive) return

        val mossa = when (livelloDifficolta) {
            "Facile" -> mossaCasuale()
            "Medio" -> if (Random.nextInt(10) < 4) mossaCasuale() else mossaMigliore()
            else -> mossaMigliore() // Difficile
        }

        if (mossa != -1) {
            eseguiMossa(mossa, "O")
            if (gameActive) {
                playerTurn = true
                statusText.text = "Turno di X"
            }
        }
    }

    private fun mossaCasuale(): Int {
        // Kotlin ti permette di filtrare facilmente gli indici in base a una condizione (ovvero celle vuote)
        val libere = board.indices.filter { board[it] == null }
        return if (libere.isEmpty()) -1 else libere.random()
    }

    private fun mossaMigliore(): Int {
        var bestVal = Int.MIN_VALUE
        var mossa = -1
        for (i in 0 until 9) {
            if (board[i] == null) {
                board[i] = "O"
                val moveVal = minimax(0, false)
                board[i] = null
                if (moveVal > bestVal) {
                    mossa = i
                    bestVal = moveVal
                }
            }
        }
        return mossa
    }

    private fun minimax(depth: Int, isMax: Boolean): Int {
        if (controllaVittoria("O")) return 10 - depth
        if (controllaVittoria("X")) return depth - 10
        if (isBoardFull()) return 0

        return if (isMax) {
            var best = Int.MIN_VALUE
            for (i in 0 until 9) {
                if (board[i] == null) {
                    board[i] = "O"
                    best = Math.max(best, minimax(depth + 1, false))
                    board[i] = null
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in 0 until 9) {
                if (board[i] == null) {
                    board[i] = "X"
                    best = Math.min(best, minimax(depth + 1, true))
                    board[i] = null
                }
            }
            best
        }
    }

    private fun controllaVittoria(s: String): Boolean {
        val winPositions = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        for (p in winPositions) {
            if (board[p[0]] == s && board[p[1]] == s && board[p[2]] == s) return true
        }
        return false
    }

    private fun isBoardFull(): Boolean {
        // Verifica se TUTTE le celle sono diverse da null in una singola riga!
        return board.all { it != null }
    }

    private fun resetGame() {
        board = arrayOfNulls(9)
        gameActive = true
        playerTurn = true
        statusText.text = "Turno di X"
        for (b in buttons) b?.text = ""
    }
}