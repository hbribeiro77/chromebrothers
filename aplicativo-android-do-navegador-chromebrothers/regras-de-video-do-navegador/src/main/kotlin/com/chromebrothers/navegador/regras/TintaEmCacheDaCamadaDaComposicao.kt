package com.chromebrothers.navegador.regras

import kotlin.math.max

object TintaEmCacheDaCamadaDaComposicao {
    /** Tinta na resolução da imagem até este lado (prints do tablet têm 2960 px; 2048 deixava o traço ampliado e áspero). */
    const val LADO_MAXIMO = 3072

    fun primeiroIndice(pontosJaDesenhados: Int, total: Int): Int {
        if (total <= pontosJaDesenhados) return total
        if (pontosJaDesenhados <= 0) return 0
        return pontosJaDesenhados - 1
    }

    fun dimensoes(largura: Int, altura: Int): Pair<Int, Int> {
        if (largura <= 0 || altura <= 0) return 1 to 1
        val maior = max(largura, altura)
        if (maior <= LADO_MAXIMO) return largura to altura
        val escala = LADO_MAXIMO.toFloat() / maior.toFloat()
        return (largura * escala).toInt().coerceAtLeast(1) to (altura * escala).toInt().coerceAtLeast(1)
    }
}
