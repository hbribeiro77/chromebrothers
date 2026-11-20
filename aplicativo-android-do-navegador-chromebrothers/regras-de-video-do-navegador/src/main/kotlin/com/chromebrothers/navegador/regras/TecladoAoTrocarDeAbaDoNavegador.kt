package com.chromebrothers.navegador.regras

/**
 * Ao trocar de aba: esconde o IME se o destino não precisa dele;
 * marca a aba de origem para reabrir o teclado ao voltar (foco web).
 */
object TecladoAoTrocarDeAbaDoNavegador {
    fun deveMarcarRestauracao(tecladoVisivel: Boolean, focoEmChromeNativo: Boolean): Boolean =
        tecladoVisivel && !focoEmChromeNativo

    fun deveEsconderAoEntrar(destinoMarcadoParaRestaurar: Boolean, focoEmChromeNativo: Boolean): Boolean =
        !destinoMarcadoParaRestaurar && !focoEmChromeNativo

    fun deveRestaurarAoEntrar(destinoMarcadoParaRestaurar: Boolean, focoEmChromeNativo: Boolean): Boolean =
        destinoMarcadoParaRestaurar && !focoEmChromeNativo
}
