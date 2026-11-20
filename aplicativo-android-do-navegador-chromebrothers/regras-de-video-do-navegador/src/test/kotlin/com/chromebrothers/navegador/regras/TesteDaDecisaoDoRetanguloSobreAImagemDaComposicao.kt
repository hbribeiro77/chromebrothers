package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDecisaoDoRetanguloSobreAImagemDaComposicao {
    @Test
    fun retanguloPequenoDemaisNaoEntra() {
        assertFalse(DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(31f, 80f))
        assertTrue(DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(32f, 32f))
    }

    @Test
    fun recortarQuaseAFotoInteiraNaoFazNadaEUmPedacoMenorRecorta() {
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.IGNORAR,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = false, cabe = false, cobreQuaseTudo = false),
        )
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.IGNORAR,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = false, cabe = true, cobreQuaseTudo = true),
        )
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.RECORTAR,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = false, cabe = true, cobreQuaseTudo = false),
        )
    }

    @Test
    fun destacarAbreBuracoEQuaseAFotoInteiraViraCopiaPorCima() {
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.DESTACAR_COM_BURACO,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = true, cabe = true, cobreQuaseTudo = false),
        )
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.COPIAR_POR_CIMA,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = true, cabe = true, cobreQuaseTudo = true),
        )
        assertEquals(
            EfeitoDoRetanguloSobreAImagem.IGNORAR,
            DecisaoDoRetanguloSobreAImagemDaComposicao.efeito(destacar = true, cabe = false, cobreQuaseTudo = false),
        )
    }

    @Test
    fun soCobreQuaseTudoQuandoORetanguloEncostaNaFotoEOcupaEla() {
        assertTrue(
            DecisaoDoRetanguloSobreAImagemDaComposicao.cobreQuaseTudo(
                recorteEsquerda = 2f,
                recorteTopo = 3f,
                recorteLargura = 99f,
                recorteAltura = 198f,
                imagemEsquerda = 0f,
                imagemTopo = 0f,
                imagemLargura = 100f,
                imagemAltura = 200f,
            ),
        )
        assertFalse(
            DecisaoDoRetanguloSobreAImagemDaComposicao.cobreQuaseTudo(
                recorteEsquerda = 20f,
                recorteTopo = 20f,
                recorteLargura = 40f,
                recorteAltura = 40f,
                imagemEsquerda = 0f,
                imagemTopo = 0f,
                imagemLargura = 100f,
                imagemAltura = 200f,
            ),
        )
    }

    @Test
    fun oRetanguloDaTelaViraPixelsDoBitmap() {
        assertEquals(
            RetanguloEmPixelsDaImagem(esquerda = 0, topo = 0, largura = 100, altura = 200),
            DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
                recorteEsquerda = 10f,
                recorteTopo = 20f,
                recorteLargura = 50f,
                recorteAltura = 100f,
                imagemEsquerda = 10f,
                imagemTopo = 20f,
                imagemLargura = 100f,
                imagemAltura = 200f,
                bitmapLargura = 200,
                bitmapAltura = 400,
            ),
        )
    }

    @Test
    fun aIntersecaoCortaASelecaoQueUltrapassaAImagem() {
        assertEquals(
            AreaDoRecorteSobreAImagemDaComposicao(esquerda = 50f, topo = 40f, largura = 50f, altura = 60f),
            DecisaoDoRetanguloSobreAImagemDaComposicao.intersecao(
                recorteEsquerda = 0f,
                recorteTopo = 0f,
                recorteLargura = 100f,
                recorteAltura = 100f,
                imagemEsquerda = 50f,
                imagemTopo = 40f,
                imagemLargura = 80f,
                imagemAltura = 60f,
            ),
        )
        assertEquals(
            null,
            DecisaoDoRetanguloSobreAImagemDaComposicao.intersecao(
                recorteEsquerda = 0f,
                recorteTopo = 0f,
                recorteLargura = 10f,
                recorteAltura = 10f,
                imagemEsquerda = 50f,
                imagemTopo = 40f,
                imagemLargura = 80f,
                imagemAltura = 60f,
            ),
        )
    }

    @Test
    fun pixelsDeUmaSelecaoMaiorQueAImagemSeguemSoAIntersecao() {
        assertEquals(
            RetanguloEmPixelsDaImagem(esquerda = 0, topo = 0, largura = 200, altura = 100),
            DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
                recorteEsquerda = 0f,
                recorteTopo = 0f,
                recorteLargura = 400f,
                recorteAltura = 300f,
                imagemEsquerda = 100f,
                imagemTopo = 50f,
                imagemLargura = 200f,
                imagemAltura = 100f,
                bitmapLargura = 200,
                bitmapAltura = 100,
            ),
        )
    }
}
