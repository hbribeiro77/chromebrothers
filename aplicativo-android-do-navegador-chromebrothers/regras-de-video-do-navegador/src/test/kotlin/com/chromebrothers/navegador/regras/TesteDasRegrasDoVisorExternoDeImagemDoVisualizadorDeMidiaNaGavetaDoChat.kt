package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat.Troca
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasRegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat {
    private val regras = RegrasDoVisorExternoDeImagemDoVisualizadorDeMidiaNaGavetaDoChat

    @Test
    fun soAbreForaDaGavetaQuandoOVisualizadorEstaAncorado() {
        assertTrue(regras.deveAbrirForaDaGaveta(ancoradoNaGaveta = true))
        assertFalse(regras.deveAbrirForaDaGaveta(ancoradoNaGaveta = false))
    }

    @Test
    fun pinçaFicaEntreOTamanhoNormalEOMaximo() {
        assertEquals(2f, regras.zoomAposPinca(atual = 1f, fator = 2f))
        assertEquals(1f, regras.zoomAposPinca(atual = 1f, fator = 0.2f))
        assertEquals(4f, regras.zoomAposPinca(atual = 3f, fator = 2f))
    }

    @Test
    fun soltarNoMeioVoltaEArrastarOBastanteTrocaDeImagem() {
        assertEquals(Troca.FICAR, regras.trocaAoSoltar(deslocamentoX = -40f, largura = 1000f, velocidadeX = 0f, zoom = 1f))
        assertEquals(Troca.PROXIMA, regras.trocaAoSoltar(deslocamentoX = -250f, largura = 1000f, velocidadeX = 0f, zoom = 1f))
        assertEquals(Troca.ANTERIOR, regras.trocaAoSoltar(deslocamentoX = 250f, largura = 1000f, velocidadeX = 0f, zoom = 1f))
    }

    @Test
    fun flingCurtoTambemTrocaEZoomImpedeATroca() {
        assertEquals(Troca.PROXIMA, regras.trocaAoSoltar(deslocamentoX = -50f, largura = 1000f, velocidadeX = -1200f, zoom = 1f))
        assertEquals(Troca.FICAR, regras.trocaAoSoltar(deslocamentoX = -400f, largura = 1000f, velocidadeX = -2000f, zoom = 2f))
    }

    @Test
    fun trocaParaNaPrimeiraENaUltima() {
        assertEquals(0, regras.indiceAposTroca(indice = 0, total = 3, troca = Troca.ANTERIOR))
        assertEquals(2, regras.indiceAposTroca(indice = 2, total = 3, troca = Troca.PROXIMA))
        assertEquals(2, regras.indiceAposTroca(indice = 1, total = 3, troca = Troca.PROXIMA))
    }

    @Test
    fun naBordaODedoAndaMenosENoMeioAcompanha() {
        assertEquals(100f, regras.deslocamentoComBorda(100f, indice = 1, total = 3, largura = 1000f))
        assertTrue(regras.deslocamentoComBorda(100f, indice = 0, total = 3, largura = 1000f) < 100f)
        assertTrue(regras.deslocamentoComBorda(-100f, indice = 2, total = 3, largura = 1000f) > -100f)
    }

    @Test
    fun panNaoPassaDaFolgaDoZoom() {
        assertEquals(0f, regras.panLimitado(pan = 80f, zoom = 1f, tamanho = 1000f))
        assertEquals(500f, regras.panLimitado(pan = 900f, zoom = 2f, tamanho = 1000f))
        assertEquals(-500f, regras.panLimitado(pan = -900f, zoom = 2f, tamanho = 1000f))
    }

    @Test
    fun toqueLongoSoDisparaParadoENoTamanhoNormal() {
        assertTrue(regras.deveDispararToqueLongo(dx = 2f, dy = 2f, folga = 16f, zoom = 1f))
        assertFalse(regras.deveDispararToqueLongo(dx = 30f, dy = 0f, folga = 16f, zoom = 1f))
        assertFalse(regras.deveDispararToqueLongo(dx = 0f, dy = 0f, folga = 16f, zoom = 2f))
    }

    @Test
    fun gradeRolaSoQuandoACartaNaoEstaInteiraNaTela() {
        assertFalse(regras.deveRolarGradeAteACarta(posicao = 4, primeiraCompletamenteVisivel = 3, ultimaCompletamenteVisivel = 8))
        assertTrue(regras.deveRolarGradeAteACarta(posicao = 12, primeiraCompletamenteVisivel = 3, ultimaCompletamenteVisivel = 8))
        assertTrue(regras.deveRolarGradeAteACarta(posicao = 2, primeiraCompletamenteVisivel = -1, ultimaCompletamenteVisivel = -1))
        assertFalse(regras.deveRolarGradeAteACarta(posicao = -1, primeiraCompletamenteVisivel = 0, ultimaCompletamenteVisivel = 5))
    }

    @Test
    fun amostraDaDecodificacaoCabeNoLadoAlvo() {
        assertEquals(1, regras.amostraParaCaber(largura = 800, altura = 600, ladoAlvo = 1080))
        assertEquals(2, regras.amostraParaCaber(largura = 8000, altura = 6000, ladoAlvo = 1600))
        assertEquals(2048, regras.ladoAlvoDaDecodificacao(maiorLadoDaTela = 2960))
    }
}
