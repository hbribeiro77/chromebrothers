package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaOrigemEFiltroNaBibliotecaDeMidia {
    @Test
    fun parseOrigemVaziaViraDesconhecido() {
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(null))
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(""))
        assertEquals("topview", OrigemDaMidiaNaBibliotecaDoNavegador.parse("Topview"))
        assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse("outra-coisa"))
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, "higgsfield")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.SJINN, "sjinn")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW, "topview")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.PAGINA, "pagina")
        assertEquals(OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO, "desconhecido")
    }

    @Test
    fun rotuloDaOrigem() {
        assertEquals("Higgsfield", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("higgsfield"))
        assertEquals("Sjinn", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("sjinn"))
        assertEquals("Topview", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("TOPVIEW"))
        assertEquals("Página", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("pagina"))
        assertEquals("Desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo("nao-existe"))
        assertEquals("Desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.rotulo(""))
    }

    @Test
    fun filtroPorOrigem() {
        val a = ItemDaBibliotecaDeVideos("1", "t", "file://x", true, false, 1L, "", origem = "sjinn")
        val b = a.copy(chave = "2", origem = "topview")
        assertEquals(listOf(a), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "sjinn"))
        assertEquals(2, FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "todos").size)
        assertEquals(listOf(a, b), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), ""))
        assertEquals(listOf(a, b), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "   "))
    }

    @Test
    fun filtroDeImagensPorOrigem() {
        val higgsfield = ItemDaBibliotecaDeImagensCapturadas(
            id = "1",
            instante = 1L,
            nomeDoArquivo = "a.jpg",
            naGaleria = false,
            favorito = false,
            origem = "higgsfield",
        )
        val pagina = higgsfield.copy(id = "2", origem = "pagina")
        val itens = listOf(higgsfield, pagina)
        assertEquals(listOf(higgsfield), FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, "higgsfield"))
        assertEquals(itens, FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, "todos"))
        assertEquals(itens, FiltroPorOrigemNaBibliotecaDeMidia.filtrarImagens(itens, ""))
    }

    @Test
    fun registrarGuardaPromptEOrigem() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar("sjinn-1", "curto", "file://a", true, 9L, origem = "sjinn", modelo = "wan3", prompt = "onda grande", refs = listOf("r1.jpg"), caminhoLocal = "/a.mp4")
        val item = bib.buscar("sjinn-1")!!
        assertEquals("sjinn", item.origem)
        assertEquals("onda grande", item.prompt)
        assertEquals(listOf("r1.jpg"), item.refs)
        assertEquals("wan3", item.modelo)
        assertEquals("/a.mp4", item.caminhoLocal)
        assertFalse(item.exportado)
    }

    @Test
    fun registrarSemMetadadosNasceDesconhecidoEVazio() {
        val video = ItemDaBibliotecaDeVideos("1", "t", "file://x", true, false, 1L, "")
        assertEquals("desconhecido", video.origem)
        assertEquals("", video.modelo)
        assertEquals("", video.prompt)
        assertEquals(emptyList(), video.refs)
        assertEquals("", video.caminhoLocal)
        assertFalse(video.exportado)
        val registrado = BibliotecaDeVideosDoNavegador().registrar("k", "t", "file://x", false, 1L)
        assertEquals("desconhecido", registrado.origem)
        assertEquals("", registrado.prompt)
        assertFalse(registrado.exportado)
    }

    @Test
    fun registrarDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar(
            "k",
            "titulo",
            "file://a",
            true,
            1L,
            origem = "sjinn",
            prompt = "onda",
            refs = listOf("r1.jpg"),
            caminhoLocal = "/a.mp4",
        )
        val deNovo = bib.registrar("k", "titulo novo", "file://b", false, 2L)
        assertEquals("sjinn", deNovo.origem)
        assertEquals("onda", deNovo.prompt)
        assertEquals(listOf("r1.jpg"), deNovo.refs)
        assertEquals("/a.mp4", deNovo.caminhoLocal)
        val atualizado = bib.registrar("k", "titulo novo", "file://b", false, 3L, origem = "Topview")
        assertEquals("topview", atualizado.origem)
        assertEquals("onda", atualizado.prompt)
        assertEquals(listOf("r1.jpg"), atualizado.refs)
        assertEquals("/a.mp4", atualizado.caminhoLocal)
    }

    @Test
    fun registrarImagemDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza() {
        val imagens = BibliotecaDeImagensCapturadasDoNavegador()
        imagens.registrar(
            "img-1",
            1L,
            "a.jpg",
            false,
            origem = "higgsfield",
            prompt = "gato",
            refs = listOf("r.png"),
            caminhoLocal = "/a.jpg",
        )
        val deNovo = imagens.registrar("img-1", 2L, "b.jpg", true)
        assertEquals("higgsfield", deNovo.origem)
        assertEquals("gato", deNovo.prompt)
        assertEquals(listOf("r.png"), deNovo.refs)
        assertEquals("/a.jpg", deNovo.caminhoLocal)
        val atualizado = imagens.registrar("img-1", 3L, "b.jpg", true, origem = "pagina")
        assertEquals("pagina", atualizado.origem)
        assertEquals("gato", atualizado.prompt)
        assertEquals(listOf("r.png"), atualizado.refs)
        assertEquals("/a.jpg", atualizado.caminhoLocal)
    }

    @Test
    fun registrarDeNovoMantemFavoritoMiniaturaEExportado() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar("k", "antigo", "file://a", true, 1L, origem = "sjinn", prompt = "antes", caminhoLocal = "/a.mp4")
        bib.definirFavorito("k", true)
        bib.definirMiniatura("k", "mini.jpg")
        bib.marcarExportado("k")
        val deNovo = bib.registrar(
            "k",
            "novo",
            "file://b",
            true,
            2L,
            origem = "Topview",
            modelo = "wan",
            prompt = "depois",
            refs = listOf("n.jpg"),
            caminhoLocal = "/b.mp4",
        )
        assertEquals("novo", deNovo.titulo)
        assertTrue(deNovo.favorito)
        assertEquals("mini.jpg", deNovo.nomeDaMiniatura)
        assertTrue(deNovo.exportado)
        assertEquals("topview", deNovo.origem)
        assertEquals("depois", deNovo.prompt)
        assertEquals(listOf("n.jpg"), deNovo.refs)
        assertEquals("/b.mp4", deNovo.caminhoLocal)
    }

    @Test
    fun favoritoMiniaturaERemocaoNaoApagamMetadadosDoVideo() {
        val bib = BibliotecaDeVideosDoNavegador()
        bib.registrar(
            "k",
            "t",
            "file://a",
            true,
            1L,
            origem = "pagina",
            modelo = "m",
            prompt = "oi",
            refs = listOf("r"),
            caminhoLocal = "/a",
        )
        bib.definirFavorito("k", true)
        bib.definirMiniatura("k", "m.jpg")
        val favorito = bib.buscar("k")!!
        assertEquals("pagina", favorito.origem)
        assertEquals("oi", favorito.prompt)
        assertEquals(listOf("r"), favorito.refs)
        assertEquals("m.jpg", favorito.nomeDaMiniatura)
        val remocao = bib.removerDoHistorico("k")
        assertFalse(remocao!!.apagouOItem)
        val fora = bib.buscar("k")!!
        assertTrue(fora.foraDoHistorico)
        assertEquals("oi", fora.prompt)
        assertEquals("/a", fora.caminhoLocal)
        assertEquals("m", fora.modelo)
    }

    @Test
    fun marcarExportadoNoVideo() {
        val videos = BibliotecaDeVideosDoNavegador()
        videos.registrar("v", "t", "file://v", true, 1L, caminhoLocal = "/v.mp4")
        assertFalse(videos.buscar("v")!!.exportado)
        assertNull(videos.marcarExportado("nao-existe"))
        val marcado = videos.marcarExportado("v")
        assertTrue(marcado!!.exportado)
        assertEquals("/v.mp4", marcado.caminhoLocal)
        assertEquals("desconhecido", marcado.origem)
        assertTrue(videos.buscar("v")!!.exportado)
    }

    @Test
    fun registrarImagemGuardaPromptEOrigemEMarcarExportado() {
        val imagens = BibliotecaDeImagensCapturadasDoNavegador()
        val criada = imagens.registrar(
            "img-1",
            3L,
            "a.jpg",
            false,
            origem = "Higgsfield",
            modelo = "flux",
            prompt = "gato",
            refs = listOf("r.png"),
            caminhoLocal = "/a.jpg",
        )
        assertEquals("higgsfield", criada.origem)
        assertEquals("flux", criada.modelo)
        assertEquals("gato", criada.prompt)
        assertEquals(listOf("r.png"), criada.refs)
        assertEquals("/a.jpg", criada.caminhoLocal)
        assertFalse(criada.exportado)
        val solta = ItemDaBibliotecaDeImagensCapturadas("z", 1L, "z.jpg", false, false)
        assertEquals("desconhecido", solta.origem)
        assertEquals("", solta.prompt)
        assertEquals(emptyList(), solta.refs)
        assertEquals("", solta.caminhoLocal)
        assertFalse(solta.exportado)
        assertNull(imagens.marcarExportado("nao"))
        val marcada = imagens.marcarExportado("img-1")
        assertTrue(marcada!!.exportado)
        assertEquals("gato", marcada.prompt)
        imagens.definirFavorito("img-1", true)
        val favorita = imagens.buscar("img-1")!!
        assertTrue(favorita.favorito)
        assertTrue(favorita.exportado)
        assertEquals("higgsfield", favorita.origem)
        assertEquals(listOf("r.png"), favorita.refs)
    }
}
