package com.chromebrothers.navegador.regras

data class RetanguloNormalizadoDaSelecaoDeCaptura(
    val esquerda: Int,
    val topo: Int,
    val largura: Int,
    val altura: Int,
) {
    companion object {
        fun de(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            larguraMaxima: Int,
            alturaMaxima: Int,
        ): RetanguloNormalizadoDaSelecaoDeCaptura {
            val minX = minOf(x1, x2).toInt().coerceIn(0, larguraMaxima.coerceAtLeast(0))
            val maxX = maxOf(x1, x2).toInt().coerceIn(0, larguraMaxima.coerceAtLeast(0))
            val minY = minOf(y1, y2).toInt().coerceIn(0, alturaMaxima.coerceAtLeast(0))
            val maxY = maxOf(y1, y2).toInt().coerceIn(0, alturaMaxima.coerceAtLeast(0))
            val largura = (maxX - minX).coerceAtLeast(if (x1 != x2) 1 else 0)
            val altura = (maxY - minY).coerceAtLeast(if (y1 != y2) 1 else 0)
            val larguraFinal = largura.coerceAtMost((larguraMaxima - minX).coerceAtLeast(0))
            val alturaFinal = altura.coerceAtMost((alturaMaxima - minY).coerceAtLeast(0))
            return RetanguloNormalizadoDaSelecaoDeCaptura(
                esquerda = minX,
                topo = minY,
                largura = larguraFinal,
                altura = alturaFinal,
            )
        }
    }
}
