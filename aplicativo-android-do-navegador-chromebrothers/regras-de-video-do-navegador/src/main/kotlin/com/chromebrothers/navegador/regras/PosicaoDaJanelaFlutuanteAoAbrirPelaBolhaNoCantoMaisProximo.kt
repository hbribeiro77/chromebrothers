package com.chromebrothers.navegador.regras

/**
 * Janela reaberta pela bolha: a bolha vira o canto da janela mais próximo dela
 * (bolha na metade direita → janela cresce para a esquerda; na metade de baixo → cresce para cima),
 * e o resultado é limitado à área disponível.
 */
object PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo {
    fun calcular(
        xDaBolha: Float,
        yDaBolha: Float,
        ladoDaBolha: Float,
        larguraDaJanela: Float,
        alturaDaJanela: Float,
        larguraDisponivel: Float,
        alturaDisponivel: Float,
    ): Pair<Float, Float> {
        val centroX = xDaBolha + ladoDaBolha / 2f
        val centroY = yDaBolha + ladoDaBolha / 2f
        val x = if (centroX > larguraDisponivel / 2f) xDaBolha + ladoDaBolha - larguraDaJanela else xDaBolha
        val y = if (centroY > alturaDisponivel / 2f) yDaBolha + ladoDaBolha - alturaDaJanela else yDaBolha
        return PosicaoDaJanelaDoCampoDeTexto.limitar(
            x,
            y,
            larguraDaJanela,
            alturaDaJanela,
            larguraDisponivel,
            alturaDisponivel,
        )
    }
}
