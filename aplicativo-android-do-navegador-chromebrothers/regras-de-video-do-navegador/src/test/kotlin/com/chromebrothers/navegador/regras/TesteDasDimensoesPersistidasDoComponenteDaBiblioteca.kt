package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDasDimensoesPersistidasDoComponenteDaBiblioteca {

    @Test
    fun indiceSemDimensoesContinuaValido() {
        val lidos = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(
            """[{"id":"a","nome":"Card","atualizadoEm":1}]""",
        )
        assertEquals(1, lidos.size)
        assertNull(lidos[0].larguraPx)
        assertNull(lidos[0].alturaPx)
    }

    @Test
    fun indiceComDimensoesRoundTrip() {
        val original = listOf(
            MetadadosDoComponenteNoIndiceDaBiblioteca(
                id = "x",
                nome = "Painel",
                descricao = "",
                tags = emptyList(),
                atualizadoEm = 9L,
                larguraPx = 640,
                alturaPx = 720,
            ),
        )
        val escrito = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.escrever(original)
        assertTrue(escrito.contains("\"larguraPx\":640"))
        assertTrue(escrito.contains("\"alturaPx\":720"))
        assertEquals(original, LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(escrito))
    }

    @Test
    fun atualizarDimensoesNaoMexeNoHtmlNemNaOrdem() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao("1", "A", "<p>a</p>", 1L),
                ItemDaBibliotecaDeComponentesHtmlDaColecao("2", "B", "<p>b</p>", 2L),
            ),
        )
        assertTrue(biblioteca.atualizarDimensoes("2", 500, 600, agora = 99L))
        val itens = biblioteca.listar()
        assertEquals(listOf("1", "2"), itens.map { it.id })
        assertEquals("<p>b</p>", itens[1].html)
        assertEquals(500, itens[1].larguraPx)
        assertEquals(600, itens[1].alturaPx)
        assertEquals(99L, itens[1].atualizadoEm)
    }

    @Test
    fun guardarPreservaDimensoesSeNaoInformadas() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao(
                    id = "1",
                    nome = "A",
                    html = "<p>a</p>",
                    atualizadoEm = 1L,
                    larguraPx = 400,
                    alturaPx = 500,
                ),
            ),
        )
        val ok = biblioteca.guardar(
            nome = "A2",
            html = "<p>a2</p>",
            idExistente = "1",
            agora = 2L,
            novoId = { "novo" },
        ) as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertEquals(400, ok.item.larguraPx)
        assertEquals(500, ok.item.alturaPx)
    }

    @Test
    fun guardarPodeTrocarDimensoes() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao("1", "A", "<p>a</p>", 1L, larguraPx = 400, alturaPx = 500),
            ),
        )
        val ok = biblioteca.guardar(
            nome = "A",
            html = "<p>a</p>",
            idExistente = "1",
            agora = 3L,
            novoId = { "novo" },
            larguraPx = 700,
            alturaPx = 800,
        ) as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertEquals(700, ok.item.larguraPx)
        assertEquals(800, ok.item.alturaPx)
    }
}
