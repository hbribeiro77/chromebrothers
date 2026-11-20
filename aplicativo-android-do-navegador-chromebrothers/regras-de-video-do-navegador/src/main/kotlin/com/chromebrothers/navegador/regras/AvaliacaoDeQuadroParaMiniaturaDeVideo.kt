package com.chromebrothers.navegador.regras

object AvaliacaoDeQuadroParaMiniaturaDeVideo {
    const val luminanciaMinima = 18

    fun serve(luminancias: IntArray): Boolean {
        if (luminancias.isEmpty()) return false
        val media = luminancias.sum().toDouble() / luminancias.size
        return media >= luminanciaMinima
    }
}
