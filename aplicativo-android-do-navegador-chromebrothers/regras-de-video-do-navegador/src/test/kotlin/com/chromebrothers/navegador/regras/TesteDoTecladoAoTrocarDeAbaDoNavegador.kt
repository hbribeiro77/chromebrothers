package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoTecladoAoTrocarDeAbaDoNavegador {
    @Test
    fun marcaRestauracaoSeTecladoAbertoSemFocoNoChrome() {
        assertTrue(
            TecladoAoTrocarDeAbaDoNavegador.deveMarcarRestauracao(
                tecladoVisivel = true,
                focoEmChromeNativo = false,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveMarcarRestauracao(
                tecladoVisivel = true,
                focoEmChromeNativo = true,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveMarcarRestauracao(
                tecladoVisivel = false,
                focoEmChromeNativo = false,
            ),
        )
    }

    @Test
    fun escondeNoDestinoSemRestauracaoNemChrome() {
        assertTrue(
            TecladoAoTrocarDeAbaDoNavegador.deveEsconderAoEntrar(
                destinoMarcadoParaRestaurar = false,
                focoEmChromeNativo = false,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveEsconderAoEntrar(
                destinoMarcadoParaRestaurar = true,
                focoEmChromeNativo = false,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveEsconderAoEntrar(
                destinoMarcadoParaRestaurar = false,
                focoEmChromeNativo = true,
            ),
        )
    }

    @Test
    fun restauraNoDestinoMarcadoSemFocoNoChrome() {
        assertTrue(
            TecladoAoTrocarDeAbaDoNavegador.deveRestaurarAoEntrar(
                destinoMarcadoParaRestaurar = true,
                focoEmChromeNativo = false,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveRestaurarAoEntrar(
                destinoMarcadoParaRestaurar = true,
                focoEmChromeNativo = true,
            ),
        )
        assertFalse(
            TecladoAoTrocarDeAbaDoNavegador.deveRestaurarAoEntrar(
                destinoMarcadoParaRestaurar = false,
                focoEmChromeNativo = false,
            ),
        )
    }
}
