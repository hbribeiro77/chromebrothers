package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaAbaDoVisorDeMidiaDoNavegador {
    private val regra = AbaDoVisorDeMidiaDoNavegador

    @Test
    fun voltaParaAAbaDeOndeVeioSeElaAindaExiste() {
        assertEquals(2L, regra.abaParaVoltar(2L, 9L, listOf(1L, 2L, 9L)))
        assertNull(regra.abaParaVoltar(3L, 9L, listOf(1L, 2L, 9L)), "fechada no meio-tempo: a gestão escolhe")
        assertNull(regra.abaParaVoltar(9L, 9L, listOf(1L, 9L)))
        assertNull(regra.abaParaVoltar(null, 9L, listOf(1L, 9L)))
    }

    @Test
    fun abrirOutraMidiaComOVisorAtivoNaoEsqueceDeOndeVeio() {
        assertEquals(2L, regra.deOndeVeio(idAtiva = 2L, idDoVisor = 9L, anterior = 1L))
        assertEquals(1L, regra.deOndeVeio(idAtiva = 9L, idDoVisor = 9L, anterior = 1L))
        assertEquals(4L, regra.deOndeVeio(idAtiva = 4L, idDoVisor = null, anterior = null))
    }

    @Test
    fun navegarParaUmEnderecoDeVerdadeTiraDoVisor() {
        assertTrue(regra.navegacaoTiraDoVisor("https://exemplo.com"))
        assertTrue(regra.navegacaoTiraDoVisor("https://www.google.com/search?q=gato"))
        assertFalse(regra.navegacaoTiraDoVisor("about:blank"), "a página vazia do próprio Visor")
        assertFalse(regra.navegacaoTiraDoVisor(""))
        assertFalse(regra.navegacaoTiraDoVisor(null))
    }

    @Test
    fun visorNaoEhGravadoEAAtivaViraADeOndeVeio() {
        val (ids, ativa) = regra.paraGravar(listOf(1L, 2L, 9L), idAtiva = 9L, idDoVisor = 9L, idDeOndeVeio = 2L)
        assertEquals(listOf(1L, 2L), ids)
        assertEquals(1, ativa)
        val (semVisor, ativa2) = regra.paraGravar(listOf(1L, 2L), idAtiva = 1L, idDoVisor = null, idDeOndeVeio = null)
        assertEquals(listOf(1L, 2L), semVisor)
        assertEquals(0, ativa2)
    }
}
