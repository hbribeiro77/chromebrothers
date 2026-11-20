package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoLeitorDeMarkdownNaColecaoDoNavegador {
    @Test
    fun slugDoTituloESeguro() {
        assertEquals(
            "plano-topview",
            LeitorDeMarkdownNaColecaoDoNavegador.slugDoTitulo("Plano Topview!"),
        )
        assertEquals(
            "documento",
            LeitorDeMarkdownNaColecaoDoNavegador.slugDoTitulo("???"),
        )
    }

    @Test
    fun htmlDoLeitorContemTituloECorpo() {
        val html = LeitorDeMarkdownNaColecaoDoNavegador.htmlDoLeitor(
            titulo = "Resumo",
            markdown = "# Olá\n\nFeito o **passo** um.",
        )
        assertTrue(html.contains("Resumo"))
        assertTrue(html.contains("Olá") || html.contains("<h"))
        assertTrue(html.contains("passo"))
        assertTrue(html.contains("<!DOCTYPE html>") || html.contains("<html"))
    }

    @Test
    fun caminhoRelativoNaPastaMarkdown() {
        assertEquals(
            "ChromeBrothers/markdown",
            LeitorDeMarkdownNaColecaoDoNavegador.PASTA_RELATIVA_DOWNLOADS,
        )
    }

    @Test
    fun interpretaMostrarMarkdownNaColecao() {
        val args = """{"titulo":"Plano","conteudo":"# Passo\n\nFeito."}"""
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.MOSTRAR_MARKDOWN,
            args,
        )
        assertTrue(resultado is ResultadoDaInterpretacaoDaAcaoDoChat.Pronta)
        val pedido = (resultado as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertTrue(pedido is PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao)
        assertEquals("Plano", (pedido as PedidoDeAcaoConfirmadaDoChat.MostrarMarkdownNaColecao).titulo)
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                pedido,
                "https://sjinn.ai",
                listOf("sjinn.ai"),
            ),
        )
        assertTrue(
            AcoesConfirmadasDoChatDaOpenRouter.definicoes()
                .any { it.contains("mostrar_markdown_na_colecao") },
        )
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: teste")
                .contains("mostrar_markdown_na_colecao"),
        )
        assertTrue(PlaybooksDoAgenteDoChatDoChromebrothers.texto().contains("mostrar_markdown_na_colecao"))
    }

    @Test
    fun recusaSemConteudo() {
        val resultado = LeitorDeMarkdownNaColecaoDoNavegador.lerPedido("""{"titulo":"X"}""")
        assertTrue(resultado is ResultadoDoLeitorDeMarkdownNaColecao.Recusado)
    }
}
