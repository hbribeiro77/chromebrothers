package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TesteDoAlinhamentoDoRetanguloAGradeDePixelsDaImagem {
    @Test
    fun alinhaAoGridDosPixelsExtraidos() {
        // Imagem em (10, 20), 100x100 na moldura, bitmap 200x200 → escala 2.
        // Recorte float (10.4, 20.6, 50.3, 40.1) → pixels floor.
        val alinhada = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            recorteEsquerda = 10.4f,
            recorteTopo = 20.6f,
            recorteLargura = 50.3f,
            recorteAltura = 40.1f,
            imagemEsquerda = 10f,
            imagemTopo = 20f,
            imagemLargura = 100f,
            imagemAltura = 100f,
            bitmapLargura = 200,
            bitmapAltura = 200,
        )
        assertNotNull(alinhada)
        val pixels = DecisaoDoRetanguloSobreAImagemDaComposicao.pixels(
            10.4f, 20.6f, 50.3f, 40.1f,
            10f, 20f, 100f, 100f, 200, 200,
        )!!
        assertEquals(10f + pixels.esquerda * 100f / 200f, alinhada.esquerda, 0.001f)
        assertEquals(20f + pixels.topo * 100f / 200f, alinhada.topo, 0.001f)
        assertEquals(pixels.largura * 100f / 200f, alinhada.largura, 0.001f)
        assertEquals(pixels.altura * 100f / 200f, alinhada.altura, 0.001f)
    }

    @Test
    fun realinharNaoMexeQuandoJaEstaNaGrade() {
        val alinhada = AlinhamentoDoRetanguloAGradeDePixelsDaImagem.daSelecao(
            recorteEsquerda = 10f,
            recorteTopo = 20f,
            recorteLargura = 50f,
            recorteAltura = 40f,
            imagemEsquerda = 10f,
            imagemTopo = 20f,
            imagemLargura = 100f,
            imagemAltura = 100f,
            bitmapLargura = 200,
            bitmapAltura = 200,
        )
        assertNotNull(alinhada)
        assertEquals(10f, alinhada.esquerda, 0.001f)
        assertEquals(20f, alinhada.topo, 0.001f)
        assertEquals(50f, alinhada.largura, 0.001f)
        assertEquals(40f, alinhada.altura, 0.001f)
    }
}
