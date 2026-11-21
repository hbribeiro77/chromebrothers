package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDoRecorteDeTrechoDeVideo {
    private val regra = RecorteDeTrechoDeVideo
    private val chaves = listOf(0L, 2000L, 4000L, 6000L)

    @Test
    fun soArquivoDoAparelhoPodeSerRecortado() {
        assertTrue(regra.podeRecortar("content://media/external/video/media/12"))
        assertTrue(regra.podeRecortar("file:///data/user/0/app/files/video.mp4"))
        assertTrue(regra.podeRecortar("file:/data/user/0/app/files/video.mp4"), "forma da biblioteca (File.toURI)")
        assertFalse(regra.podeRecortar("https://site.com/video.m3u8"))
        assertFalse(regra.podeRecortar(null))
    }

    @Test
    fun inicioSemEncodeRecuaAoQuadroChaveAnterior() {
        assertEquals(2000L, regra.inicioSemEncode(chaves, 3500L))
        assertEquals(4000L, regra.inicioSemEncode(chaves, 4000L))
        assertEquals(4000L, regra.inicioSemEncode(chaves, 3980L), "dentro da tolerância conta como o quadro-chave")
        assertEquals(0L, regra.inicioSemEncode(emptyList(), 3500L))
    }

    @Test
    fun reconheceInicioQueJaEhQuadroChave() {
        assertTrue(regra.inicioJaEhQuadroChave(chaves, 2010L))
        assertFalse(regra.inicioJaEhQuadroChave(chaves, 2500L))
        assertTrue(regra.inicioJaEhQuadroChave(emptyList(), 0L))
    }

    @Test
    fun trechoFicaEmOrdemDentroDoVideoEComDuracaoMinima() {
        assertEquals(1000L to 5000L, regra.trechoValido(5000L, 1000L, 8000L))
        assertEquals(7000L to 8000L, regra.trechoValido(7000L, 9000L, 8000L))
        assertNull(regra.trechoValido(1000L, 1200L, 8000L))
    }

    @Test
    fun tamanhoEstimadoProporcionalAoTrecho() {
        assertEquals(25_000_000L, regra.tamanhoEstimadoBytes(100_000_000L, 60_000L, 0L, 15_000L))
        assertEquals(-1L, regra.tamanhoEstimadoBytes(0L, 60_000L, 0L, 15_000L))
    }

    @Test
    fun nomeSugeridoVemDoOriginalComOTrecho() {
        assertEquals("trailer_recorte_0m12s-0m45s.mp4", regra.nomeSugerido("trailer.mp4", 12_400L, 45_000L))
        assertEquals("video_recorte_1h0m5s-1h1m0s.mp4", regra.nomeSugerido("", 3_605_000L, 3_660_000L))
        assertEquals("a_b_recorte_0m0s-0m3s.mp4", regra.nomeSugerido("content://x/a:b.mov", 0L, 3_000L))
    }

    @Test
    fun nomeDigitadoViraArquivoMp4Valido() {
        assertEquals("meu corte.mp4", regra.nomeFinal("meu corte", "x.mp4"))
        assertEquals("meu corte.mp4", regra.nomeFinal("meu corte.mp4", "x.mp4"))
        assertEquals("a_b.mp4", regra.nomeFinal("a/b", "x.mp4"))
        assertEquals("x.mp4", regra.nomeFinal("   ", "x.mp4"))
    }

    @Test
    fun tempoComDecimosMostraOAjuste() {
        assertEquals("0:12,4", regra.tempoComDecimos(12_400L))
        assertEquals("1:00:03,0", regra.tempoComDecimos(3_603_000L))
    }
}
