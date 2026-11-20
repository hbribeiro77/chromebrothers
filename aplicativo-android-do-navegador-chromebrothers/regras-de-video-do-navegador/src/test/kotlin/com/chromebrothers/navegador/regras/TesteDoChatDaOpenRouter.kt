package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoChatDaOpenRouter {
    @Test
    fun oPedidoLevaOModeloAsMensagensEOFluxo() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "openai/gpt-4o-mini",
            listOf(MensagemDeChat("user", "oi \"lá\"\npróxima")),
        )
        assertTrue(corpo.contains("\"model\":\"openai/gpt-4o-mini\""))
        assertTrue(corpo.contains("\"stream\":true"))
        assertTrue(corpo.contains("\"role\":\"user\""))
        assertTrue(corpo.contains("oi \\\"lá\\\"\\npróxima"))
        assertFalse(corpo.contains("\npróxima"))
        val cabecalhos = PedidoDeChatParaAOpenRouter.cabecalhos("  sk-abc  ")
        assertEquals("Bearer sk-abc", cabecalhos["Authorization"])
    }

    @Test
    fun oFluxoSeparaTextoFimEErro() {
        val texto = TrechosDaRespostaEmFluxoDaOpenRouter.interpretarLinha(
            """data: {"choices":[{"delta":{"content":"Olá \"mundo\""}}]}""",
        )
        assertIs<TrechoDaRespostaEmFluxo.Texto>(texto)
        assertEquals("Olá \"mundo\"", texto.conteudo)
        assertIs<TrechoDaRespostaEmFluxo.Fim>(
            TrechosDaRespostaEmFluxoDaOpenRouter.interpretarLinha("data: [DONE]"),
        )
        assertNull(TrechosDaRespostaEmFluxoDaOpenRouter.interpretarLinha(""))
        assertNull(TrechosDaRespostaEmFluxoDaOpenRouter.interpretarLinha(": ping"))
        val falha = TrechosDaRespostaEmFluxoDaOpenRouter.interpretarDados(
            """{"error":{"message":"chave inválida","code":401}}""",
        )
        assertIs<TrechoDaRespostaEmFluxo.Falha>(falha)
        assertEquals("chave inválida", falha.mensagem)
    }

    @Test
    fun oContextoDaPaginaCabeNoLimite() {
        val script = ContextoDaPaginaParaOChat.scriptDoTextoVisivel()
        assertTrue(script.contains("innerText"))
        assertTrue(script.contains("slice(0, 6000)"), script)
        val completo = ContextoDaPaginaParaOChat.scriptDoTextoCompletoNormalizado()
        assertTrue(completo.contains("innerText"))
        assertTrue(!completo.contains("6000"), completo)
        assertTrue(completo.contains("slice(0, 2000000)"), completo)
        val longo = "a".repeat(7000)
        val mensagem = ContextoDaPaginaParaOChat.mensagem("Gatos", "https://exemplo.test", longo)
        assertTrue(mensagem.startsWith("Página aberta: Gatos\nEndereço: https://exemplo.test\n\n"))
        assertEquals(6000, mensagem.substringAfter("\n\n").length)
        assertEquals(
            "Página aberta: Sem título\nEndereço: sem endereço",
            ContextoDaPaginaParaOChat.mensagem("  ", "", "  "),
        )
    }
}
