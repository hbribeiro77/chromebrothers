package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao {

    @Test
    fun somaPaddingMargensELargurasDosBotoesParaReservarEspacoFixo() {
        assertEquals(
            8 + 8 + 40 + 4 + 34 + 10,
            ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.larguraReservadaAosBotoesPx(
                paddingStartPx = 8,
                paddingEndPx = 8,
                larguraDoBotaoNovaAbaPx = 40,
                margemStartDoBotaoNovaAbaPx = 4,
                larguraDoContadorPx = 34,
                margemStartDoContadorPx = 10,
            ),
        )
    }

    @Test
    fun usaFallbackQuandoBotoesAindaNaoForamMedidos() {
        assertEquals(
            8 + 8 + 40 + 4 + 34 + 10,
            ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.larguraReservadaAosBotoesPx(
                paddingStartPx = 8,
                paddingEndPx = 8,
                larguraDoBotaoNovaAbaPx = 0,
                margemStartDoBotaoNovaAbaPx = 4,
                larguraDoContadorPx = 0,
                margemStartDoContadorPx = 10,
                fallbackLarguraDoBotaoNovaAbaPx = 40,
                fallbackLarguraDoContadorPx = 34,
            ),
        )
    }

    @Test
    fun quandoAbasCabemARolagemEncolheEOvaoEmpurraOContadorParaADireita() {
        val resultado = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
            larguraDaFaixaPx = 1000,
            larguraDoConteudoDasAbasPx = 400,
            larguraReservadaAosBotoesPx = 100,
        )
        assertEquals(400, resultado.larguraDaRolagemPx)
        assertEquals(0f, resultado.pesoDaRolagem)
        assertEquals(1f, resultado.pesoDoVaoAntesDoContador)
        assertTrue(resultado.larguraDaRolagemPx + 100 <= 1000)
    }

    @Test
    fun quandoAbasNaoCabemARolagemUsaSoOEspacoRestanteSemInvadirOsBotoes() {
        val resultado = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
            larguraDaFaixaPx = 1000,
            larguraDoConteudoDasAbasPx = 2400,
            larguraReservadaAosBotoesPx = 120,
        )
        assertEquals(0, resultado.larguraDaRolagemPx)
        assertEquals(1f, resultado.pesoDaRolagem)
        assertEquals(0f, resultado.pesoDoVaoAntesDoContador)
    }

    @Test
    fun nuncaDeixaARolagemMaiorQueOLimiteMesmoSeOConteudoParecerCaberPorArredondamento() {
        val resultado = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
            larguraDaFaixaPx = 500,
            larguraDoConteudoDasAbasPx = 450,
            larguraReservadaAosBotoesPx = 100,
        )
        assertEquals(0, resultado.larguraDaRolagemPx)
        assertEquals(1f, resultado.pesoDaRolagem)
        assertEquals(0f, resultado.pesoDoVaoAntesDoContador)
    }

    @Test
    fun faixaInvalidaOuSemConteudoMantemRolagemFlexivelSemVao() {
        val semFaixa = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
            larguraDaFaixaPx = 0,
            larguraDoConteudoDasAbasPx = 200,
            larguraReservadaAosBotoesPx = 80,
        )
        assertEquals(0, semFaixa.larguraDaRolagemPx)
        assertEquals(1f, semFaixa.pesoDaRolagem)
        assertEquals(0f, semFaixa.pesoDoVaoAntesDoContador)

        val semConteudo = ReservaDoEspacoDosBotoesDaFaixaDeAbasParaEvitarSobreposicao.calcular(
            larguraDaFaixaPx = 800,
            larguraDoConteudoDasAbasPx = 0,
            larguraReservadaAosBotoesPx = 80,
        )
        assertEquals(0, semConteudo.larguraDaRolagemPx)
        assertEquals(1f, semConteudo.pesoDaRolagem)
        assertEquals(0f, semConteudo.pesoDoVaoAntesDoContador)
    }
}
