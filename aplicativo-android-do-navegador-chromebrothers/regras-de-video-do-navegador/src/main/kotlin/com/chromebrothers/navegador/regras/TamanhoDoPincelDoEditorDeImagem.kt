package com.chromebrothers.navegador.regras

import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Tamanho do pincel/borracha como no Procreate: controle 0..1 (1%..100%) em curva exponencial,
 * com muita precisão nos tamanhos pequenos e alcance até pincéis enormes. Diâmetro em dp na tela.
 */
object TamanhoDoPincelDoEditorDeImagem {
    const val MINIMO_DP = 0.5f
    const val MAXIMO_DP_PINCEL = 300f
    const val MAXIMO_DP_BORRACHA = 400f

    /** Passo dos botões −/+ (5%). */
    const val PASSO = 0.05f

    /** Os padrões batem com o tamanho médio de antes (14 dp pincel, 28 dp borracha). */
    val PADRAO_PINCEL: Float = valorDoDiametro(14f, borracha = false)
    val PADRAO_BORRACHA: Float = valorDoDiametro(28f, borracha = true)

    private fun maximo(borracha: Boolean) = if (borracha) MAXIMO_DP_BORRACHA else MAXIMO_DP_PINCEL

    fun diametroDp(valor: Float, borracha: Boolean): Float =
        MINIMO_DP * (maximo(borracha) / MINIMO_DP).pow(valor.coerceIn(0f, 1f))

    fun valorDoDiametro(diametroDp: Float, borracha: Boolean): Float {
        val d = diametroDp.coerceIn(MINIMO_DP, maximo(borracha))
        return (ln(d / MINIMO_DP) / ln(maximo(borracha) / MINIMO_DP)).coerceIn(0f, 1f)
    }

    fun percentual(valor: Float): Int = (valor.coerceIn(0f, 1f) * 100f).roundToInt().coerceIn(1, 100)

    /**
     * Ajuste fino ao arrastar o controle: quanto mais o dedo se afasta para o lado,
     * mais devagar o valor anda (até 10× mais lento).
     */
    fun fatorDoAjusteFino(distanciaLateralPx: Float, referenciaPx: Float): Float {
        if (distanciaLateralPx <= 0f || referenciaPx <= 0f) return 1f
        return (1f / (1f + distanciaLateralPx / referenciaPx)).coerceAtLeast(0.1f)
    }
}
