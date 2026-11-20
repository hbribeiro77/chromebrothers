package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaLeituraPaginadaDoTextoDaPaginaParaOChat {
    private val dez = "0123456789"

    @Test
    fun fatiaInicialComLimiteQuatroIndicaQueTemMais() {
        val fatia = LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(dez, offset = 0, limite = 4)
        assertEquals("0123", fatia.corpo)
        assertEquals(10, fatia.total)
        assertEquals(0, fatia.offset)
        assertEquals(4, fatia.proximoOffset)
        assertTrue(fatia.temMais)
    }

    @Test
    fun fatiaNoOffsetOitoDevolveOTrechoParcialSemMais() {
        val fatia = LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(dez, offset = 8, limite = 4)
        assertEquals("89", fatia.corpo)
        assertEquals(10, fatia.total)
        assertEquals(8, fatia.offset)
        assertEquals(10, fatia.proximoOffset)
        assertFalse(fatia.temMais)
    }

    @Test
    fun offsetAlemDoFimDevolveCorpoVazioSemMais() {
        val fatia = LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(dez, offset = 99, limite = 4)
        assertEquals("", fatia.corpo)
        assertEquals(10, fatia.total)
        assertEquals(99, fatia.offset)
        assertEquals(10, fatia.proximoOffset)
        assertFalse(fatia.temMais)
    }

    @Test
    fun pedidoComLimiteAcimaDoTetoFicaEmSeisMil() {
        val pedido = LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido("""{"offset":4,"limite":99999}""")
        assertEquals(4, pedido.offset)
        assertEquals(LeituraPaginadaDoTextoDaPaginaParaOChat.LIMITE_PADRAO, pedido.limite)
        assertEquals(6000, pedido.limite)
    }

    @Test
    fun pedidoSemChavesUsaOffsetZeroELimitePadrao() {
        val pedido = LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido("{}")
        assertEquals(0, pedido.offset)
        assertEquals(6000, pedido.limite)
    }

    @Test
    fun offsetNegativoELimiteInvalidoVoltamAoPiso() {
        val negativo = LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido("""{"offset":-5,"limite":10}""")
        assertEquals(0, negativo.offset)
        assertEquals(10, negativo.limite)
        assertEquals(6000, LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido("""{"limite":0}""").limite)
        assertEquals(6000, LeituraPaginadaDoTextoDaPaginaParaOChat.lerPedido("""{"limite":-3}""").limite)
    }

    @Test
    fun fatiarTambemRespeitaOTetoDeSeisMil() {
        val texto = "a".repeat(7000)
        val fatia = LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(texto, offset = 0, limite = 99999)
        assertEquals(6000, fatia.corpo.length)
        assertEquals(7000, fatia.total)
        assertEquals(0, fatia.offset)
        assertEquals(6000, fatia.proximoOffset)
        assertTrue(fatia.temMais)
    }

    @Test
    fun mensagemIncluiMetadadosEOtrecho() {
        val fatia = FatiaDoTextoDaPagina(
            corpo = "abcd",
            total = 10,
            offset = 0,
            proximoOffset = 4,
            temMais = true,
        )
        val mensagem = LeituraPaginadaDoTextoDaPaginaParaOChat.mensagem("Gatos", "https://exemplo.test", fatia)
        assertEquals(
            """
            Página aberta: Gatos
            Endereço: https://exemplo.test

            total=10 offset=0 proximo_offset=4 tem_mais=sim

            abcd
            """.trimIndent(),
            mensagem,
        )
    }

    @Test
    fun mensagemVaziaUsaNaoETituloEmBrancoComoOContexto() {
        val fatia = FatiaDoTextoDaPagina(
            corpo = "",
            total = 10,
            offset = 99,
            proximoOffset = 10,
            temMais = false,
        )
        val mensagem = LeituraPaginadaDoTextoDaPaginaParaOChat.mensagem("  ", "", fatia)
        assertEquals(
            """
            Página aberta: Sem título
            Endereço: sem endereço

            total=10 offset=99 proximo_offset=10 tem_mais=nao
            """.trimIndent(),
            mensagem,
        )
    }

    @Test
    fun normalizarColapsaEspacosEmBranco() {
        assertEquals(
            "um dois tres",
            LeituraPaginadaDoTextoDaPaginaParaOChat.normalizar("  um \n\t dois   tres  "),
        )
    }

    @Test
    fun normalizarEFatiarColapsamEspacoInquebravelEntrePalavras() {
        val bruto = "\u00A0gato\u00A0\u00A0preto\u00A0"
        val normalizado = LeituraPaginadaDoTextoDaPaginaParaOChat.normalizar(bruto)
        assertEquals("gato preto", normalizado)
        val fatia = LeituraPaginadaDoTextoDaPaginaParaOChat.fatiar(normalizado, offset = 0, limite = 20)
        assertEquals("gato preto", fatia.corpo)
        assertFalse(fatia.corpo.contains('\u00A0'))
    }

    @Test
    fun normalizarColapsaEspacosUnicodeSemUsarRegexDoJvm() {
        assertEquals(
            "a b c d",
            LeituraPaginadaDoTextoDaPaginaParaOChat.normalizar("a\u2003\u2009b\u202Fc\u3000\r\n d"),
        )
        assertEquals("", LeituraPaginadaDoTextoDaPaginaParaOChat.normalizar(" \u00A0\n\t "))
    }
}
