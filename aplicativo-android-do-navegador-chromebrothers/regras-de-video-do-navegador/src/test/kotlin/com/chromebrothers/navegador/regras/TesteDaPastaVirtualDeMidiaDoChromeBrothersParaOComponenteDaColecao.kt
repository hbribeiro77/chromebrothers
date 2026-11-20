package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaPastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao {
    @Test
    fun reconheceUriVirtual() {
        assertTrue(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehUriVirtual("chromebrothers://midia"),
        )
        assertTrue(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehUriVirtual(" chromebrothers://midia "),
        )
        assertFalse(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehUriVirtual("content://tree/download"),
        )
    }

    @Test
    fun montaListagemComNomeEUriFixos() {
        val json = PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.montarListagem(
            listOf(
                ItemDaPastaEscolhidaPeloComponente(
                    nome = "clip.mp4",
                    uri = "content://downloads/1",
                    mime = "video/mp4",
                    pasta = false,
                    tamanho = 10L,
                ),
                ItemDaPastaEscolhidaPeloComponente(
                    nome = "quadro.png",
                    uri = "content://images/2",
                    mime = "image/png",
                    pasta = false,
                    tamanho = 3L,
                ),
            ),
        )
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("\"nome\":\"ChromeBrothers\""))
        assertTrue(json.contains("\"uri\":\"chromebrothers://midia\""))
        assertTrue(json.contains("clip.mp4"))
        assertTrue(json.contains("quadro.png"))
        assertTrue(json.contains("video/mp4"))
        assertTrue(json.contains("image/png"))
    }

    @Test
    fun identificaVisualizadorPeloNome() {
        assertTrue(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehVisualizadorDeMidiaPeloNome("Visualizador de mídia"),
        )
        assertTrue(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehVisualizadorDeMidiaPeloNome("visualizador de midia — v2"),
        )
        assertFalse(
            PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
                .ehVisualizadorDeMidiaPeloNome("Geração MCP imagem"),
        )
    }

    @Test
    fun warmStartContinuaReconhecendoVisualizador() {
        val item = ItemDaBibliotecaDeComponentesHtmlDaColecao(
            id = "x",
            nome = "Visualizador de mídia",
            html = "<div></div>",
            atualizadoEm = 1L,
        )
        assertTrue(WarmStartDeComponentesDaColecaoDoNavegador.marcadoNoComponente(item))
    }

    @Test
    fun mapeiaArquivoDaGavetaParaItemDaPasta() {
        val item = PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.itemDaPasta(
            ArquivoDeMidiaSalvoNaGaveta(
                uri = "content://d/9",
                nomeDoArquivo = "a.mp4",
                instante = 1000L,
                tamanho = 44L,
                mime = "video/mp4",
            ),
        )
        assertEquals("a.mp4", item.nome)
        assertEquals("content://d/9", item.uri)
        assertEquals("video/mp4", item.mime)
        assertFalse(item.pasta)
        assertEquals(44L, item.tamanho)
    }
}
