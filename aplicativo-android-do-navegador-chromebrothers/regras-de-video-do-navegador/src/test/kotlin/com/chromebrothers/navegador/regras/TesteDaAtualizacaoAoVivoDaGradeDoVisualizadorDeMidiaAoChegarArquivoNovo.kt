package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaAtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo {
    private val regras = AtualizacaoAoVivoDaGradeDoVisualizadorDeMidiaAoChegarArquivoNovo

    @Test
    fun avisosEmRajadaViramUmaReleituraSo() {
        assertTrue(regras.ESPERA_PARA_JUNTAR_AVISOS_MS in 300L..1000L)
    }

    @Test
    fun soAquecePrimeiroOQueAindaNaoTemMiniaturaNaMemoria() {
        val novos = listOf("a", "b", "c", "d")
        val emCache = setOf("b", "d")
        assertEquals(listOf("a", "c"), regras.urisParaAquecerAntesDeMostrar(novos, emCache::contains))
    }

    @Test
    fun aquecimentoAntesDeMostrarTemTetoParaNaoAtrasarAGrade() {
        val novos = (1..100).map { "u$it" }
        val alvo = regras.urisParaAquecerAntesDeMostrar(novos, { false })
        assertEquals(regras.LIMITE_DE_AQUECIMENTO_ANTES_DE_MOSTRAR, alvo.size)
        assertEquals("u1", alvo.first())
    }

    @Test
    fun quemEstaNoTopoVeACartaNovaEQuemRolouFicaOndeEsta() {
        assertTrue(regras.deveVoltarAoTopoAposInserir(primeiraVisivelAntes = 0, inseriuNoTopo = true))
        assertFalse(regras.deveVoltarAoTopoAposInserir(primeiraVisivelAntes = 30, inseriuNoTopo = true))
        assertFalse(regras.deveVoltarAoTopoAposInserir(primeiraVisivelAntes = 0, inseriuNoTopo = false))
    }

    @Test
    fun detectaInsercaoNoTopo() {
        assertTrue(regras.inseriuNoTopo(antigos = listOf("b", "c"), novos = listOf("a", "b", "c")))
        assertFalse(regras.inseriuNoTopo(antigos = listOf("a", "b"), novos = listOf("a", "b")))
        assertFalse(regras.inseriuNoTopo(antigos = listOf("a", "b"), novos = listOf("b")))
        assertTrue(regras.inseriuNoTopo(antigos = emptyList(), novos = listOf("a")))
    }
}
