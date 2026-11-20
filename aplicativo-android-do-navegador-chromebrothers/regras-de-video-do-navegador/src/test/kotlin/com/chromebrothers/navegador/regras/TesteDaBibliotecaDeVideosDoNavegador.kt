package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaBibliotecaDeVideosDoNavegador {
    @Test
    fun videoDePaginaGuardaOEnderecoDaPaginaENaoOArquivoTemporario() {
        val identidade = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            enderecoDaPagina = "https://www.youtube.com/watch?v=abc",
            enderecoDoVideo = "https://rr3---sn.googlevideo.com/videoplayback?expire=1",
            titulo = "Um vídeo",
        )
        assertEquals("https://www.youtube.com/watch?v=abc", identidade!!.chave)
        assertEquals("https://www.youtube.com/watch?v=abc", identidade.enderecoParaAbrir)
        assertFalse(identidade.abreNoPlayerDeArquivo)
        assertEquals("Um vídeo", identidade.titulo)
    }

    @Test
    fun arquivoDiretoAbreNoPlayerNativoPeloEnderecoDoArquivo() {
        val identidade = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            enderecoDaPagina = "https://site.test/pagina",
            enderecoDoVideo = "https://cdn.test/filme.mp4?x=1",
            titulo = "",
        )
        assertTrue(identidade!!.abreNoPlayerDeArquivo)
        assertEquals("https://cdn.test/filme.mp4?x=1", identidade.chave)
        assertEquals("https://cdn.test/filme.mp4?x=1", identidade.enderecoParaAbrir)
        assertEquals("filme.mp4", identidade.titulo)
    }

    @Test
    fun barraNoFimDoEnderecoNaoCriaOutroVideo() {
        val comBarra = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            "https://site.test/v/",
            "blob:https://site.test/1",
            "Clipe",
        )
        val semBarra = MontagemDaIdentidadeDoVideoNaBiblioteca.montar(
            "https://site.test/v",
            "blob:https://site.test/2",
            "Clipe",
        )
        assertEquals(comBarra!!.chave, semBarra!!.chave)
    }

    @Test
    fun enderecoQueNaoEPaginaNemArquivoFicaDeFora() {
        assertNull(
            MontagemDaIdentidadeDoVideoNaBiblioteca.montar("", "blob:https://site.test/1", "Clipe"),
        )
    }

    @Test
    fun abrirDeNovoAtualizaOTituloEMantemFavoritoEMiniatura() {
        val biblioteca = BibliotecaDeVideosDoNavegador()
        biblioteca.registrar("https://site.test/v", "Antigo", "https://site.test/v", false, 1)
        biblioteca.definirFavorito("https://site.test/v", true)
        biblioteca.definirMiniatura("https://site.test/v", "miniatura-a.jpg")
        biblioteca.registrar("https://outro.test/v", "Outro", "https://outro.test/v", false, 2)
        val deNovo = biblioteca.registrar("https://site.test/v", "Novo", "https://site.test/v", false, 3)
        assertEquals("Novo", deNovo.titulo)
        assertTrue(deNovo.favorito)
        assertEquals("miniatura-a.jpg", deNovo.nomeDaMiniatura)
        assertEquals("https://site.test/v", biblioteca.recentes().first().chave)
        assertEquals(2, biblioteca.listar().size)
    }

    @Test
    fun recenteAntigoSemFavoritoSaiQuandoPassaDoLimite() {
        val biblioteca = BibliotecaDeVideosDoNavegador(limiteDeRecentes = 2)
        biblioteca.registrar("a", "A", "https://a", false, 1)
        biblioteca.definirFavorito("a", true)
        biblioteca.registrar("b", "B", "https://b", false, 2)
        biblioteca.registrar("c", "C", "https://c", false, 3)
        assertEquals(listOf("c", "b"), biblioteca.recentes().map { it.chave })
        assertEquals(listOf("a"), biblioteca.favoritos().map { it.chave })
        assertEquals(listOf("c", "b", "a"), biblioteca.listar().map { it.chave })
        assertNull(biblioteca.definirFavorito("a", false))
        assertEquals(listOf("c", "b"), biblioteca.listar().map { it.chave })
    }

    @Test
    fun removerDoHistoricoApagaQuemNaoEFavorito() {
        val biblioteca = BibliotecaDeVideosDoNavegador()
        biblioteca.registrar("https://site.test/v", "Clipe", "https://site.test/v", false, 1)
        biblioteca.definirMiniatura("https://site.test/v", "miniatura-a.jpg")
        val remocao = biblioteca.removerDoHistorico("https://site.test/v")
        assertTrue(remocao!!.apagouOItem)
        assertEquals("miniatura-a.jpg", remocao.nomeDaMiniatura)
        assertEquals(emptyList(), biblioteca.listar())
    }

    @Test
    fun favoritoSaiDoHistoricoEContinuaNosFavoritos() {
        val biblioteca = BibliotecaDeVideosDoNavegador()
        biblioteca.registrar("https://site.test/v", "Clipe", "https://site.test/v", false, 1)
        biblioteca.definirFavorito("https://site.test/v", true)
        val remocao = biblioteca.removerDoHistorico("https://site.test/v")
        assertFalse(remocao!!.apagouOItem)
        assertEquals(emptyList(), biblioteca.recentes())
        assertEquals(listOf("https://site.test/v"), biblioteca.favoritos().map { it.chave })
        biblioteca.registrar("https://site.test/v", "Clipe", "https://site.test/v", false, 2)
        assertEquals(listOf("https://site.test/v"), biblioteca.recentes().map { it.chave })
        assertTrue(biblioteca.favoritos().single().favorito)
    }

    @Test
    fun quadroPretoNaoViraMiniatura() {
        assertFalse(AvaliacaoDeQuadroParaMiniaturaDeVideo.serve(intArrayOf(0, 2, 4)))
        assertFalse(AvaliacaoDeQuadroParaMiniaturaDeVideo.serve(intArrayOf()))
        assertTrue(AvaliacaoDeQuadroParaMiniaturaDeVideo.serve(intArrayOf(40, 80, 120)))
    }
}
