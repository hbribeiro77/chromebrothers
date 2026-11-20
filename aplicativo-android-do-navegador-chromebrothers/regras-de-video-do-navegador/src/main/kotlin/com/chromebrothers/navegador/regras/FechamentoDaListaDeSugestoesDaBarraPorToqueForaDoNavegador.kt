package com.chromebrothers.navegador.regras

/**
 * Decide se um toque deve fechar a lista de sugestões (estilo Chrome: toque fora).
 */
object FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador {
    fun deveFechar(
        listaVisivel: Boolean,
        toqueNaCaixaDeEndereco: Boolean,
        toqueNaLista: Boolean,
    ): Boolean = listaVisivel && !toqueNaCaixaDeEndereco && !toqueNaLista
}
