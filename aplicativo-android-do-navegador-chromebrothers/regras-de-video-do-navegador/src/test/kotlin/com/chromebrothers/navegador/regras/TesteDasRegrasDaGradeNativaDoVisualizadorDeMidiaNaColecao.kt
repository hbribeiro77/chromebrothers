package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao.Filtro
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasRegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao {
    private val regras = RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao

    private fun item(nome: String, mime: String, pasta: Boolean = false) =
        ItemDaPastaEscolhidaPeloComponente(
            nome = nome,
            uri = "content://x/$nome",
            mime = mime,
            pasta = pasta,
            tamanho = 1L,
        )

    @Test
    fun colunasCabemNaLarguraEModoMaioresUsaDuas() {
        assertEquals(3, regras.colunas(larguraDisponivelPx = 1000, densidade = 3f, maiores = false))
        assertEquals(1, regras.colunas(larguraDisponivelPx = 100, densidade = 3f, maiores = false))
        assertEquals(2, regras.colunas(larguraDisponivelPx = 100, densidade = 3f, maiores = true))
    }

    @Test
    fun filtroSeparaImagensEVideosEIgnoraPastas() {
        val itens = listOf(
            item("a.jpg", "image/jpeg"),
            item("b.mp4", "video/mp4"),
            item("c.mov", ""),
            item("dir", "", pasta = true),
            item("captura.pdf", "application/pdf"),
            item("pacote.zip", "application/zip"),
        )
        assertEquals(3, regras.filtrar(itens, Filtro.TUDO).size)
        assertEquals(listOf("a.jpg"), regras.filtrar(itens, Filtro.IMAGENS).map { it.nome })
        assertEquals(listOf("b.mp4", "c.mov"), regras.filtrar(itens, Filtro.VIDEOS).map { it.nome })
        assertEquals(Filtro.VIDEOS, Filtro.daChave("videos"))
        assertEquals(Filtro.TUDO, Filtro.daChave("???"))
    }

    @Test
    fun visorRecebeSoImagensComIndiceDaTocada() {
        val itens = listOf(
            item("a.jpg", "image/jpeg"),
            item("b.mp4", "video/mp4"),
            item("c.png", "image/png"),
        )
        val (lista, indice) = regras.imagensParaOVisor(itens, "content://x/c.png")
        assertEquals(listOf("content://x/a.jpg", "content://x/c.png"), lista)
        assertEquals(1, indice)
    }

    @Test
    fun folgaDeToqueSeparaToqueParadoDeRolagem() {
        assertFalse(regras.moveuAlemDaFolga(3f, 4f, 10f))
        assertTrue(regras.moveuAlemDaFolga(8f, 8f, 10f))
    }
}
