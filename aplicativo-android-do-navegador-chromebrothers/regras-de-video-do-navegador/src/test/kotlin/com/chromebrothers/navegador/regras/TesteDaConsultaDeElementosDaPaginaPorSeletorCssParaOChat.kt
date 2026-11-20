package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDaConsultaDeElementosDaPaginaPorSeletorCssParaOChat {
    private val consulta = ConsultaDeElementosDaPaginaPorSeletorCssParaOChat

    @Test
    fun seletorVazioOuAusenteDevolveErro() {
        assertIs<ResultadoDoPedidoDeConsulta.Erro>(consulta.lerPedido("""{"seletor":""}"""))
        assertIs<ResultadoDoPedidoDeConsulta.Erro>(consulta.lerPedido("""{"seletor":"   "}"""))
        assertIs<ResultadoDoPedidoDeConsulta.Erro>(consulta.lerPedido("{}"))
    }

    @Test
    fun seletorAcimaDeTrezentosCaracteresDevolveErro() {
        val longo = "a".repeat(consulta.LIMITE_DO_SELETOR + 1)
        assertIs<ResultadoDoPedidoDeConsulta.Erro>(consulta.lerPedido("""{"seletor":"$longo"}"""))
        val noLimite = "a".repeat(consulta.LIMITE_DO_SELETOR)
        assertIs<ResultadoDoPedidoDeConsulta.Ok>(consulta.lerPedido("""{"seletor":"$noLimite"}"""))
    }

    @Test
    fun pedidoValidoUsaOffsetZeroELimiteDeQuarentaPorPadrao() {
        val pedido = assertIs<ResultadoDoPedidoDeConsulta.Ok>(consulta.lerPedido("""{"seletor":"article.card"}"""))
        assertEquals("article.card", pedido.seletor)
        assertEquals(0, pedido.offset)
        assertEquals(40, pedido.limite)
    }

    @Test
    fun offsetELimiteSaoAjustadosAosTetos() {
        val pedido = assertIs<ResultadoDoPedidoDeConsulta.Ok>(
            consulta.lerPedido("""{"seletor":"a","offset":-3,"limite":999}"""),
        )
        assertEquals(0, pedido.offset)
        assertEquals(40, pedido.limite)
        val outro = assertIs<ResultadoDoPedidoDeConsulta.Ok>(
            consulta.lerPedido("""{"seletor":"a","offset":5,"limite":7}"""),
        )
        assertEquals(5, outro.offset)
        assertEquals(7, outro.limite)
        val zerado = assertIs<ResultadoDoPedidoDeConsulta.Ok>(
            consulta.lerPedido("""{"seletor":"a","limite":0}"""),
        )
        assertEquals(40, zerado.limite)
    }

    @Test
    fun scriptUsaQuerySelectorAllSemEvalEComSeletorEscapado() {
        val script = consulta.script("a[title=\"x\"]", 0, 40)
        assertTrue(script.contains("querySelectorAll"))
        assertFalse(script.contains("eval("))
        assertFalse(script.contains("new Function"))
        assertTrue(script.contains("try"))
        assertTrue(script.contains("catch"))
        assertTrue(script.contains("""a[title=\"x\"]"""))
        assertTrue(script.contains("https?:"))
    }

    @Test
    fun scriptDevolveErroFixoEmPortuguesQuandoOSeletorCssEInvalido() {
        val script = consulta.script("a", 0, 40)
        assertTrue(script.contains("""erro\tSeletor CSS inválido."""), script)
        assertFalse(script.contains("erro.message"), script)
    }

    @Test
    fun descreverListaItensComTotalETagDoElemento() {
        val bruto = listOf(
            "ok\ttotal\t2",
            "article\tTítulo um\t\t\t\t\t\timg=https://x.test/a.jpg|a=https://x.test/p1",
            "article\tTítulo dois\thttps://x.test/p2\t\t\t\t\t",
        ).joinToString("\n")
        val texto = consulta.descrever(bruto)
        assertTrue(texto.contains("2"), texto)
        assertTrue(texto.contains("- article Título um"), texto)
        assertTrue(texto.contains("- article Título dois"), texto)
        assertTrue(texto.contains("https://x.test/p2"), texto)
        assertTrue(texto.contains("img=https://x.test/a.jpg|a=https://x.test/p1"), texto)
    }

    @Test
    fun descreverRepassaMensagemDeErroDoScript() {
        val texto = consulta.descrever("erro\tSeletor CSS inválido.")
        assertTrue(texto.contains("Seletor CSS inválido."), texto)
    }

    @Test
    fun descreverSemElementosEBrutoVazioDaoMensagemClara() {
        assertTrue(consulta.descrever("ok\ttotal\t0").contains("Nenhum elemento"))
        assertTrue(consulta.descrever("").isNotBlank())
    }

    @Test
    fun descreverIgnoraUrlsQueNaoSaoHttpEMostraNoMaximoQuarentaItens() {
        val linhas = (1..50).map { "div\titem $it\tjavascript:alert(1)\tdata:x\t\t\t\t" }
        val texto = consulta.descrever((listOf("ok\ttotal\t50") + linhas).joinToString("\n"))
        assertFalse(texto.contains("javascript:"), texto)
        assertFalse(texto.contains("data:x"), texto)
        assertEquals(40, texto.lines().count { it.startsWith("- div") })
    }

    @Test
    fun descreverInformaOffsetProximoOffsetETemMaisQuandoHaMaisElementos() {
        val linhas = (1..10).map { "div\titem $it\t\t\t\t\t\t" }
        val texto = consulta.descrever((listOf("ok\ttotal\t25") + linhas).joinToString("\n"), offset = 5, limite = 10)
        assertTrue(texto.contains("total=25 offset=5 proximo_offset=15 tem_mais=sim"), texto)
        assertEquals(10, texto.lines().count { it.startsWith("- div") })
    }

    @Test
    fun descreverInformaTemMaisNaoNaUltimaPagina() {
        val linhas = (1..5).map { "div\titem $it\t\t\t\t\t\t" }
        val texto = consulta.descrever((listOf("ok\ttotal\t25") + linhas).joinToString("\n"), offset = 20, limite = 10)
        assertTrue(texto.contains("total=25 offset=20 proximo_offset=25 tem_mais=nao"), texto)
    }

    @Test
    fun descreverComOffsetAlemDoTotalNaoPropoeMaisPaginas() {
        val texto = consulta.descrever("ok\ttotal\t3", offset = 10, limite = 5)
        assertTrue(texto.contains("total=3 offset=10 proximo_offset=3 tem_mais=nao"), texto)
    }

    @Test
    fun descreverSemOffsetExplicitoUsaZero() {
        val texto = consulta.descrever("ok\ttotal\t1\ndiv\titem\t\t\t\t\t\t")
        assertTrue(texto.contains("total=1 offset=0 proximo_offset=1 tem_mais=nao"), texto)
    }

    @Test
    fun scriptSoTrataHrefESrcComoUrlQuandoForemTextoEUsaGetAttributeNoRestante() {
        val script = consulta.script("a", 0, 40)
        assertTrue(script.contains("typeof direto === \"string\""), script)
        assertTrue(script.contains("getAttribute(nome)"), script)
        assertFalse(script.contains("filho.href"), script)
        assertFalse(script.contains("el.href"), script)
        assertFalse(script.contains("filho.src"), script)
        assertFalse(script.contains("el.src"), script)
    }
}
