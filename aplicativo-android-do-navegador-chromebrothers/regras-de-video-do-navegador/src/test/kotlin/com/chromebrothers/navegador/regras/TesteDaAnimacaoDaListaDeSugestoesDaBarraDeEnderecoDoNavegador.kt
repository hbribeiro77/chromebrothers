package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaAnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador {
    @Test
    fun deslocamentoUsaFracaoOuMinimo() {
        val densidade = 2f
        val minimo = -(28f * densidade)
        assertEquals(
            minimo,
            AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deslocamentoInicialEmPixels(0, densidade),
        )
        // altura 200 → 40% = 80; com densidade 2 mínimo = 56; fica -80
        assertEquals(
            -80f,
            AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deslocamentoInicialEmPixels(200, densidade),
        )
    }

    @Test
    fun soAnimaAberturaQuandoAindaNaoVisivel() {
        assertTrue(AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deveAnimarAbertura(false))
        assertFalse(AnimacaoDaListaDeSugestoesDaBarraDeEnderecoDoNavegador.deveAnimarAbertura(true))
    }
}
