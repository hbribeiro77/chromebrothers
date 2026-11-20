package com.chromebrothers.navegador.regras

object PlanilhaDeRolagemParaCapturaDaPaginaInteira {
    fun offsets(
        alturaDaPagina: Int,
        alturaDaJanela: Int,
        sobreposicao: Int,
        maximoDeFatias: Int,
    ): List<Int> {
        if (alturaDaJanela <= 0 || maximoDeFatias <= 0) return emptyList()
        if (alturaDaPagina <= alturaDaJanela) return listOf(0)
        val passo = (alturaDaJanela - sobreposicao.coerceAtLeast(0)).coerceAtLeast(1)
        val ultimo = (alturaDaPagina - alturaDaJanela).coerceAtLeast(0)
        val lista = mutableListOf<Int>()
        var y = 0
        while (lista.size < maximoDeFatias) {
            lista += y.coerceAtMost(ultimo)
            if (y >= ultimo) break
            y = (y + passo).coerceAtMost(ultimo)
            if (lista.last() == y) break
        }
        return lista
    }

    fun alturaUtilDaFatia(
        indice: Int,
        alturaDaJanela: Int,
        sobreposicao: Int,
        restoAteOFundo: Int,
    ): Int {
        if (alturaDaJanela <= 0 || restoAteOFundo <= 0) return 0
        val util = if (indice == 0) {
            alturaDaJanela
        } else {
            (alturaDaJanela - sobreposicao.coerceAtLeast(0)).coerceAtLeast(1)
        }
        return util.coerceAtMost(restoAteOFundo)
    }
}
