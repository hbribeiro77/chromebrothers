package com.chromebrothers.navegador.regras

import java.util.Locale

object OrigemDaMidiaNaBibliotecaDoNavegador {
    const val HIGGSFIELD = "higgsfield"
    const val SJINN = "sjinn"
    const val TOPVIEW = "topview"
    const val MAGNIFIC = "magnific"
    const val VIVIX = "vivix"
    const val PAGINA = "pagina"
    const val DESCONHECIDO = "desconhecido"

    private val conhecidas = setOf(HIGGSFIELD, SJINN, TOPVIEW, MAGNIFIC, VIVIX, PAGINA)

    fun parse(bruto: String?): String {
        val normalizada = bruto?.trim()?.lowercase(Locale.ROOT).orEmpty()
        return if (normalizada in conhecidas) normalizada else DESCONHECIDO
    }

    fun rotulo(origem: String): String = when (parse(origem)) {
        HIGGSFIELD -> "Higgsfield"
        SJINN -> "Sjinn"
        TOPVIEW -> "Topview"
        MAGNIFIC -> "Magnific"
        VIVIX -> "Vivix"
        PAGINA -> "Página"
        else -> "Desconhecido"
    }
}
