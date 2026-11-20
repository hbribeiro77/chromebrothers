package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoApagadorSobreAImagemDaComposicao {
    @Test
    fun oCentroDaImagemParadaViraOCentroDoBitmap() {
        val pixel = ApagadorSobreAImagemDaComposicao.pixel(
            u = 0.5f,
            v = 0.5f,
            molduraLargura = 100f,
            molduraAltura = 50f,
            esquerda = 0f,
            topo = 0f,
            largura = 100f,
            altura = 50f,
            angulo = 0f,
            bitmapLargura = 200,
            bitmapAltura = 100,
        )
        assertEquals(100f, pixel?.x)
        assertEquals(50f, pixel?.y)
    }

    @Test
    fun oPontoForaDaImagemNaoFuraOBitmap() {
        val pixel = ApagadorSobreAImagemDaComposicao.pixel(
            u = 0.1f,
            v = 0.1f,
            molduraLargura = 200f,
            molduraAltura = 200f,
            esquerda = 100f,
            topo = 100f,
            largura = 50f,
            altura = 50f,
            angulo = 0f,
            bitmapLargura = 10,
            bitmapAltura = 10,
        )
        assertNull(pixel)
    }

    @Test
    fun aEspessuraAcompanhaOTamanhoDoBitmap() {
        assertEquals(
            20f,
            ApagadorSobreAImagemDaComposicao.espessura(
                larguraNormalizada = 0.1f,
                molduraLargura = 100f,
                larguraDaPeca = 100f,
                bitmapLargura = 200,
            ),
        )
    }
}
