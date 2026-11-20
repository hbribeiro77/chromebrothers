package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaAnimacaoDaGavetaDoChatDoNavegador {

    @Test
    fun larguraAlvoRespeitaMaximoDeQuarentaESeisPorCentoDoPai() {
        val densidade = 2f
        val pai = 1000
        val maximo = (pai * 0.46f).toInt()
        assertEquals(
            maximo,
            AnimacaoDaGavetaDoChatDoNavegador.calcularLarguraAlvo(densidade, pai),
        )
    }

    @Test
    fun larguraAlvoUsaPreferidaQuandoCabeNoPai() {
        assertEquals(
            380,
            AnimacaoDaGavetaDoChatDoNavegador.calcularLarguraAlvo(1f, 2000),
        )
    }

    @Test
    fun noInicioSoExisteBlocoDaPagina() {
        val geo = AnimacaoDaGavetaDoChatDoNavegador.geometriaDosBlocos(
            larguraDaFaixa = 1000,
            larguraDoChatAlvo = 400,
            fracao = 0f,
        )
        assertEquals(1000, geo.larguraDaPagina)
        assertEquals(0, geo.larguraDoChat)
    }

    @Test
    fun noFimPaginaEChatDividemAFaixa() {
        val geo = AnimacaoDaGavetaDoChatDoNavegador.geometriaDosBlocos(
            larguraDaFaixa = 1000,
            larguraDoChatAlvo = 400,
            fracao = 1f,
        )
        assertEquals(600, geo.larguraDaPagina)
        assertEquals(400, geo.larguraDoChat)
    }

    @Test
    fun noMeioAGeometriaAcompanhaAFracao() {
        val geo = AnimacaoDaGavetaDoChatDoNavegador.geometriaDosBlocos(
            larguraDaFaixa = 1000,
            larguraDoChatAlvo = 400,
            fracao = 0.5f,
        )
        assertEquals(800, geo.larguraDaPagina)
        assertEquals(200, geo.larguraDoChat)
    }

    @Test
    fun crossfadeEMaisCurtoQueAAnimacaoPrincipal() {
        assertTrue(
            AnimacaoDaGavetaDoChatDoNavegador.DURACAO_DO_CROSSFADE_MS <
                AnimacaoDaGavetaDoChatDoNavegador.DURACAO_MS,
        )
    }

    @Test
    fun esperaAntesDoCrossfadeGaranteAssentamento() {
        assertEquals(200L, AnimacaoDaGavetaDoChatDoNavegador.ESPERA_ANTES_DO_CROSSFADE_MS)
        assertTrue(AnimacaoDaGavetaDoChatDoNavegador.DURACAO_DA_ENTRADA_DOS_BLOCOS_MS > 0L)
    }

    @Test
    fun blocosDaAberturaSaoTranslucidos() {
        val alpha = AnimacaoDaGavetaDoChatDoNavegador.ALPHA_DOS_BLOCOS_ESQUELETO
        assertTrue(alpha > 0f && alpha < 1f)
    }

    @Test
    fun margemDeSobreposicaoParaFechamentoSimples() {
        assertEquals(-400, AnimacaoDaGavetaDoChatDoNavegador.margemDeSobreposicao(400))
        assertEquals(0f, AnimacaoDaGavetaDoChatDoNavegador.translationAberta())
        assertEquals(400f, AnimacaoDaGavetaDoChatDoNavegador.translationAoFechar(400))
    }

    @Test
    fun chatRealDeslizaJuntoComAFracaoDosBlocos() {
        assertEquals(
            400f,
            AnimacaoDaGavetaDoChatDoNavegador.translationDoChatDuranteAbertura(400, 0f),
        )
        assertEquals(
            200f,
            AnimacaoDaGavetaDoChatDoNavegador.translationDoChatDuranteAbertura(400, 0.5f),
        )
        assertEquals(
            0f,
            AnimacaoDaGavetaDoChatDoNavegador.translationDoChatDuranteAbertura(400, 1f),
        )
    }

    @Test
    fun barrinhaSplitAcompanhaFracaoEmFloatContinuo() {
        assertEquals(
            1000f,
            AnimacaoDaGavetaDoChatDoNavegador.posicaoXDaBarrinhaSplit(1000, 400, 0f),
        )
        assertEquals(
            800f,
            AnimacaoDaGavetaDoChatDoNavegador.posicaoXDaBarrinhaSplit(1000, 400, 0.5f),
        )
        assertEquals(
            600f,
            AnimacaoDaGavetaDoChatDoNavegador.posicaoXDaBarrinhaSplit(1000, 400, 1f),
        )
        val meioFino = AnimacaoDaGavetaDoChatDoNavegador.posicaoXDaBarrinhaSplit(1000, 400, 0.33f)
        assertEquals(1000f - 400f * 0.33f, meioFino)
        assertTrue(AnimacaoDaGavetaDoChatDoNavegador.DURACAO_DO_FADE_DA_BARRINHA_SPLIT_MS > 0L)
    }
}
