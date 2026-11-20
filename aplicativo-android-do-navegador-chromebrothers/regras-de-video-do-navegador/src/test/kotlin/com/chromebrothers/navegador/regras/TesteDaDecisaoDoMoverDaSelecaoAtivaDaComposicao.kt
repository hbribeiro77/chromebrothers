package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDecisaoDoMoverDaSelecaoAtivaDaComposicao {
    @Test
    fun marcadoCriaNovaCamada() {
        assertTrue(DecisaoDoMoverDaSelecaoAtivaDaComposicao.criaNovaCamada(preferenciaMarcada = true))
    }

    @Test
    fun desmarcadoMoveNaMesmaCamada() {
        assertFalse(DecisaoDoMoverDaSelecaoAtivaDaComposicao.criaNovaCamada(preferenciaMarcada = false))
    }

    @Test
    fun desmarcadoFundeAoEncerrarAMoldura() {
        assertTrue(
            DecisaoDoMoverDaSelecaoAtivaDaComposicao.fundirAoEncerrarTransformar(
                preferenciaNovaCamada = false,
            ),
        )
        assertFalse(
            DecisaoDoMoverDaSelecaoAtivaDaComposicao.fundirAoEncerrarTransformar(
                preferenciaNovaCamada = true,
            ),
        )
    }
}
