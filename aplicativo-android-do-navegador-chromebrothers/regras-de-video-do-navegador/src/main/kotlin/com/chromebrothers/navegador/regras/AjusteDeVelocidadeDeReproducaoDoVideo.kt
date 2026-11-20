package com.chromebrothers.navegador.regras

object AjusteDeVelocidadeDeReproducaoDoVideo {
    const val VELOCIDADE_MINIMA = 0.25
    val opcoes = listOf(0.25, 0.5, 0.75, 1.0, 1.25, 1.5, 2.0)

    fun ajustar(pedida: Double): Double {
        if (pedida.isNaN() || pedida.isInfinite()) {
            return 1.0
        }
        return maxOf(VELOCIDADE_MINIMA, pedida)
    }
}
