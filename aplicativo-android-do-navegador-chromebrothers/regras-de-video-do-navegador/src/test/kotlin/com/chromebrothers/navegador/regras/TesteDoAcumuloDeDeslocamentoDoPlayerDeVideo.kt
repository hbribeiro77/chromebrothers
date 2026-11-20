package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoAcumuloDeDeslocamentoDoPlayerDeVideo {
    @Test
    fun incrementoForaDaListaVoltaParaDezSegundos() {
        assertEquals(10, IncrementosDeDeslocamentoDoPlayerDeVideo.ajustar(7))
        assertEquals(listOf(2, 5, 10, 30), IncrementosDeDeslocamentoDoPlayerDeVideo.opcoes)
    }

    @Test
    fun toquesSeguidosSomamOAviso() {
        val acumulo = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo()
        assertEquals("+10", acumulo.registrar(10, 1_000).texto)
        val segundo = acumulo.registrar(10, 1_800)
        assertEquals("+20", segundo.texto)
        assertTrue(segundo.visivel)
    }

    @Test
    fun toqueNoSentidoContrarioDiminuiOAviso() {
        val acumulo = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo()
        acumulo.registrar(10, 1_000)
        val aviso = acumulo.registrar(-10, 1_500)
        assertEquals("", aviso.texto)
        assertFalse(aviso.visivel)
    }

    @Test
    fun toqueDepoisDaJanelaComecaDeNovo() {
        val acumulo = AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo(janelaMillis = 1200)
        acumulo.registrar(10, 1_000)
        assertEquals("-5", acumulo.registrar(-5, 2_300).texto)
    }
}
