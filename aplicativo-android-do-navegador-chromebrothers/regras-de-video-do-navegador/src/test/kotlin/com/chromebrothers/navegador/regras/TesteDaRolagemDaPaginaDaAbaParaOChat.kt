package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDaRolagemDaPaginaDaAbaParaOChat {
    private val rolagem = RolagemDaPaginaDaAbaParaOChat

    private fun pedido(json: String): PedidoDeRolagemDaPagina =
        assertIs<ResultadoDoPedidoDeRolagem.Ok>(rolagem.lerPedido(json)).pedido

    @Test
    fun semArgumentosDevolveErro() {
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("{}"))
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido(""))
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("""{"seletor":"  "}"""))
    }

    @Test
    fun seletorTemPrioridadeSobreTodosOsOutros() {
        val lido = pedido("""{"seletor":"#rodape","y":10,"paginas":2,"pixels":50}""")
        assertEquals(PedidoDeRolagemDaPagina.PorSeletor("#rodape"), lido)
    }

    @Test
    fun yTemPrioridadeSobrePaginasEPixels() {
        val lido = pedido("""{"y":120,"paginas":2,"pixels":50}""")
        assertEquals(PedidoDeRolagemDaPagina.Absoluto(120), lido)
    }

    @Test
    fun yZeroEhValidoEVaiParaOTopo() {
        assertEquals(PedidoDeRolagemDaPagina.Absoluto(0), pedido("""{"y":0}"""))
    }

    @Test
    fun yNegativoViraZero() {
        assertEquals(PedidoDeRolagemDaPagina.Absoluto(0), pedido("""{"y":-30}"""))
    }

    @Test
    fun paginasTemPrioridadeSobrePixels() {
        val lido = pedido("""{"paginas":1.5,"pixels":50}""")
        assertEquals(PedidoDeRolagemDaPagina.PorPaginas(1.5), lido)
    }

    @Test
    fun paginasNegativasSaoAceitasParaRolarParaCima() {
        assertEquals(PedidoDeRolagemDaPagina.PorPaginas(-1.0), pedido("""{"paginas":-1}"""))
    }

    @Test
    fun paginasZeroEhValidoENaoCaiParaPixels() {
        assertEquals(PedidoDeRolagemDaPagina.PorPaginas(0.0), pedido("""{"paginas":0,"pixels":200}"""))
    }

    @Test
    fun yPresenteMasInvalidoDevolveErroEmVezDeCairParaPaginas() {
        val erro = assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("""{"y":"abc","paginas":2}"""))
        assertTrue(erro.mensagem.contains("y"))
    }

    @Test
    fun paginasPresenteMasInvalidoDevolveErroEmVezDeCairParaPixels() {
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("""{"paginas":"muitas","pixels":50}"""))
    }

    @Test
    fun pixelsPresenteMasInvalidoDevolveErro() {
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("""{"pixels":"abc"}"""))
    }

    @Test
    fun chaveComValorNullEhTratadaComoAusente() {
        assertEquals(PedidoDeRolagemDaPagina.Relativo(50), pedido("""{"y":null,"paginas":null,"pixels":50}"""))
    }

    @Test
    fun seletorEmBrancoCaiParaAsOutrasChaves() {
        assertEquals(PedidoDeRolagemDaPagina.Absoluto(7), pedido("""{"seletor":"   ","y":7}"""))
    }

    @Test
    fun apenasPixelsDevolveRelativo() {
        assertEquals(PedidoDeRolagemDaPagina.Relativo(-300), pedido("""{"pixels":-300}"""))
    }

    @Test
    fun seletorAcimaDeTrezentosCaracteresDevolveErro() {
        val longo = "a".repeat(RolagemDaPaginaDaAbaParaOChat.LIMITE_DO_SELETOR + 1)
        assertIs<ResultadoDoPedidoDeRolagem.Erro>(rolagem.lerPedido("""{"seletor":"$longo","y":5}"""))
        val noLimite = "a".repeat(RolagemDaPaginaDaAbaParaOChat.LIMITE_DO_SELETOR)
        assertIs<ResultadoDoPedidoDeRolagem.Ok>(rolagem.lerPedido("""{"seletor":"$noLimite"}"""))
    }

    @Test
    fun scriptPorSeletorUsaScrollIntoViewEEscapaOSeletor() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.PorSeletor("a[href=\"x\"]"))
        assertTrue(js.contains("scrollIntoView"))
        assertTrue(js.contains("querySelector"))
        assertTrue(js.contains("\"a[href=\\\"x\\\"]\""))
        assertTrue(js.contains("scrollY") && js.contains("scrollHeight") && js.contains("innerHeight"))
    }

    @Test
    fun scriptAbsolutoUsaScrollToComOYPedido() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.Absoluto(450))
        assertTrue(js.contains("scrollTo"))
        assertTrue(js.contains("450"))
    }

    @Test
    fun scriptPorPaginasMultiplicaPelaAlturaDaViewport() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.PorPaginas(1.5))
        assertTrue(js.contains("scrollBy"))
        assertTrue(js.contains("1.5"))
        assertTrue(js.contains("innerHeight"))
    }

    @Test
    fun scriptRelativoUsaScrollBy() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.Relativo(-300))
        assertTrue(js.contains("scrollBy"))
        assertTrue(js.contains("-300"))
    }

    @Test
    fun scriptRetornaOkOuErroSeparadosPorTab() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.Absoluto(0))
        assertTrue(js.contains("\"ok\\t\""))
        assertTrue(js.contains("\"erro\\t"))
    }

    @Test
    fun scriptNaoVazaErrorMessageDoNavegadorNoCatch() {
        val js = rolagem.script(PedidoDeRolagemDaPagina.Absoluto(0))
        assertTrue(js.contains("erro\\tNão deu para rolar a página."))
        assertTrue(!js.contains("erro.message"))
    }

    @Test
    fun mensagemFormataPosicaoEAlturas() {
        val texto = rolagem.mensagem("ok\t300\t5000\t800")
        assertTrue(texto.contains("y=300"))
        assertTrue(texto.contains("altura_pagina=5000"))
        assertTrue(texto.contains("altura_viewport=800"))
        assertTrue(texto.contains("no_fim=nao"))
    }

    @Test
    fun mensagemMarcaNoFimQuandoYMaisViewportAlcancaAlturaMenosDois() {
        assertTrue(rolagem.mensagem("ok\t4200\t5000\t800").contains("no_fim=sim"))
        assertTrue(rolagem.mensagem("ok\t4198\t5000\t800").contains("no_fim=sim"))
        assertTrue(rolagem.mensagem("ok\t4197\t5000\t800").contains("no_fim=nao"))
    }

    @Test
    fun mensagemRepassaErroDoScript() {
        val texto = rolagem.mensagem("erro\tElemento não encontrado.")
        assertTrue(texto.contains("Elemento não encontrado."))
        assertTrue(!texto.contains("no_fim="))
    }

    @Test
    fun mensagemComRespostaVaziaOuInesperadaNaoQuebra() {
        assertTrue(rolagem.mensagem("").isNotBlank())
        assertTrue(rolagem.mensagem("lixo").isNotBlank())
        assertTrue(rolagem.mensagem("ok\tabc").isNotBlank())
    }
}
