package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDaInterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao {
    private val regra = InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao

    @Test
    fun semCabecalhoOuTamanhoInvalidoRetornaNulo() {
        assertNull(regra.interpretar(null, 100))
        assertNull(regra.interpretar("", 100))
        assertNull(regra.interpretar("bytes=0-9", 0))
        assertNull(regra.interpretar("bytes=0-9", -1))
    }

    @Test
    fun faixaSimples() {
        val f = regra.interpretar("bytes=0-99", 1000)!!
        assertEquals(0L, f.inicio)
        assertEquals(99L, f.fim)
        assertEquals(100L, f.tamanhoDaFaixa)
        assertEquals("bytes 0-99/1000", f.cabecalhoContentRange)
    }

    @Test
    fun faixaAbertaAteOFim() {
        val f = regra.interpretar("bytes=500-", 1000)!!
        assertEquals(500L, f.inicio)
        assertEquals(999L, f.fim)
        assertEquals(500L, f.tamanhoDaFaixa)
    }

    @Test
    fun sufixoDosUltimosBytes() {
        val f = regra.interpretar("bytes=-200", 1000)!!
        assertEquals(800L, f.inicio)
        assertEquals(999L, f.fim)
        assertEquals(200L, f.tamanhoDaFaixa)
    }

    @Test
    fun foraDosLimitesOuFormatoInvalido() {
        assertNull(regra.interpretar("bytes=100-50", 1000))
        assertNull(regra.interpretar("bytes=1000-1005", 1000))
        assertNull(regra.interpretar("cats=0-1", 1000))
    }

    @Test
    fun extraiLadoDaMiniaturaDoUri() {
        assertEquals(512, regra.ladoDaMiniatura("content://x/y?cb_thumb=512"))
        assertEquals(256, regra.ladoDaMiniatura("file:///a.jpg?foo=1&cb_thumb=256"))
        assertNull(regra.ladoDaMiniatura("content://x/y"))
        assertNull(regra.ladoDaMiniatura("content://x/y?cb_thumb=abc"))
    }

    @Test
    fun removeCbThumbParaLerAImagemCheiaNoArraste() {
        assertEquals(
            "content://x/y",
            regra.uriSemParametroDeMiniatura("content://x/y?cb_thumb=512"),
        )
        assertEquals(
            "file:///a.jpg?foo=1",
            regra.uriSemParametroDeMiniatura("file:///a.jpg?foo=1&cb_thumb=256"),
        )
        assertEquals(
            "content://x/y?outro=1",
            regra.uriSemParametroDeMiniatura("content://x/y?cb_thumb=512&outro=1"),
        )
        assertEquals(
            "content://x/y",
            regra.uriSemParametroDeMiniatura("content://x/y"),
        )
        assertEquals("", regra.uriSemParametroDeMiniatura(""))
        assertEquals("", regra.uriSemParametroDeMiniatura(null))
    }
}
