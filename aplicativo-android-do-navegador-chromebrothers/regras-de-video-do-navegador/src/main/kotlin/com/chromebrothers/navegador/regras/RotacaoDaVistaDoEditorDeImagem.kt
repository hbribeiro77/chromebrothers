package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.math.roundToInt

/** Rotação da vista do editor (só a vista: não mexe na imagem nem no histórico). Ângulos em graus. */
object RotacaoDaVistaDoEditorDeImagem {
    /** A esta distância de 0/90/180/270 o ângulo encaixa no reto. */
    const val ENCAIXE_GRAUS = 6f

    /** Giro acumulado no gesto antes de começar a girar (a pinça de zoom não gira sem querer). */
    const val LIMIAR_PARA_COMECAR_GRAUS = 3f

    /** Leva para (-180, 180]. */
    fun normalizar(graus: Float): Float {
        var g = graus % 360f
        if (g <= -180f) g += 360f
        if (g > 180f) g -= 360f
        return g
    }

    /** Diferença `agora - antes` pelo caminho mais curto (atan2 pula de 180 para -180). */
    fun diferenca(agora: Float, antes: Float): Float = normalizar(agora - antes)

    fun comEncaixe(graus: Float): Float {
        val g = normalizar(graus)
        val reto = (g / 90f).roundToInt() * 90f
        return if (abs(g - reto) <= ENCAIXE_GRAUS) normalizar(reto) else g
    }

    fun estaEncaixado(graus: Float): Boolean {
        val g = normalizar(graus)
        return abs(g - (g / 90f).roundToInt() * 90f) < 0.01f
    }

    /** Ângulo para mostrar: inteiro em 0..359. */
    fun grausInteiros(graus: Float): Int = ((normalizar(graus).roundToInt() % 360) + 360) % 360

    /** Texto do chip: só o zoom em 0°; com giro, `67% · 35°`. */
    fun rotuloDoChip(zoom: Float, graus: Float): String {
        val percentual = ZoomDaVistaDoEditorDeImagem.rotuloPercentual(zoom)
        val inteiros = grausInteiros(graus)
        return if (inteiros == 0) percentual else "$percentual · $inteiros°"
    }
}
