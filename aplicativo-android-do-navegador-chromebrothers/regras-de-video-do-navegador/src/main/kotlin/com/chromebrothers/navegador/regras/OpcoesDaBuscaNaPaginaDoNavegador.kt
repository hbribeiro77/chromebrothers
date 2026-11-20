package com.chromebrothers.navegador.regras

object OpcoesDaBuscaNaPaginaDoNavegador {
    const val ATRASO_DO_DEBOUNCE_MS = 180L

    fun destacaSemResultado(texto: String, encontrado: Boolean): Boolean =
        texto.trim().isNotEmpty() && !encontrado

    fun repeteBuscaAoTrocarDeAba(barraAberta: Boolean): Boolean = barraAberta

    fun buscaParaTrasNoEnter(shiftPressionado: Boolean): Boolean = shiftPressionado
}
