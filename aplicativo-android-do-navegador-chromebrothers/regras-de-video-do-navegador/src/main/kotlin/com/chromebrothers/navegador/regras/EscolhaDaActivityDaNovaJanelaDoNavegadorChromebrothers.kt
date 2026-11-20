package com.chromebrothers.navegador.regras

object EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers {
    const val SLOT_PRIMARIA = 0
    const val SLOT_SECUNDARIA = 1
    const val SLOT_TERCIARIA = 2

    /**
     * Dado quais slots já estão ocupados (0..2), devolve o próximo slot livre
     * ou null se o limite foi atingido.
     */
    fun proximoSlotLivre(ocupados: Set<Int>): Int? {
        val candidatos = listOf(SLOT_PRIMARIA, SLOT_SECUNDARIA, SLOT_TERCIARIA)
        return candidatos.firstOrNull { it !in ocupados }
    }
}
