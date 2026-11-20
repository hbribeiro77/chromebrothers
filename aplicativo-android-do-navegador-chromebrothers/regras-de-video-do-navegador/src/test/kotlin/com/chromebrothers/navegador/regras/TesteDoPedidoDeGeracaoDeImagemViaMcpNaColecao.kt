package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao {
    @Test
    fun exigePromptEQuantidadeEntre1e4() {
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"","quantidade":1}"""),
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"x","quantidade":5}"""),
        )
        val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"gato","modelo":"nano_banana_2_1","quantidade":2,"temReferencia":true,"proporcao":"16:9"}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals("higgsfield", ok.pedido.mcp)
        assertEquals(2, ok.pedido.quantidade)
        assertTrue(ok.pedido.temReferencia)
        assertEquals("16:9", ok.pedido.proporcao)
        assertEquals("1k", ok.pedido.resolucao)
    }

    @Test
    fun modeloEmBrancoViraNanoBanana21EProporcaoPadraoUmPorUm() {
        val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"gato","quantidade":1}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals(PedidoDeGeracaoDeImagemViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO, ok.pedido.modelo)
        assertEquals("1:1", ok.pedido.proporcao)
        assertEquals("1k", ok.pedido.resolucao)
    }

    @Test
    fun aceitaResolucao2kE4k() {
        val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"x","quantidade":1,"resolucao":"2k"}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals("2k", ok.pedido.resolucao)
    }

    @Test
    fun recusaProporcaoInvalida() {
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
                """{"mcp":"higgsfield","prompt":"x","quantidade":1,"proporcao":"21:9"}""",
            ),
        )
    }

    @Test
    fun recusaResolucaoInvalida() {
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
            PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
                """{"mcp":"higgsfield","prompt":"x","quantidade":1,"resolucao":"8k"}""",
            ),
        )
    }

    @Test
    fun recusaMcpDesconhecido() {
        val r = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"outro","prompt":"x","quantidade":1}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(r)
    }

    @Test
    fun aceitaSjinnComModeloPadraoGptImage2() {
        val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
            """{"mcp":"sjinn","prompt":"gato","quantidade":1}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals(PedidoDeGeracaoDeImagemViaMcpNaColecao.MCP_SJINN, ok.pedido.mcp)
        assertEquals(PedidoDeGeracaoDeImagemViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN, ok.pedido.modelo)
    }
}

class TesteDaResolucaoECatalogoDoSjinnNaColecao {
    @Test
    fun mapeia1k2k4kParaMaiusculasDoSjinn() {
        assertEquals("1K", ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn.paraApi("1k"))
        assertEquals("2K", ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn.paraApi("2k"))
        assertEquals("4K", ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn.paraApi("4k"))
    }

    @Test
    fun catalogoEstaticoDeImagemTemGptImage2ENanoBanana() {
        val itens = CatalogoDeModelosDeImagemDoSjinnNaColecao.itens()
        assertTrue(itens.any { it.first == "gpt-image-2" })
        assertTrue(itens.any { it.first == "nano-banana-2" })
        val json = CatalogoDeModelosDeImagemDoSjinnNaColecao.jsonDosItensParaPonte()
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("\"id\":\"gpt-image-2\""))
    }

    @Test
    fun mapeiaModeloParaToolTypeDaApi() {
        assertEquals(
            "gpt-image-2-api",
            CatalogoDeModelosDeImagemDoSjinnNaColecao.toolTypeDe("gpt-image-2"),
        )
        assertEquals(
            "nano-banana-image-2-api",
            CatalogoDeModelosDeImagemDoSjinnNaColecao.toolTypeDe("nano-banana-2"),
        )
    }

}

class TesteDaRespostaJsonDaPonteDeGeracaoMcp {
    @Test
    fun fileUrlDeCaminhoAbsolutoViraUriFile() {
        val uri = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho("/data/user/0/app/files/foto.jpg")
        assertTrue(uri.startsWith("file:"))
        assertTrue(uri.contains("foto.jpg"))
        assertTrue(AberturaDeMidiaLocalDoComponenteDaColecao.esquemaAceito(uri))
    }

    @Test
    fun jsonFinalIncluiFileUrlEPreview() {
        val json = RespostaJsonDaPonteDeGeracaoMcp.jsonFinalDosItens(
            itens = listOf(
                Triple("id-1", "file:///tmp/a.jpg", "data:image/jpeg;base64,xx"),
            ),
            erro = null,
        )
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("\"idBiblioteca\":\"id-1\""))
        assertTrue(json.contains("\"fileUrl\":\"file:///tmp/a.jpg\""))
        assertTrue(json.contains("\"previewDataUrl\":\"data:image/jpeg;base64,xx\""))
    }

    @Test
    fun jsonFinalParcialComErroMantemItens() {
        val json = RespostaJsonDaPonteDeGeracaoMcp.jsonFinalDosItens(
            itens = listOf(Triple("id-1", "file:///tmp/a.jpg", "data:image/jpeg;base64,a")),
            erro = "Falhou a 2ª",
        )
        assertTrue(json.contains("\"ok\":false"))
        assertTrue(json.contains("\"erro\":\"Falhou a 2ª\""))
        assertTrue(json.contains("\"idBiblioteca\":\"id-1\""))
    }
}
