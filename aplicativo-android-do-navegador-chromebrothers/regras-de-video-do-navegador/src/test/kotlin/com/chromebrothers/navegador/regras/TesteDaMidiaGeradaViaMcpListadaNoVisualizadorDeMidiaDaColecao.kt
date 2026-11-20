package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.Filtro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaMidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao {
    private val regra = MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao
    private val pastaDasImagens = "/data/app/files/biblioteca-de-imagens-capturadas"
    private val pastaDosVideos = "/data/app/files/biblioteca-de-videos-higgsfield"

    private fun imagem(id: String, origem: String, instante: Long, caminho: String = "") =
        ItemDaBibliotecaDeImagensCapturadas(
            id = id,
            instante = instante,
            nomeDoArquivo = "$id.png",
            naGaleria = false,
            favorito = false,
            origem = origem,
            caminhoLocal = caminho,
        )

    private fun video(chave: String, origem: String, instante: Long) =
        ItemDaBibliotecaDeVideos(
            chave = chave,
            titulo = chave,
            enderecoParaAbrir = "",
            abreNoPlayerDeArquivo = true,
            favorito = false,
            instante = instante,
            nomeDaMiniatura = "",
            origem = origem,
            caminhoLocal = "$pastaDosVideos/$chave.mp4",
        )

    @Test
    fun listaSoAsDeOrigemMcpMaisRecentesPrimeiro() {
        val itens = regra.listar(
            imagens = listOf(
                imagem("captura", OrigemDaMidiaNaBibliotecaDoNavegador.PAGINA, 50),
                imagem("img-sjinn", OrigemDaMidiaNaBibliotecaDoNavegador.SJINN, 30),
            ),
            videos = listOf(
                video("topview-1", OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW, 40),
                video("antigo", OrigemDaMidiaNaBibliotecaDoNavegador.DESCONHECIDO, 60),
            ),
            pastaDasImagens = pastaDasImagens,
            tamanhoDoArquivo = { 10L },
        )
        assertEquals(listOf("topview-1.mp4", "img-sjinn.png"), itens.map { it.nomeDoArquivo })
        assertEquals("video/mp4", itens[0].mime)
        assertEquals("image/png", itens[1].mime)
        assertTrue(itens.all { regra.ehDoMcp(it.uri) })
    }

    @Test
    fun arquivoQueSumiuFicaDeFora() {
        val itens = regra.listar(
            imagens = listOf(imagem("img", OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, 1)),
            videos = listOf(video("vid", OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, 2)),
            pastaDasImagens = pastaDasImagens,
            tamanhoDoArquivo = { caminho -> if (caminho.endsWith(".mp4")) -1L else 5L },
        )
        assertEquals(listOf("img.png"), itens.map { it.nomeDoArquivo })
        assertTrue(itens[0].uri.contains("/biblioteca-de-imagens-capturadas/img.png"))
    }

    @Test
    fun filtroMcpPegaSoArquivosDasPastasDoMcp() {
        assertTrue(regra.ehDoMcp("file:/data/app/files/biblioteca-de-videos-higgsfield/a.mp4"))
        assertFalse(regra.ehDoMcp("content://media/external/images/media/9"))
        val itens = listOf(
            ItemDaPastaEscolhidaPeloComponente("a.mp4", "file:/x/biblioteca-de-videos-higgsfield/a.mp4", "video/mp4", false, 1),
            ItemDaPastaEscolhidaPeloComponente("b.jpg", "content://x/b.jpg", "image/jpeg", false, 1),
        )
        val regras = RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao
        assertEquals(listOf("a.mp4"), regras.filtrar(itens, Filtro.MCP).map { it.nome })
        assertEquals(Filtro.MCP, Filtro.daChave("mcp"))
    }

    @Test
    fun excluirNoVisualizadorTiraOItemDaBiblioteca() {
        val videos = listOf(
            video("fica", OrigemDaMidiaNaBibliotecaDoNavegador.SJINN, 1),
            video("sai", OrigemDaMidiaNaBibliotecaDoNavegador.SJINN, 2),
        )
        val uriDoQueSai = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho("$pastaDosVideos/sai.mp4")
        val comBarrasTriplas = uriDoQueSai.replaceFirst("file:/", "file:///")
        assertEquals(listOf("fica"), regra.videosRestantes(videos, listOf(comBarrasTriplas)).map { it.chave })
        assertEquals(2, regra.videosRestantes(videos, emptyList()).size)

        val imagens = listOf(
            imagem("velha", OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, 1),
            imagem("outra", OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD, 2),
        )
        val uriDaVelha = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho("$pastaDasImagens/velha.png")
        assertEquals(
            listOf("outra"),
            regra.imagensRestantes(imagens, listOf(uriDaVelha), pastaDasImagens).map { it.id },
        )
    }

    @Test
    fun playerRecebeSoVideosComIndiceDoTocado() {
        val itens = listOf(
            ItemDaPastaEscolhidaPeloComponente("a.mp4", "u/a.mp4", "video/mp4", false, 1),
            ItemDaPastaEscolhidaPeloComponente("b.jpg", "u/b.jpg", "image/jpeg", false, 1),
            ItemDaPastaEscolhidaPeloComponente("c.mov", "u/c.mov", "", false, 1),
        )
        val (lista, indice) = RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.videosParaOPlayer(itens, "u/c.mov")
        assertEquals(listOf("u/a.mp4", "u/c.mov"), lista)
        assertEquals(1, indice)
    }
}
