package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaGeracaoDeArquivoPeloChatDaOpenRouter {
    @Test
    fun interpretaMarkdownComImagens() {
        val bruto = """
            {"titulo":"Aula 1","conteudo":"# Olá\n\nVeja:","imagens":[{"endereco":"https://exemplo.test/a.png","nome":"a.png"}]}
        """.trimIndent()
        val pronto = GeracaoDeArquivoPeloChatDaOpenRouter.lerMarkdown(bruto)
            as ResultadoDaGeracaoDeMarkdownDoChat.Pronto
        assertEquals("Aula 1", pronto.pedido.titulo)
        assertEquals(1, pronto.pedido.imagens.size)
        assertEquals("https://exemplo.test/a.png", pronto.pedido.imagens[0].endereco)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pronto.pedido).contains("markdown"))
    }

    @Test
    fun aceitaFileLocalNasImagensDoMarkdown() {
        val bruto = """
            {"titulo":"Captura","conteudo":"x","imagens":[{"endereco":"file:///data/captura.png","nome":"captura.png"}]}
        """.trimIndent()
        val pronto = GeracaoDeArquivoPeloChatDaOpenRouter.lerMarkdown(bruto)
            as ResultadoDaGeracaoDeMarkdownDoChat.Pronto
        assertEquals("file:///data/captura.png", pronto.pedido.imagens[0].endereco)
    }

    @Test
    fun interpretaZipComItemEMarkdownEmbutido() {
        val bruto = """
            {"nome":"pacote","itens":[{"endereco":"https://exemplo.test/v.mp4","caminho_no_zip":"videos/v.mp4"}],"conteudo_markdown":"# Pacote"}
        """.trimIndent()
        val pronto = GeracaoDeArquivoPeloChatDaOpenRouter.lerZip(bruto)
            as ResultadoDaGeracaoDeZipDoChat.Pronto
        assertEquals("pacote", pronto.pedido.nome)
        assertEquals(1, pronto.pedido.itens.size)
        assertEquals("# Pacote", pronto.pedido.conteudoMarkdown)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pronto.pedido).contains("ZIP"))
    }

    @Test
    fun nomeDeArquivoRemoveCaracteresPerigosos() {
        assertEquals(
            "Aula_1_resumo",
            GeracaoDeArquivoPeloChatDaOpenRouter.nomeDeArquivoSeguro("Aula 1 / resumo?"),
        )
    }

    @Test
    fun reescreveConteudoComAssetsBaixados() {
        val saida = GeracaoDeArquivoPeloChatDaOpenRouter.reescreverComAssets(
            "# Titulo\n\n![x](https://exemplo.test/a.png)",
            mapOf("https://exemplo.test/a.png" to "assets/a.png"),
        )
        assertTrue(saida.contains("assets/a.png"))
        assertTrue(!saida.contains("https://exemplo.test/a.png"))
    }

    @Test
    fun siteLiberadoSegueSemPerguntarParaMarkdownColecaoEZip() {
        val md = PedidoDeAcaoConfirmadaDoChat.GerarMarkdown(
            titulo = "t",
            conteudo = "c",
            imagens = emptyList(),
        )
        val zip = PedidoDeAcaoConfirmadaDoChat.GerarZip(
            nome = "p",
            itens = emptyList(),
            conteudoMarkdown = null,
        )
        val pagina = PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao("t", "<p>a</p>")
        val sites = listOf("sjinn.ai")
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(md, "https://sjinn.ai/aula", sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(zip, "https://sjinn.ai/aula", sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(pagina, "https://sjinn.ai/aula", sites))
    }
}
