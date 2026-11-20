package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoTituloDaConversaDoChatDaOpenRouter {
    @Test
    fun tituloPegaPrimeiraMensagemUserCortada() {
        val msgs = listOf(
            MensagemDeChat("system", "x"),
            MensagemDeChat("user", "a".repeat(100)),
        )
        val t = TituloDaConversaDoChatDaOpenRouter.gerar(msgs, maximo = 60)
        assertEquals(60, t.length)
        assertTrue(t.startsWith("aaa"))
    }

    @Test
    fun tituloUsaApenasAPerguntaQuandoMensagemTemContextoDaPagina() {
        val msgs = listOf(
            MensagemDeChat("user", "Título: Página X\nURL: https://exemplo.com\n\nPergunta:\nQual é o resumo?"),
        )
        assertEquals("Qual é o resumo?", TituloDaConversaDoChatDaOpenRouter.gerar(msgs))
    }

    @Test
    fun semUserUsaConversa() {
        assertEquals("Conversa", TituloDaConversaDoChatDaOpenRouter.gerar(emptyList()))
    }

    @Test
    fun tituloColapsaEspacosEUsaConversaQuandoUserFicaVazio() {
        val msgs = listOf(
            MensagemDeChat("system", "ignore"),
            MensagemDeChat("user", "  olá   mundo \n novo  "),
        )
        assertEquals("olá mundo novo", TituloDaConversaDoChatDaOpenRouter.gerar(msgs))
        assertEquals(
            "Conversa",
            TituloDaConversaDoChatDaOpenRouter.gerar(listOf(MensagemDeChat("user", " \n\t "))),
        )
    }
}
