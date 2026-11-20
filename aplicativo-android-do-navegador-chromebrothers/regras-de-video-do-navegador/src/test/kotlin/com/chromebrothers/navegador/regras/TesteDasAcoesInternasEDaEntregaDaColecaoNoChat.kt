package com.chromebrothers.navegador.regras

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class TesteDasAcoesInternasEDaEntregaDaColecaoNoChat {
    @Test
    fun acoesDentroDoAppSeguemSemPerguntar() {
        listOf(
            PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao("t", "<p>oi</p>"),
            PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao("t", "# oi"),
            PedidoDeAcaoConfirmadaDoChat.AbrirVideoDaBiblioteca("k"),
            PedidoDeAcaoConfirmadaDoChat.AbrirImagemDaBiblioteca("i"),
            PedidoDeAcaoConfirmadaDoChat.CriarAba(""),
        ).forEach { assertTrue(AcoesInternasDoChatSemConfirmacao.segueSemPerguntar(it), it.toString()) }
    }

    @Test
    fun acoesNoSiteCreditosENotasContinuamPedindo() {
        listOf(
            PedidoDeAcaoConfirmadaDoChat.Clicar("Menu"),
            PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina("oi"),
            PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://example.com"),
            PedidoDeAcaoConfirmadaDoChat.CriarAba("https://example.com"),
            PedidoDeAcaoConfirmadaDoChat.GuardarNota("", "x"),
            PedidoDeAcaoConfirmadaDoChat.ApagarComponenteDaBiblioteca("id"),
            PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield("p", ""),
        ).forEach { assertFalse(AcoesInternasDoChatSemConfirmacao.segueSemPerguntar(it), it.toString()) }
    }

    @Test
    fun umaJanelaPorRespostaEAparecendoSoNoFim() {
        val entrega = EntregaDaColecaoPorRespostaDoChat<String>()
        assertNull(entrega.reaproveitar { true })
        entrega.iniciar()
        assertNull(entrega.reaproveitar { true })
        entrega.guardar("janela-1")
        assertEquals("janela-1", entrega.reaproveitar { true })
        assertEquals("janela-1", entrega.concluir())
        assertFalse(entrega.emAndamento)
        assertNull(entrega.concluir())
    }

    @Test
    fun janelaFechadaNoMeioNaoEReaproveitada() {
        val entrega = EntregaDaColecaoPorRespostaDoChat<String>()
        entrega.iniciar()
        entrega.guardar("janela-1")
        assertNull(entrega.reaproveitar { false })
        assertNull(entrega.concluir())
    }

    @Test
    fun foraDeUmaRespostaNaoGuardaNada() {
        val entrega = EntregaDaColecaoPorRespostaDoChat<String>()
        entrega.guardar("solta")
        assertNull(entrega.concluir())
    }
}
