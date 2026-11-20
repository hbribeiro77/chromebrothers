package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDaMolduraEDoRedimensionamentoDaComposicaoDeImagem {
    @Test
    fun asProporcoesProntasUsamOLadoLongoDe1920() {
        assertEquals(1920 to 1920, MolduraDaComposicaoDeImagem.tamanho(ProporcaoDaMolduraDaComposicao.QUADRADO))
        assertEquals(1920 to 1440, MolduraDaComposicaoDeImagem.tamanho(ProporcaoDaMolduraDaComposicao.QUATRO_POR_TRES))
        assertEquals(1920 to 1080, MolduraDaComposicaoDeImagem.tamanho(ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE))
        assertEquals(1080 to 1920, MolduraDaComposicaoDeImagem.tamanho(ProporcaoDaMolduraDaComposicao.NOVE_POR_DEZESSEIS))
    }

    @Test
    fun umTamanhoForaDasProporcoesProntasFicaLivre() {
        assertEquals(
            ProporcaoDaMolduraDaComposicao.DEZESSEIS_POR_NOVE,
            MolduraDaComposicaoDeImagem.reconhecer(1920, 1080),
        )
        assertNull(MolduraDaComposicaoDeImagem.reconhecer(1000, 800))
        assertEquals(64, MolduraDaComposicaoDeImagem.limitar(10))
        assertEquals(2048, MolduraDaComposicaoDeImagem.limitar(9000))
    }

    @Test
    fun redimensionarPeloCantoOpostoMantemAProporcao() {
        val original = RetanguloDaComposicao(0f, 0f, 100f, 50f)
        val ampliado = redimensionarMantendoProporcao(original, CantoDaComposicao.SUDESTE, 200f, 40f)
        assertEquals(0f, ampliado.esquerda, 0.01f)
        assertEquals(0f, ampliado.topo, 0.01f)
        assertEquals(200f, ampliado.largura, 0.01f)
        assertEquals(100f, ampliado.altura, 0.01f)

        val peloNoroeste = redimensionarMantendoProporcao(
            RetanguloDaComposicao(10f, 20f, 100f, 50f),
            CantoDaComposicao.NOROESTE,
            0f,
            0f,
        )
        assertEquals(140f, peloNoroeste.largura, 0.01f)
        assertEquals(70f, peloNoroeste.altura, 0.01f)
        assertEquals(-30f, peloNoroeste.esquerda, 0.01f)
        assertEquals(0f, peloNoroeste.topo, 0.01f)
    }

    @Test
    fun encaixarCentralizaSemEstourarAMoldura() {
        val encaixe = encaixarNaMoldura(1000f, 500f, 1920f, 1080f, ocupacao = 1f)
        assertEquals(1920f, encaixe.largura, 0.01f)
        assertEquals(960f, encaixe.altura, 0.01f)
        assertEquals(0f, encaixe.esquerda, 0.01f)
        assertEquals(60f, encaixe.topo, 0.01f)
    }

    @Test
    fun aResolucaoDoQuadroUsaOsPixelsDaImagemSalva() {
        assertEquals("1920 × 1080", MolduraDaComposicaoDeImagem.resolucao(1920, 1080))
    }

    @Test
    fun aProporcaoOriginalUsaOLadoLongoDe1920MantendoARazao() {
        assertEquals(1920 to 1080, MolduraDaComposicaoDeImagem.tamanhoDaProporcaoOriginal(1600, 900))
        assertEquals(1080 to 1920, MolduraDaComposicaoDeImagem.tamanhoDaProporcaoOriginal(900, 1600))
        assertEquals(1920 to 1920, MolduraDaComposicaoDeImagem.tamanhoDaProporcaoOriginal(500, 500))
    }

    @Test
    fun reconheceQuandoAMolduraSegueAProporcaoOriginal() {
        val moldura = MolduraDaComposicaoDeImagem.tamanhoDaProporcaoOriginal(800, 600)
        assertEquals(true, MolduraDaComposicaoDeImagem.ehProporcaoOriginal(moldura.first, moldura.second, 800, 600))
        assertEquals(false, MolduraDaComposicaoDeImagem.ehProporcaoOriginal(1920, 1080, 800, 600))
    }
}
