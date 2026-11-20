package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoMapeamentoDoLacoParaPixelsDoRecorteDaImagem {
    @Test
    fun pontoNoRecorte_usaEspacoDaImagemMenosOrigemDoCrop() {
        // Imagem em (10, 20) na moldura; bitmap 200x200 em caixa 100x100 → escala 2.
        // Crop começa no pixel (20, 40) (= composição 20, 40).
        val ponto = MapeamentoDoLacoParaPixelsDoRecorteDaImagem.pontoNoRecorte(
            pontoX = 25f,
            pontoY = 45f,
            imagemEsquerda = 10f,
            imagemTopo = 20f,
            escalaX = 2f,
            escalaY = 2f,
            pixelsEsquerda = 20,
            pixelsTopo = 40,
        )
        assertEquals(10f, ponto.x, 0.001f)
        assertEquals(10f, ponto.y, 0.001f)
    }

    @Test
    fun pontoNoRecorte_comCropNaOrigemCoincideComMapeamentoDireto() {
        val ponto = MapeamentoDoLacoParaPixelsDoRecorteDaImagem.pontoNoRecorte(
            pontoX = 30f,
            pontoY = 50f,
            imagemEsquerda = 10f,
            imagemTopo = 20f,
            escalaX = 2f,
            escalaY = 2f,
            pixelsEsquerda = 0,
            pixelsTopo = 0,
        )
        assertEquals(40f, ponto.x, 0.001f)
        assertEquals(60f, ponto.y, 0.001f)
    }

    @Test
    fun pontoNoRecorte_naoUsaLarguraDoAreaComoEscala() {
        // Regressão: a fórmula antiga (ponto - area) / area.largura * cropW
        // com area em (20,40) e largura 50 daria outro valor para o mesmo ponto.
        val ponto = MapeamentoDoLacoParaPixelsDoRecorteDaImagem.pontoNoRecorte(
            pontoX = 45f,
            pontoY = 55f,
            imagemEsquerda = 10f,
            imagemTopo = 20f,
            escalaX = 2f,
            escalaY = 2f,
            pixelsEsquerda = 20,
            pixelsTopo = 40,
        )
        assertEquals(50f, ponto.x, 0.001f)
        assertEquals(30f, ponto.y, 0.001f)
    }
}
