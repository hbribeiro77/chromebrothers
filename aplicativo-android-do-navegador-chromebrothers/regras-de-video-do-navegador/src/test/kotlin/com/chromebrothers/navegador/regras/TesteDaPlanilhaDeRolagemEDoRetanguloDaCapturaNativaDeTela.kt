package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaPlanilhaDeRolagemEDoRetanguloDaCapturaNativaDeTela {
    @Test
    fun paginaQueCabeNumaTelaGeraUmaUnicaRolagemEmZero() {
        assertEquals(
            listOf(0),
            PlanilhaDeRolagemParaCapturaDaPaginaInteira.offsets(
                alturaDaPagina = 800,
                alturaDaJanela = 900,
                sobreposicao = 40,
                maximoDeFatias = 40,
            ),
        )
    }

    @Test
    fun paginaLongaGeraOffsetsComSobreposicaoAteOFundo() {
        val offsets = PlanilhaDeRolagemParaCapturaDaPaginaInteira.offsets(
            alturaDaPagina = 2500,
            alturaDaJanela = 1000,
            sobreposicao = 100,
            maximoDeFatias = 40,
        )
        assertEquals(listOf(0, 900, 1500), offsets)
        assertEquals(1500, offsets.last())
    }

    @Test
    fun respeitaOLimiteDeFatias() {
        val offsets = PlanilhaDeRolagemParaCapturaDaPaginaInteira.offsets(
            alturaDaPagina = 50_000,
            alturaDaJanela = 1000,
            sobreposicao = 0,
            maximoDeFatias = 5,
        )
        assertEquals(5, offsets.size)
        assertEquals(0, offsets.first())
        assertEquals(4000, offsets.last())
    }

    @Test
    fun retanguloDaSelecaoNormalizaELimitaAosLimitesDaTela() {
        val area = RetanguloNormalizadoDaSelecaoDeCaptura.de(
            x1 = 120f,
            y1 = 80f,
            x2 = 20f,
            y2 = 10f,
            larguraMaxima = 100,
            alturaMaxima = 50,
        )
        assertEquals(20, area.esquerda)
        assertEquals(10, area.topo)
        assertEquals(80, area.largura)
        assertEquals(40, area.altura)
    }

    @Test
    fun retanguloMinimoViraPeloMenosUmPixelQuandoHaArraste() {
        val area = RetanguloNormalizadoDaSelecaoDeCaptura.de(
            x1 = 5f,
            y1 = 5f,
            x2 = 5.4f,
            y2 = 5.2f,
            larguraMaxima = 200,
            alturaMaxima = 200,
        )
        assertTrue(area.largura >= 1)
        assertTrue(area.altura >= 1)
    }

    @Test
    fun alturaUtilDaFatiaDescartaASobreposicaoExcetoNaPrimeira() {
        assertEquals(
            1000,
            PlanilhaDeRolagemParaCapturaDaPaginaInteira.alturaUtilDaFatia(
                indice = 0,
                alturaDaJanela = 1000,
                sobreposicao = 100,
                restoAteOFundo = 5000,
            ),
        )
        assertEquals(
            900,
            PlanilhaDeRolagemParaCapturaDaPaginaInteira.alturaUtilDaFatia(
                indice = 1,
                alturaDaJanela = 1000,
                sobreposicao = 100,
                restoAteOFundo = 5000,
            ),
        )
        assertEquals(
            250,
            PlanilhaDeRolagemParaCapturaDaPaginaInteira.alturaUtilDaFatia(
                indice = 2,
                alturaDaJanela = 1000,
                sobreposicao = 100,
                restoAteOFundo = 250,
            ),
        )
    }
}
