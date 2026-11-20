package com.chromebrothers.navegador.regras

object PosicaoDaJanelaDoCampoDeTexto {
    fun limitar(
        x: Float,
        y: Float,
        largura: Float,
        altura: Float,
        limiteLargura: Float,
        limiteAltura: Float,
    ): Pair<Float, Float> {
        val direita = (limiteLargura - largura).coerceAtLeast(0f)
        val baixo = (limiteAltura - altura).coerceAtLeast(0f)
        return x.coerceIn(0f, direita) to y.coerceIn(0f, baixo)
    }

    fun largura(atual: Float, delta: Float, esquerda: Float, minimo: Float, limiteLargura: Float): Float {
        val maximo = (limiteLargura - esquerda).coerceAtLeast(minimo)
        return (atual + delta).coerceIn(minimo, maximo)
    }

    fun altura(atual: Float, delta: Float, topo: Float, minimo: Float, limiteAltura: Float): Float {
        val maximo = (limiteAltura - topo).coerceAtLeast(minimo)
        return (atual + delta).coerceIn(minimo, maximo)
    }

    fun topoSeOTecladoCobrir(
        topoAtual: Float,
        altura: Float,
        topoDaBarra: Float,
        limiteAltura: Float,
    ): Float {
        if (topoAtual + altura <= limiteAltura) return topoAtual
        val maximo = (limiteAltura - altura).coerceAtLeast(0f)
        return topoDaBarra.coerceIn(0f, maximo)
    }

    fun alturaEntreABarraEOTeclado(
        desejada: Float,
        topoDaBarra: Float,
        limiteAltura: Float,
        minima: Float,
    ): Float {
        val disponivel = (limiteAltura - topoDaBarra).coerceAtLeast(0f)
        if (disponivel <= 0f) return desejada.coerceAtLeast(minima)
        val piso = minima.coerceAtMost(disponivel)
        return desejada.coerceIn(piso, disponivel)
    }
}
