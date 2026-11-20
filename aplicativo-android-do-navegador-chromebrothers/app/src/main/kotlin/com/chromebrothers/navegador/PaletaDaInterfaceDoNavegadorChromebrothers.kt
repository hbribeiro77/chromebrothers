package com.chromebrothers.navegador

import android.graphics.Color

data class PaletaDaInterfaceDoNavegadorChromebrothers(
    val escura: Boolean,
    val fundoDaFaixa: Int,
    val fundoDaBarra: Int,
    val fundoDaPagina: Int,
    val fundoDoCampo: Int,
    val texto: Int,
    val textoSuave: Int,
    val icone: Int,
    val destaque: Int,
) {
    companion object {
        fun escolher(escura: Boolean): PaletaDaInterfaceDoNavegadorChromebrothers {
            return if (escura) escura() else clara()
        }

        fun clara() = PaletaDaInterfaceDoNavegadorChromebrothers(
            escura = false,
            fundoDaFaixa = Color.parseColor("#DEE1E6"),
            fundoDaBarra = Color.WHITE,
            fundoDaPagina = Color.WHITE,
            fundoDoCampo = Color.parseColor("#F1F3F4"),
            texto = Color.parseColor("#202124"),
            textoSuave = Color.parseColor("#5F6368"),
            icone = Color.parseColor("#5F6368"),
            destaque = Color.parseColor("#1A73E8"),
        )

        fun escura() = PaletaDaInterfaceDoNavegadorChromebrothers(
            escura = true,
            fundoDaFaixa = Color.parseColor("#202124"),
            fundoDaBarra = Color.parseColor("#2D2E31"),
            fundoDaPagina = Color.parseColor("#202124"),
            fundoDoCampo = Color.parseColor("#3C4043"),
            texto = Color.parseColor("#E8EAED"),
            textoSuave = Color.parseColor("#9AA0A6"),
            icone = Color.parseColor("#E8EAED"),
            destaque = Color.parseColor("#8AB4F8"),
        )
    }
}
