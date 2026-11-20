package com.chromebrothers.navegador.regras

import kotlin.math.roundToInt

object ZoomDaVistaDoEditorDeImagem {
    const val MINIMO = 0.25f
    const val MAXIMO = 8f
    const val ENCAIXE = 1f

    fun limitar(zoom: Float): Float = zoom.coerceIn(MINIMO, MAXIMO)

    fun percentualInteiro(zoom: Float): Int = (limitar(zoom) * 100f).roundToInt()

    fun rotuloPercentual(zoom: Float): String = "${percentualInteiro(zoom)}%"
}
