package com.example.gameplus

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SudokuActivity : AppCompatActivity() {

    private val GRID_SIZE = 9
    // Array bidimensionali in Kotlin
    private val cells = Array(GRID_SIZE) { arrayOfNulls<EditText>(GRID_SIZE) }
    private val solution = Array(GRID_SIZE) { IntArray(GRID_SIZE) }
    private val puzzle = Array(GRID_SIZE) { IntArray(GRID_SIZE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sudoku)

        val gridLayout = findViewById<GridLayout>(R.id.sudoku_grid)
        val btnVerifica = findViewById<Button>(R.id.btn_verifica)
        val btnSuggerimento = findViewById<Button>(R.id.btn_suggerimento)

        generaNuovoSudoku()
        creaInterfaccia(gridLayout)

        btnVerifica.setOnClickListener { verificaSoluzione() }
        btnSuggerimento.setOnClickListener { daiSuggerimento() }
    }

    private fun generaNuovoSudoku() {
        for (i in 0 until GRID_SIZE) solution[i].fill(0)

        solve(0, 0)

        for (i in 0 until GRID_SIZE) {
            System.arraycopy(solution[i], 0, puzzle[i], 0, GRID_SIZE)
        }

        val rand = java.util.Random()
        var rimosse = 45
        while (rimosse > 0) {
            val r = rand.nextInt(GRID_SIZE)
            val c = rand.nextInt(GRID_SIZE)
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0
                rimosse--
            }
        }
    }

    private fun solve(row: Int, col: Int): Boolean {
        if (row == GRID_SIZE) return true
        val nextRow = if (col == GRID_SIZE - 1) row + 1 else row
        val nextCol = if (col == GRID_SIZE - 1) 0 else col + 1

        val nums = mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        nums.shuffle()

        for (num in nums) {
            if (isValid(row, col, num)) {
                solution[row][col] = num
                if (solve(nextRow, nextCol)) return true
                solution[row][col] = 0
            }
        }
        return false
    }

    private fun isValid(row: Int, col: Int, num: Int): Boolean {
        for (i in 0 until GRID_SIZE) {
            if (solution[row][i] == num || solution[i][col] == num) return false
        }
        val sRow = (row / 3) * 3
        val sCol = (col / 3) * 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (solution[sRow + i][sCol + j] == num) return false
            }
        }
        return true
    }

    private fun applicaStileCella(r: Int, c: Int, cella: EditText) {
        if (((r / 3) + (c / 3)) % 2 == 0) {
            cella.setBackgroundColor(Color.parseColor("#E0E0E0"))
        } else {
            cella.setBackgroundColor(Color.WHITE)
        }

        cella.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (cella.isEnabled) cella.setTextColor(Color.BLUE)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun creaInterfaccia(gridLayout: GridLayout) {
        gridLayout.removeAllViews()
        val screenWidth = resources.displayMetrics.widthPixels
        val cellSize = (screenWidth - 100) / 9

        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val cella = EditText(this)
                cells[r][c] = cella
                applicaStileCella(r, c, cella)

                cella.gravity = Gravity.CENTER
                cella.inputType = InputType.TYPE_CLASS_NUMBER
                cella.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(1))

                val params = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(1, 1, 1, 1)
                }
                cella.layoutParams = params

                if (puzzle[r][c] != 0) {
                    cella.setText(puzzle[r][c].toString())
                    cella.isEnabled = false
                    cella.setTextColor(Color.BLACK)
                    cella.setTypeface(null, Typeface.BOLD)
                } else {
                    cella.setTextColor(Color.BLUE)
                }
                gridLayout.addView(cella)
            }
        }
    }

    private fun verificaSoluzione() {
        var errore = false
        var completo = true

        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val cella = cells[r][c] ?: continue
                if (!cella.isEnabled) continue

                val valStr = cella.text.toString()
                if (valStr.isEmpty()) {
                    completo = false
                } else {
                    if (valStr.toInt() != solution[r][c]) {
                        cella.setTextColor(Color.RED)
                        errore = true
                    } else {
                        cella.setTextColor(Color.BLUE)
                    }
                }
            }
        }

        when {
            errore -> Toast.makeText(this, "Ci sono errori in rosso!", Toast.LENGTH_SHORT).show()
            completo -> Toast.makeText(this, "VITTORIA! Sudoku completato!", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this, "Corretto finora!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun daiSuggerimento() {
        val vuote = ArrayList<IntArray>()
        for (r in 0 until GRID_SIZE) {
            for (c in 0 until GRID_SIZE) {
                val cella = cells[r][c] ?: continue
                val valStr = cella.text.toString()
                if (valStr.isEmpty() || (valStr.toIntOrNull() != solution[r][c])) {
                    vuote.add(intArrayOf(r, c))
                }
            }
        }

        if (vuote.isNotEmpty()) {
            val scelta = vuote[Random.nextInt(vuote.size)]
            val r = scelta[0]
            val c = scelta[1]
            val cellaSuggerimento = cells[r][c]

            cellaSuggerimento?.setText(solution[r][c].toString())
            cellaSuggerimento?.setTextColor(Color.parseColor("#800080"))
            cellaSuggerimento?.setTypeface(null, Typeface.ITALIC)
        }
    }
}