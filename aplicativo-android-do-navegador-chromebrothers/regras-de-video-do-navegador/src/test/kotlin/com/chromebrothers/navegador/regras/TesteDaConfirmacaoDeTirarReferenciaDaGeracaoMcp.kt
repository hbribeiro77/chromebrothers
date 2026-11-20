package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaConfirmacaoDeTirarReferenciaDaGeracaoMcp {
    private val textos = ConfirmacaoDeTirarReferenciaDaGeracaoMcp

    @Test
    fun mensagemMostraAPosicaoComoOUsuarioVe() {
        assertTrue(textos.mensagemDeUma(0).startsWith("A referência 1 "))
        assertTrue(textos.mensagemDeUma(3).startsWith("A referência 4 "))
    }

    @Test
    fun tituloDeTodasConcordaComAQuantidade() {
        assertEquals("Tirar a referência?", textos.tituloDeTodas(1))
        assertEquals("Tirar as 3 referências?", textos.tituloDeTodas(3))
        assertTrue(textos.mensagemDeTodas(3).startsWith("As 3 referências saem"))
    }

    @Test
    fun htmlPedeConfirmacaoAntesDeTirar() {
        for (h in listOf(
            HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html(),
            HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.html(),
        )) {
            val umaSo = h.substringAfter("function limparIndice(indice)").substringBefore("\n}")
            assertTrue(umaSo.contains("chromebrothers.pedirParaRemoverReferencia("))
            val todas = h.substringAfter("function limpar()").substringBefore("\n}")
            assertTrue(todas.contains("chromebrothers.pedirParaLimparReferencias()"))
        }
    }
}
