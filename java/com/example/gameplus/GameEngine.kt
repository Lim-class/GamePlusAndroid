package com.example.gameplus

import kotlin.math.abs
import kotlin.random.Random

// 1. ASSET (Un modello dati perfetto per Kotlin)
class Asset(
    val nome: String,
    val tipo: String,
    var prezzoCorrente: Double,
    val volatilita: Double,
    val cedolaAnnuale: Double
) {
    var quantitaPosseduta: Long = 0
    var prezzoMedioAcquisto: Double = 0.0

    fun registraAcquisto(quantita: Long, prezzo: Double) {
        val costoTotaleVecchio = quantitaPosseduta * prezzoMedioAcquisto
        val costoNuovo = quantita * prezzo
        quantitaPosseduta += quantita
        prezzoMedioAcquisto = (costoTotaleVecchio + costoNuovo) / quantitaPosseduta
    }

    fun getValoreTotale(): Double = quantitaPosseduta * prezzoCorrente
}

// 2. FINANCE MANAGER
class FinanceManager(private val paeseSelezionato: String) {
    fun calcolaTasseReddito(redditoLordoSemestre: Double): Double {
        val aliquota = if (paeseSelezionato == "Italia") 0.43 else 0.23
        return redditoLordoSemestre * aliquota
    }

    fun calcolaTassaCapitalGain(a: Asset, plusvalenza: Double): Double {
        if (plusvalenza <= 0) return 0.0
        var aliquota = 0.26
        if (a.tipo == "BOND" && paeseSelezionato == "Italia") aliquota = 0.125
        return plusvalenza * aliquota
    }

    fun calcolaInteressiPassivi(liquidita: Double): Double {
        return if (liquidita < 0) abs(liquidita) * 0.05 else 0.0
    }
}

// 3. MARKET MANAGER
class MarketManager {
    val assets: List<Asset> = listOf(
        Asset("BTP Italia 10Y", "BOND", 100.0, 0.01, 0.045),
        Asset("US Treasury", "BOND", 100.0, 0.005, 0.035),
        Asset("Azioni Tech", "AZIONI", 150.0, 0.20, 0.0),
        Asset("ETF S&P 500", "AZIONI", 400.0, 0.08, 0.0),
        Asset("Iniziale", "AZIONI", 0.0, 100.0, 0.0),
        Asset("Oro Fisico", "COMMODITY", 1800.0, 0.03, 0.0),
        Asset("Bitcoin", "CRYPTO", 45000.0, 0.30, 0.0)
    )

    fun getAsset(index: Int): Asset = assets[index]

    fun simulaVariazioniEGeneraCedole(): Double {
        var cedoleIncassate = 0.0
        for (a in assets) {
            if (a.tipo == "BOND" && a.quantitaPosseduta > 0) {
                cedoleIncassate += (a.quantitaPosseduta * 100.0) * (a.cedolaAnnuale / 2.0)
            }

            // Calcolo variazioni snellito tramite il .nextDouble(min, max) di Kotlin
            var variazione = when (a.tipo) {
                "BOND" -> Random.nextDouble(-0.02, 0.02)
                "COMMODITY" -> Random.nextDouble(-0.01, 0.04)
                else -> {
                    var v = Random.nextDouble(-a.volatilita, a.volatilita)
                    if (Random.nextInt(100) < 5) v = -0.30 // Shock
                    v
                }
            }

            a.prezzoCorrente *= (1 + variazione)
            if (a.prezzoCorrente < 0.01) a.prezzoCorrente = 0.01
        }
        return cedoleIncassate
    }

    // Usiamo sumOf per calcolare la somma in una sola riga
    fun getValoreTotalePortafoglio(): Double = assets.sumOf { it.getValoreTotale() }
}

// 4. GAME ENGINE
enum class StatoGioco { IN_CORSO, VITTORIA, BANCAROTTA, VECCHIAIA }

class GameEngine(
    etaPartenza: Int,
    paeseSelezionato: String,
    private val obiettivoFinanziario: Long
) {
    var liquidita: Double = if (paeseSelezionato == "Delaware (USA)") 8000.0 else 10000.0
        private set // Permette la lettura all'esterno, ma la modifica solo da dentro
    var mesiTotaliDiVita: Double = (etaPartenza * 12).toDouble()
        private set
    var stipendioAnnuo: Double = 0.0
        private set
    var titoloLavoro: String = "Disoccupato"
        private set
    var tassePagateTotali: Double = 0.0
        private set
    var tasseUltimoSemestre: Double = 0.0
        private set

    private val financeManager = FinanceManager(paeseSelezionato)
    val marketManager = MarketManager()

    // Proprietà calcolata
    val patrimonioTotale: Double
        get() = liquidita + marketManager.getValoreTotalePortafoglio()

    fun compraAsset(assetIndex: Int, quantita: Long): Boolean {
        val a = marketManager.getAsset(assetIndex)
        val costo = quantita * a.prezzoCorrente
        return if (liquidita >= costo) {
            liquidita -= costo
            a.registraAcquisto(quantita, a.prezzoCorrente)
            true
        } else false
    }

    fun vendiAsset(assetIndex: Int, quantita: Long): Double {
        val a = marketManager.getAsset(assetIndex)
        if (a.quantitaPosseduta >= quantita) {
            val ricavoLordo = quantita * a.prezzoCorrente
            val costoOriginale = quantita * a.prezzoMedioAcquisto
            val plusvalenza = ricavoLordo - costoOriginale

            val tasseSuVendita = financeManager.calcolaTassaCapitalGain(a, plusvalenza)

            liquidita += (ricavoLordo - tasseSuVendita)
            a.quantitaPosseduta -= quantita
            tassePagateTotali += tasseSuVendita

            return tasseSuVendita
        }
        return -1.0
    }

    fun lavoraSemestre(): String {
        var messaggio = "Hai lavorato per 6 mesi."
        if (stipendioAnnuo == 0.0) {
            stipendioAnnuo = 18000.0 + Random.nextInt(12000)
            titoloLavoro = "Apprendista"
            messaggio = "Hai trovato lavoro come Apprendista!"
        } else if (Random.nextInt(100) < 15) {
            stipendioAnnuo += 5000
            titoloLavoro = "Senior Manager"
            messaggio = "Promozione ottenuta!"
        }
        avanzaTempo(6)
        return messaggio
    }

    fun tentaStartup(): String {
        if (liquidita < 50000) return "Servono 50.000€!"
        liquidita -= 50000

        val messaggio = when (Random.nextInt(100)) {
            in 0..9 -> { liquidita += 1000000; "UNICORNO! Hai fatto il botto! +1.000.000€" }
            in 10..34 -> { liquidita += 200000; "Successo! Buona exit! +200.000€" }
            else -> "Startup Fallita."
        }
        avanzaTempo(6)
        return messaggio
    }

    private fun avanzaTempo(mesi: Int) {
        mesiTotaliDiVita += mesi
        liquidita -= financeManager.calcolaInteressiPassivi(liquidita)

        val guadagnoLavoro = (stipendioAnnuo / 12.0) * mesi
        tasseUltimoSemestre = financeManager.calcolaTasseReddito(guadagnoLavoro)
        tassePagateTotali += tasseUltimoSemestre

        liquidita += (guadagnoLavoro - tasseUltimoSemestre)
        liquidita += marketManager.simulaVariazioniEGeneraCedole()
    }

    fun controllaStatoGioco(): StatoGioco {
        return when {
            patrimonioTotale >= obiettivoFinanziario -> StatoGioco.VITTORIA
            mesiTotaliDiVita >= 100 * 12 -> StatoGioco.VECCHIAIA
            liquidita < -50000 -> StatoGioco.BANCAROTTA
            else -> StatoGioco.IN_CORSO
        }
    }
}