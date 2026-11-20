package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaTintaEmCacheDaCamadaDaComposicao {
    @Test
    fun oPrimeiroPontoDoTracoAindaNaoFoiPintado() {
        assertEquals(0, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(0, 1))
        assertEquals(0, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(0, 5))
    }

    @Test
    fun oTrechoNovoRecomecaNoUltimoPontoJaPintado() {
        assertEquals(3, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(4, 6))
        assertEquals(0, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(1, 2))
    }

    @Test
    fun naoHaTrechoNovoQuandoNadaChegou() {
        assertEquals(4, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(4, 4))
        assertEquals(4, TintaEmCacheDaCamadaDaComposicao.primeiroIndice(5, 4))
    }

    @Test
    fun aTintaCabeNoLadoMaximoSemEsticarUmaMolduraPequena() {
        assertEquals(1000 to 500, TintaEmCacheDaCamadaDaComposicao.dimensoes(1000, 500))
        assertEquals(3072 to 2304, TintaEmCacheDaCamadaDaComposicao.dimensoes(4000, 3000))
        // Print do tablet (2960 px) fica na resolução cheia.
        assertEquals(2960 to 1848, TintaEmCacheDaCamadaDaComposicao.dimensoes(2960, 1848))
        assertEquals(1 to 1, TintaEmCacheDaCamadaDaComposicao.dimensoes(0, 10))
    }
}
