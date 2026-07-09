package com.example.gameplus

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class DamaActivity : AppCompatActivity() {

    private val caselle = Array(8) { arrayOfNulls<FrameLayout>(8) }
    private var turnoNeri = true
    private var casellaSelezionata: FrameLayout? = null
    private var rSel = -1
    private var cSel = -1
    private var multiplaInCorso = false
    private var vsAI = false

    private lateinit var txtStatus: TextView
    private lateinit var grid: GridLayout
    private lateinit var rootLayout: View
    private lateinit var spinnerMode: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dama)

        txtStatus = findViewById(R.id.status_dama)
        grid = findViewById(R.id.dama_grid)
        rootLayout = findViewById(android.R.id.content)
        spinnerMode = findViewById(R.id.spinner_mode)

        setupSpinner()
        inizializzaScacchiera()
        aggiornaInterfacciaTurno()

        findViewById<View>(R.id.btn_reset_dama).setOnClickListener { recreate() }
    }

    private fun setupSpinner() {
        val opzioni = arrayOf("2 Giocatori", "Contro PC (Facile)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, opzioni)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMode.adapter = adapter
        spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vsAI = (position == 1)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun inizializzaScacchiera() {
        val displayWidth = resources.displayMetrics.widthPixels
        val cellSize = (displayWidth - 64) / 8

        grid.removeAllViews()
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val box = FrameLayout(this)
                val params = GridLayout.LayoutParams(
                    GridLayout.spec(r), GridLayout.spec(c)
                ).apply {
                    width = cellSize
                    height = cellSize
                }
                box.layoutParams = params

                val isCasellaAttiva = (r + c) % 2 != 0
                box.setBackgroundColor(if (isCasellaAttiva) Color.parseColor("#B58863") else Color.parseColor("#F0D9B5"))

                if (isCasellaAttiva) setupPezzi(box, r)

                box.setOnClickListener { gestisciTocco(r, c) }
                caselle[r][c] = box
                grid.addView(box)
            }
        }
    }

    private fun setupPezzi(box: FrameLayout, r: Int) {
        if (r < 3 || r > 4) {
            val pezzo = ImageView(this)
            val pieceSize = (resources.displayMetrics.widthPixels / 10)
            pezzo.layoutParams = FrameLayout.LayoutParams(pieceSize, pieceSize, Gravity.CENTER)
            pezzo.setImageResource(android.R.drawable.presence_online)

            if (r < 3) {
                pezzo.setColorFilter(Color.WHITE)
                pezzo.tag = "BIANCO"
            } else {
                pezzo.setColorFilter(Color.BLACK)
                pezzo.tag = "NERO"
            }
            box.addView(pezzo)
        }
    }

    private fun gestisciTocco(r: Int, c: Int) {
        if (vsAI && !turnoNeri) return

        val coloreTurno = if (turnoNeri) "NERO" else "BIANCO"
        val boxToccata = caselle[r][c] ?: return

        if (boxToccata.childCount > 0) {
            if (multiplaInCorso) return
            val pezzo = boxToccata.getChildAt(0) as ImageView
            if (pezzo.tag.toString().startsWith(coloreTurno)) {
                selezionaPezzo(boxToccata, r, c)
            }
        } else if (casellaSelezionata != null) {
            analizzaMossa(r, c)
        }
    }

    private fun selezionaPezzo(box: FrameLayout, r: Int, c: Int) {
        resetColoriScacchiera()
        casellaSelezionata = box
        rSel = r
        cSel = c
        box.setBackgroundColor(Color.parseColor("#7B9734"))
    }

    private fun analizzaMossa(r: Int, c: Int) {
        val distR = r - rSel
        val distC = c - cSel
        val absR = Math.abs(distR)
        val absC = Math.abs(distC)

        if (absR != absC) return

        val tagPezzo = casellaSelezionata?.getChildAt(0)?.tag.toString()
        val isDama = tagPezzo.contains("DAMA")

        if (absR == 2) {
            val rM = rSel + distR / 2
            val cM = cSel + distC / 2
            if (isNemico(rM, cM)) {
                eseguiMossa(r, c, rM, cM)
                return
            }
        }

        if (!multiplaInCorso && absR == 1) {
            val direzioneValida = if (turnoNeri) -1 else 1
            if (isDama || distR == direzioneValida) {
                eseguiMossa(r, c, -1, -1)
            }
        }
    }

    private fun isNemico(r: Int, c: Int): Boolean {
        val casella = caselle[r][c] ?: return false
        if (casella.childCount == 0) return false
        val tag = casella.getChildAt(0).tag.toString()
        val alleato = if (turnoNeri) "NERO" else "BIANCO"
        return !tag.startsWith(alleato)
    }

    private fun eseguiMossa(r: Int, c: Int, rM: Int, cM: Int) {
        val boxSelezionata = casellaSelezionata ?: return
        val pezzo = boxSelezionata.getChildAt(0) as ImageView

        if (rM != -1) caselle[rM][cM]?.removeAllViews()

        boxSelezionata.removeAllViews()
        caselle[r][c]?.addView(pezzo)

        if (!pezzo.tag.toString().contains("DAMA")) {
            if ((turnoNeri && r == 0) || (!turnoNeri && r == 7)) {
                pezzo.tag = if (turnoNeri) "NERO_DAMA" else "BIANCO_DAMA"
                pezzo.setBackgroundResource(android.R.drawable.btn_star_big_on)
            }
        }

        rSel = r
        cSel = c
        casellaSelezionata = caselle[r][c]

        if (rM != -1 && puoAncoraMangiare(r, c)) {
            multiplaInCorso = true
            if (!vsAI || turnoNeri) casellaSelezionata?.setBackgroundColor(Color.CYAN)
            if (vsAI && !turnoNeri) Handler(Looper.getMainLooper()).postDelayed({ mossaAI() }, 800)
        } else {
            concludiTurno()
        }
    }

    private fun concludiTurno() {
        multiplaInCorso = false
        turnoNeri = !turnoNeri
        casellaSelezionata = null
        resetColoriScacchiera()
        aggiornaInterfacciaTurno()
        controllaVittoria()

        if (!vsAI) {
            val targetRotation = if (turnoNeri) 0f else 180f
            grid.animate().rotation(targetRotation).setDuration(600).start()
            ruotaPezzi(targetRotation)
        }

        if (vsAI && !turnoNeri) {
            Handler(Looper.getMainLooper()).postDelayed({ mossaAI() }, 1000)
        }

        if (!haMosseDisponibili(turnoNeri)) {
            Toast.makeText(this, "PAREGGIO! Nessuna mossa disponibile.", Toast.LENGTH_LONG).show()
        }
    }

    private fun mossaAI() {
        val mosseMangia = ArrayList<IntArray>()
        val mosseSemplici = ArrayList<IntArray>()

        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val casella = caselle[r][c] ?: continue
                if (casella.childCount > 0) {
                    val tag = casella.getChildAt(0).tag.toString()
                    if (tag.startsWith("BIANCO")) {
                        val isDamaAI = tag.contains("DAMA")
                        val dirs = arrayOf(
                            intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1),
                            intArrayOf(2, 2), intArrayOf(2, -2), intArrayOf(-2, 2), intArrayOf(-2, -2)
                        )
                        for (d in dirs) {
                            val nr = r + d[0]
                            val nc = c + d[1]
                            if (nr in 0..7 && nc in 0..7 && caselle[nr][nc]?.childCount == 0) {
                                if (Math.abs(d[0]) == 2) {
                                    if (isNemicoAI(r + d[0] / 2, c + d[1] / 2)) {
                                        mosseMangia.add(intArrayOf(r, c, nr, nc, r + d[0] / 2, c + d[1] / 2))
                                    }
                                } else if (!multiplaInCorso && (isDamaAI || d[0] == 1)) {
                                    mosseSemplici.add(intArrayOf(r, c, nr, nc, -1, -1))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mosseMangia.isNotEmpty()) {
            val m = mosseMangia[Random.nextInt(mosseMangia.size)]
            applicatoAI(m)
        } else if (mosseSemplici.isNotEmpty()) {
            val m = mosseSemplici[Random.nextInt(mosseSemplici.size)]
            applicatoAI(m)
        }
    }

    private fun applicatoAI(m: IntArray) {
        casellaSelezionata = caselle[m[0]][m[1]]
        rSel = m[0]
        cSel = m[1]
        eseguiMossa(m[2], m[3], m[4], m[5])
    }

    private fun isNemicoAI(r: Int, c: Int): Boolean {
        val casella = caselle[r][c] ?: return false
        if (casella.childCount == 0) return false
        return casella.getChildAt(0).tag.toString().startsWith("NERO")
    }

    private fun ruotaPezzi(rotation: Float) {
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val casella = caselle[r][c] ?: continue
                if (casella.childCount > 0) {
                    casella.getChildAt(0).animate().rotation(rotation).setDuration(600).start()
                }
            }
        }
    }

    private fun aggiornaInterfacciaTurno() {
        if (turnoNeri) {
            txtStatus.text = "TURNO: NERO"
            txtStatus.setTextColor(Color.WHITE)
            rootLayout.setBackgroundColor(Color.parseColor("#212121"))
        } else {
            txtStatus.text = "TURNO: BIANCO (PC)"
            txtStatus.setTextColor(Color.BLACK)
            rootLayout.setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
    }

    private fun puoAncoraMangiare(r: Int, c: Int): Boolean {
        val dirs = arrayOf(intArrayOf(2, 2), intArrayOf(2, -2), intArrayOf(-2, 2), intArrayOf(-2, -2))
        for (d in dirs) {
            val tr = r + d[0]
            val tc = c + d[1]
            if (tr in 0..7 && tc in 0..7) {
                if (caselle[tr][tc]?.childCount == 0 && isNemico(r + d[0] / 2, c + d[1] / 2)) return true
            }
        }
        return false
    }

    private fun controllaVittoria() {
        var b = 0
        var n = 0
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val casella = caselle[r][c] ?: continue
                if (casella.childCount > 0) {
                    if (casella.getChildAt(0).tag.toString().startsWith("BIANCO")) b++
                    else n++
                }
            }
        }
        if (b == 0) Toast.makeText(this, "NERI VINCONO!", Toast.LENGTH_LONG).show()
        if (n == 0) Toast.makeText(this, "BIANCHI VINCONO!", Toast.LENGTH_LONG).show()
    }

    private fun resetColoriScacchiera() {
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                if ((r + c) % 2 != 0) caselle[r][c]?.setBackgroundColor(Color.parseColor("#B58863"))
            }
        }
    }

    private fun haMosseDisponibili(neri: Boolean): Boolean {
        val colore = if (neri) "NERO" else "BIANCO"
        val dirs = arrayOf(
            intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1),
            intArrayOf(2, 2), intArrayOf(2, -2), intArrayOf(-2, 2), intArrayOf(-2, -2)
        )

        for (r in 0 until 8) {
            for (c in 0 until 8) {
                val casella = caselle[r][c] ?: continue
                if (casella.childCount > 0) {
                    val pezzo = casella.getChildAt(0) as ImageView
                    val tag = pezzo.tag.toString()

                    if (!tag.startsWith(colore)) continue
                    val dama = tag.contains("DAMA")

                    for (d in dirs) {
                        val nr = r + d[0]
                        val nc = c + d[1]

                        if (nr !in 0..7 || nc !in 0..7) continue
                        if (caselle[nr][nc]?.childCount != 0) continue

                        // Mossa semplice
                        if (Math.abs(d[0]) == 1) {
                            if (dama || (neri && d[0] == -1) || (!neri && d[0] == 1)) {
                                return true
                            }
                        }

                        // Mangiare
                        if (Math.abs(d[0]) == 2) {
                            val mr = r + d[0] / 2
                            val mc = c + d[1] / 2
                            val casellaMezzo = caselle[mr][mc]

                            if (casellaMezzo?.childCount ?: 0 > 0) {
                                val tagM = casellaMezzo!!.getChildAt(0).tag.toString()
                                if (!tagM.startsWith(colore)) return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}