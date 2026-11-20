package com.chromebrothers.navegador.regras

object DestinoDoArrasteDaAbaNaFaixa {
    fun indice(origem: Int, deslocamento: Float, larguras: List<Float>): Int {
        if (larguras.isEmpty()) return 0
        val origemSegura = origem.coerceIn(0, larguras.lastIndex)
        val largura = larguras[origemSegura]
        if (deslocamento >= 0f) {
            var limite = 0f
            var indice = origemSegura
            for (i in origemSegura + 1 until larguras.size) {
                val proximo = limite + (largura + larguras[i]) / 2f
                if (deslocamento < proximo) break
                limite += larguras[i]
                indice = i
            }
            return indice
        }
        var limite = 0f
        var indice = origemSegura
        for (i in origemSegura - 1 downTo 0) {
            val proximo = limite - (largura + larguras[i]) / 2f
            if (deslocamento > proximo) break
            limite -= larguras[i]
            indice = i
        }
        return indice
    }
}

object DeslocamentoDoVizinhoNoArrasteDaAba {
    fun pixels(indice: Int, origem: Int, destino: Int, larguraArrastada: Float): Float {
        if (indice == origem) return 0f
        if (destino > origem && indice in (origem + 1)..destino) return -larguraArrastada
        if (destino < origem && indice in destino until origem) return larguraArrastada
        return 0f
    }
}
