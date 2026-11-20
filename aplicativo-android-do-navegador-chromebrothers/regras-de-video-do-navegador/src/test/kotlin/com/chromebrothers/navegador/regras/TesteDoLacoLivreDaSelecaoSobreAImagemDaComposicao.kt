package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoLacoLivreDaSelecaoSobreAImagemDaComposicao {
    @Test
    fun soltarMantemOContornoAberto() {
        val aberto = listOf(
            PontoDoLacoLivre(0f, 0f),
            PontoDoLacoLivre(200f, 0f),
            PontoDoLacoLivre(200f, 80f),
        )
        assertEquals(aberto, LacoLivreDaSelecaoSobreAImagemDaComposicao.soltar(aberto))
        assertFalse(LacoLivreDaSelecaoSobreAImagemDaComposicao.deveFechar(aberto))
        assertFalse(LacoLivreDaSelecaoSobreAImagemDaComposicao.contem(100f, 20f, aberto))
    }

    @Test
    fun continuarLigaOFimDoTracoAoRecomeco() {
        val jaFeito = listOf(PontoDoLacoLivre(0f, 0f), PontoDoLacoLivre(200f, 0f))
        val retomada = listOf(PontoDoLacoLivre(210f, 40f), PontoDoLacoLivre(200f, 200f))
        val junto = LacoLivreDaSelecaoSobreAImagemDaComposicao.continuar(jaFeito, retomada)
        assertEquals(
            listOf(
                PontoDoLacoLivre(0f, 0f),
                PontoDoLacoLivre(200f, 0f),
                PontoDoLacoLivre(210f, 40f),
                PontoDoLacoLivre(200f, 200f),
            ),
            junto,
        )
    }

    @Test
    fun aoFecharAAreaOInteriorInteiroFicaSelecionado() {
        val quase = listOf(
            PontoDoLacoLivre(0f, 0f),
            PontoDoLacoLivre(200f, 0f),
            PontoDoLacoLivre(200f, 200f),
            PontoDoLacoLivre(0f, 200f),
            PontoDoLacoLivre(8f, 6f),
        )
        assertTrue(LacoLivreDaSelecaoSobreAImagemDaComposicao.deveFechar(quase))
        val fechado = LacoLivreDaSelecaoSobreAImagemDaComposicao.fechar(quase)
        assertEquals(quase.first(), fechado.last())
        assertTrue(LacoLivreDaSelecaoSobreAImagemDaComposicao.contem(100f, 100f, fechado))
        assertFalse(LacoLivreDaSelecaoSobreAImagemDaComposicao.contem(250f, 100f, fechado))
        val caixa = LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(fechado)
        assertEquals(0f, caixa?.esquerda)
        assertEquals(0f, caixa?.topo)
        assertEquals(200f, caixa?.largura)
        assertEquals(200f, caixa?.altura)
    }

    @Test
    fun desfazerUltimoTracoTiraSoOTrechoEReabreAArea() {
        val primeiro = listOf(PontoDoLacoLivre(0f, 0f), PontoDoLacoLivre(200f, 0f))
        val retomada = listOf(
            PontoDoLacoLivre(210f, 20f),
            PontoDoLacoLivre(200f, 200f),
            PontoDoLacoLivre(0f, 200f),
            PontoDoLacoLivre(8f, 6f),
        )
        val fechado = LacoLivreDaSelecaoSobreAImagemDaComposicao.fechar(
            LacoLivreDaSelecaoSobreAImagemDaComposicao.continuar(primeiro, retomada),
        )
        val (semUltimo, inicios) = LacoLivreDaSelecaoSobreAImagemDaComposicao.desfazerUltimoTraco(
            fechado,
            listOf(0, primeiro.size),
        )
        assertEquals(primeiro, semUltimo)
        assertEquals(listOf(0), inicios)
        assertFalse(LacoLivreDaSelecaoSobreAImagemDaComposicao.deveFechar(semUltimo))
        val (vazio, semInicios) = LacoLivreDaSelecaoSobreAImagemDaComposicao.desfazerUltimoTraco(semUltimo, inicios)
        assertEquals(emptyList(), vazio)
        assertEquals(emptyList(), semInicios)
    }

    @Test
    fun oDedoSoDesenhaOLacoQuandoEstaMarcado() {
        assertFalse(LacoLivreDaSelecaoSobreAImagemDaComposicao.segueOToque(ehCaneta = false, comODedo = false))
        assertTrue(LacoLivreDaSelecaoSobreAImagemDaComposicao.segueOToque(ehCaneta = true, comODedo = false))
        assertTrue(LacoLivreDaSelecaoSobreAImagemDaComposicao.segueOToque(ehCaneta = false, comODedo = true))
    }

    @Test
    fun umToqueSemContornoNaoSelecionaArea() {
        assertNull(LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(listOf(PontoDoLacoLivre(4f, 4f))))
        assertNull(LacoLivreDaSelecaoSobreAImagemDaComposicao.caixa(emptyList()))
    }

    @Test
    fun oPontoDoLacoFicaPresoNoQuadroInteiro() {
        assertEquals(
            PontoDoLacoLivre(0f, 100f),
            LacoLivreDaSelecaoSobreAImagemDaComposicao.pontoNaMoldura(-20f, 100f, 1920f, 1080f),
        )
        assertEquals(
            PontoDoLacoLivre(1920f, 1080f),
            LacoLivreDaSelecaoSobreAImagemDaComposicao.pontoNaMoldura(2000f, 2000f, 1920f, 1080f),
        )
    }
}
