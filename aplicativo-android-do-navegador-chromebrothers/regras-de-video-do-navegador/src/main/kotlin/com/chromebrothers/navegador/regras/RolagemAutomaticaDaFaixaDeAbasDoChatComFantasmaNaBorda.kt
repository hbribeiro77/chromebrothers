package com.chromebrothers.navegador.regras

/**
 * Fantasma perto da borda da faixa de abas do chat: a faixa rola sozinha para mostrar as abas
 * que não couberam. Quanto mais perto da ponta, mais rápido (px por quadro; negativo = para a esquerda).
 */
object RolagemAutomaticaDaFaixaDeAbasDoChatComFantasmaNaBorda {
    const val ZONA_DA_BORDA_DP = 56f
    const val VELOCIDADE_MAXIMA_DP_POR_QUADRO = 14f
    const val VELOCIDADE_MINIMA_DP_POR_QUADRO = 2f
    const val FOLGA_VERTICAL_DP = 16f

    fun pixelsPorQuadro(
        xNaFaixa: Float,
        larguraDaFaixa: Float,
        densidade: Float,
        podeRolarParaAEsquerda: Boolean,
        podeRolarParaADireita: Boolean,
    ): Int {
        if (larguraDaFaixa <= 0f || xNaFaixa < 0f || xNaFaixa > larguraDaFaixa) return 0
        val zona = (ZONA_DA_BORDA_DP * densidade).coerceAtMost(larguraDaFaixa / 3f)
        val intensidade = when {
            xNaFaixa < zona && podeRolarParaAEsquerda -> -(1f - xNaFaixa / zona)
            larguraDaFaixa - xNaFaixa < zona && podeRolarParaADireita -> 1f - (larguraDaFaixa - xNaFaixa) / zona
            else -> return 0
        }
        val minima = VELOCIDADE_MINIMA_DP_POR_QUADRO * densidade
        val maxima = VELOCIDADE_MAXIMA_DP_POR_QUADRO * densidade
        val modulo = minima + (maxima - minima) * kotlin.math.abs(intensidade)
        return (if (intensidade < 0f) -modulo else modulo).toInt()
    }
}
