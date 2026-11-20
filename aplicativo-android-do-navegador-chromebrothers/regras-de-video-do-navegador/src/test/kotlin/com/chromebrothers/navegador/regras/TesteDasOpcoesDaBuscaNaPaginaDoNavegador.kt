package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasOpcoesDaBuscaNaPaginaDoNavegador {
    @Test
    fun debounceTemAtrasoDeCercaDeDuzentosMs() {
        assertEquals(180L, OpcoesDaBuscaNaPaginaDoNavegador.ATRASO_DO_DEBOUNCE_MS)
    }

    @Test
    fun destacaSemResultadoSoComTextoESemAchado() {
        assertTrue(OpcoesDaBuscaNaPaginaDoNavegador.destacaSemResultado(texto = "foo", encontrado = false))
        assertFalse(OpcoesDaBuscaNaPaginaDoNavegador.destacaSemResultado(texto = "foo", encontrado = true))
        assertFalse(OpcoesDaBuscaNaPaginaDoNavegador.destacaSemResultado(texto = "  ", encontrado = false))
        assertFalse(OpcoesDaBuscaNaPaginaDoNavegador.destacaSemResultado(texto = "", encontrado = false))
    }

    @Test
    fun aoTrocarDeAbaMantemABarraERepeteABusca() {
        assertTrue(OpcoesDaBuscaNaPaginaDoNavegador.repeteBuscaAoTrocarDeAba(barraAberta = true))
        assertFalse(OpcoesDaBuscaNaPaginaDoNavegador.repeteBuscaAoTrocarDeAba(barraAberta = false))
    }

    @Test
    fun enterSemShiftVaiParaProximoEComShiftParaAnterior() {
        assertFalse(OpcoesDaBuscaNaPaginaDoNavegador.buscaParaTrasNoEnter(shiftPressionado = false))
        assertTrue(OpcoesDaBuscaNaPaginaDoNavegador.buscaParaTrasNoEnter(shiftPressionado = true))
    }
}
