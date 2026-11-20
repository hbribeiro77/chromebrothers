package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoMovimentoDaMidiaGeradaParaAGaleria {
    private val regra = MovimentoDaMidiaGeradaParaAGaleria

    @Test
    fun soMidiaGeradaDentroDoAppPodeSerMovida() {
        assertTrue(regra.podeMover("file:///data/user/0/x/files/biblioteca-de-videos-higgsfield/vivix-1.mp4"))
        assertTrue(regra.podeMover("file:///data/user/0/x/files/biblioteca-de-imagens-capturadas/a.png"))
        assertFalse(regra.podeMover("content://media/external/video/media/12"))
        assertFalse(regra.podeMover("file:///storage/emulated/0/Download/ChromeBrothers/v.mp4"))
    }

    @Test
    fun videoVaiParaMoviesEImagemParaPictures() {
        assertEquals("Movies/ChromeBrothers", regra.pastaDeDestino("vivix-1.mp4"))
        assertEquals("Pictures/ChromeBrothers", regra.pastaDeDestino("gerada.png"))
        assertEquals("video/mp4", regra.mime("vivix-1.MP4"))
        assertEquals("image/webp", regra.mime("a.webp"))
        assertEquals("image/jpeg", regra.mime("sem-extensao"))
    }

    @Test
    fun copiaSoConfereComOMesmoTamanho() {
        assertTrue(regra.copiaConfere(1000, 1000))
        assertFalse(regra.copiaConfere(1000, 999))
        assertFalse(regra.copiaConfere(0, 0))
    }

    @Test
    fun reconhecePastaDosVideosMovidos() {
        assertTrue(regra.ehDaPastaDosVideosMovidos("Movies/ChromeBrothers/"))
        assertTrue(regra.ehDaPastaDosVideosMovidos("Movies/ChromeBrothers/sub/"))
        assertFalse(regra.ehDaPastaDosVideosMovidos("Download/ChromeBrothers/"))
        assertFalse(regra.ehDaPastaDosVideosMovidos("Movies/Outro/"))
    }

    @Test
    fun resumoDoMovimento() {
        assertEquals("Movido para a Galeria.", regra.resumo(1, 0))
        assertEquals("3 arquivos movidos para a Galeria.", regra.resumo(3, 0))
        assertEquals("Não deu para mover para a Galeria.", regra.resumo(0, 2))
        assertEquals("2 movidos para a Galeria; 1 não deu certo.", regra.resumo(2, 1))
        assertEquals("1 movido para a Galeria; 2 não deram certo.", regra.resumo(1, 2))
    }
}
