package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDoPedidoDeGeracaoDeVideoViaMcpNaColecao {
    @Test
    fun aceitaSjinnWan3ComDefaults() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcp":"sjinn","prompt":"drone sobre a praia","quantidade":1}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals("sjinn", ok.pedido.mcp)
        assertEquals(PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_SJINN, ok.pedido.modelo)
        assertEquals(5, ok.pedido.duracaoSegundos)
        assertEquals("480p", ok.pedido.resolucao)
        assertEquals("16:9", ok.pedido.proporcao)
        assertEquals(1, ok.pedido.quantidade)
    }

    @Test
    fun aceitaQuantidadeAteQuatro() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"x","quantidade":4,"proporcao":"9:16","resolucao":"720p","duracao":8}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals(4, ok.pedido.quantidade)
        assertEquals("9:16", ok.pedido.proporcao)
        assertEquals("720p", ok.pedido.resolucao)
        assertEquals(8, ok.pedido.duracaoSegundos)
    }

    @Test
    fun aceitaDuracaoEResolucao() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcp":"sjinn","prompt":"x","duracao":10,"resolucao":"720p","proporcao":"9:16"}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals(10, ok.pedido.duracaoSegundos)
        assertEquals("720p", ok.pedido.resolucao)
        assertEquals("720P", PedidoDeGeracaoDeVideoViaMcpNaColecao.resolucaoParaApi(ok.pedido.resolucao))
    }

    @Test
    fun recusaDuracaoForaDaFaixa() {
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Recusado>(
            PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
                """{"mcp":"sjinn","prompt":"x","duracao":1}""",
            ),
        )
    }

    @Test
    fun catalogoTemWan3ComoToolType() {
        assertEquals("wan3-video-api", CatalogoDeModelosDeVideoDoSjinnNaColecao.toolTypeDe("wan3"))
        assertTrue(CatalogoDeModelosDeVideoDoSjinnNaColecao.jsonDosItensParaPonte().contains("\"id\":\"wan3\""))
    }

    @Test
    fun aceitaHiggsfieldComModeloPadraoWan3() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcp":"higgsfield","prompt":"onda na praia"}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals("higgsfield", ok.pedido.mcp)
        assertEquals("wan3_0", ok.pedido.modelo)
        assertEquals(
            PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_HIGGSFIELD,
            ok.pedido.modelo,
        )
    }

    @Test
    fun listaDeMcpsDeVideoIncluiHiggsfieldESjinn() {
        val json = RespostaJsonDaPonteDeGeracaoDeVideoMcp.okMcps()
        assertTrue(json.contains("\"id\":\"higgsfield\""))
        assertTrue(json.contains("\"id\":\"sjinn\""))
        assertTrue(json.contains("\"id\":\"topview\""))
    }
}
