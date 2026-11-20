package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDaGeracaoDeImagemEmVariosMcps {
    private val pedido = PedidoDeGeracaoDeImagemViaMcpNaColecao

    @Test
    fun leListaDeMcpsEUsaFamiliaPadraoEmComum() {
        val ok = pedido.ler("""{"mcps":["higgsfield","sjinn"],"prompt":"gato","quantidade":2}""")
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals(listOf("higgsfield", "sjinn"), ok.pedido.mcps)
        assertEquals("higgsfield", ok.pedido.mcp)
        assertEquals(EquivalenciaDeModelosDeImagemEntreMcps.FAMILIA_PADRAO, ok.pedido.modelo)
        assertEquals("nano_banana_2_1", ok.pedido.modeloPara("higgsfield"))
        assertEquals("nano-banana-2.1", ok.pedido.modeloPara("sjinn"))
    }

    @Test
    fun semListaCaiNoMcpUnicoComoAntes() {
        val ok = pedido.ler("""{"mcp":"sjinn","prompt":"gato","quantidade":1,"modelo":"gpt-image-2"}""")
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
        assertEquals(listOf("sjinn"), ok.pedido.mcps)
        assertEquals("gpt-image-2", ok.pedido.modeloPara("sjinn"))
    }

    @Test
    fun variosMcpsRecusamModeloQueNaoExisteEmTodos() {
        val r = pedido.ler("""{"mcps":["higgsfield","sjinn"],"prompt":"x","quantidade":1,"modelo":"gpt-image-2"}""")
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(r)
    }

    @Test
    fun recusaMcpDesconhecidoNaLista() {
        val r = pedido.ler("""{"mcps":["higgsfield","outro"],"prompt":"x","quantidade":1}""")
        assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(r)
    }

    @Test
    fun intersecaoTemSoNanoBanana() {
        val ids = EquivalenciaDeModelosDeImagemEntreMcps.intersecao(listOf("higgsfield", "sjinn")).map { it.id }
        assertEquals(listOf("nano-banana-2-1", "nano-banana-2", "nano-banana-pro"), ids)
        val json = EquivalenciaDeModelosDeImagemEntreMcps.jsonDaIntersecao(listOf("sjinn", "higgsfield"))
        assertTrue(json.contains("\"nome\":\"Nano Banana Pro\""))
    }

    @Test
    fun idLocalDeQualquerMcpTraduzParaOOutro() {
        assertEquals("nano-banana-pro", EquivalenciaDeModelosDeImagemEntreMcps.idParaMcp("nano_banana_pro", "sjinn"))
        assertEquals("nano_banana_2", EquivalenciaDeModelosDeImagemEntreMcps.idParaMcp("nano-banana-2", "higgsfield"))
        assertEquals("seedream_x", EquivalenciaDeModelosDeImagemEntreMcps.idParaMcp("seedream_x", "sjinn"))
    }

    @Test
    fun jsonFinalLevaOMcpDeCadaImagem() {
        val json = RespostaJsonDaPonteDeGeracaoMcp.jsonFinalDasImagens(
            listOf(
                ImagemGeradaNaColecao("a", "file:///a.png", "data:x", "higgsfield"),
                ImagemGeradaNaColecao("b", "file:///b.png", "data:y"),
            ),
            "Sjinn: sem crédito",
        )
        assertTrue(json.contains("\"mcp\":\"higgsfield\""))
        assertEquals(1, Regex("\"mcp\"").findAll(json).count())
        assertTrue(json.contains("\"ok\":false"))
        assertFalse(json.contains("\"mcp\":\"\""))
    }
}
