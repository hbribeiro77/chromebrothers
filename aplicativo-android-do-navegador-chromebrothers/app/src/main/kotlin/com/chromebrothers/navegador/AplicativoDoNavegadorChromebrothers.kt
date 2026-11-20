package com.chromebrothers.navegador

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Alinha o DayNight do sistema à preferência do app antes da 1ª Activity,
 * para o windowBackground do tema já nascer escuro/claro (sem piscada branca).
 */
class AplicativoDoNavegadorChromebrothers : Application() {
    override fun onCreate() {
        super.onCreate()
        val escura = PreferenciasVisuaisEDeNavegacaoDoChromebrothers(this).aparenciaEscura
        AppCompatDelegate.setDefaultNightMode(
            if (escura) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
        )
    }
}
