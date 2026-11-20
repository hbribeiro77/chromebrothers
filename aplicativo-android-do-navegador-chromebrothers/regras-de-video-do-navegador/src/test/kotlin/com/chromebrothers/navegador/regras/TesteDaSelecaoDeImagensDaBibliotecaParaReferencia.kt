package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaSelecaoDeImagensDaBibliotecaParaReferencia {
    private val regra = SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao

    @Test
    fun ordemDoToqueViraNumeroDoSelo() {
        val escolhidas = regra.alternar(regra.alternar(emptyList(), "b", 4), "a", 4)
        assertEquals(listOf("b", "a"), escolhidas)
        assertEquals(1, regra.numero(escolhidas, "b"))
        assertEquals(2, regra.numero(escolhidas, "a"))
        assertNull(regra.numero(escolhidas, "c"))
    }

    @Test
    fun desmarcarRenumeraAsSeguintes() {
        val escolhidas = regra.alternar(listOf("a", "b", "c"), "a", 4)
        assertEquals(listOf("b", "c"), escolhidas)
        assertEquals(1, regra.numero(escolhidas, "b"))
    }

    @Test
    fun limiteCheioIgnoraNovaEBloqueiaAsOutras() {
        val cheio = listOf("a", "b")
        assertEquals(cheio, regra.alternar(cheio, "c", 2))
        assertTrue(regra.bloqueada(cheio, "c", 2))
        assertTrue(!regra.bloqueada(cheio, "a", 2))
        assertEquals(listOf("b"), regra.alternar(cheio, "a", 2))
    }

    @Test
    fun filtroSeparaImagensEVideos() {
        val itens = listOf("img1", "vid1", "img2")
        val ehVideo: (String) -> Boolean = { it.startsWith("vid") }
        assertEquals(itens, regra.filtrar(itens, SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao.Filtro.TUDO, ehVideo))
        assertEquals(listOf("img1", "img2"), regra.filtrar(itens, SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao.Filtro.IMAGENS, ehVideo))
        assertEquals(listOf("vid1"), regra.filtrar(itens, SelecaoDeImagensDaBibliotecaParaReferenciaDaGeracao.Filtro.VIDEOS, ehVideo))
        assertTrue(regra.mostraFiltro(itens, ehVideo))
        assertTrue(!regra.mostraFiltro(listOf("img1", "img2"), ehVideo))
    }

    @Test
    fun textosDoModal() {
        assertEquals("Toque para escolher até 4 imagens", regra.subtitulo(0, 4))
        assertEquals("1 de 4 escolhida", regra.subtitulo(1, 4))
        assertEquals("4 de 4 escolhidas · limite atingido", regra.subtitulo(4, 4))
        assertEquals("Adicionar", regra.rotuloDoBotao(0))
        assertEquals("Adicionar (3)", regra.rotuloDoBotao(3))
    }
}
