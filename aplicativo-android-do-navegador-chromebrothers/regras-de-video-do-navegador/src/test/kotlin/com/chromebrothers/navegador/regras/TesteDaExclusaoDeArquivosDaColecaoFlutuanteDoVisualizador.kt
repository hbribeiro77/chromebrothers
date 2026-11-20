package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador {
    @Test
    fun parseiaArrayDeUris() {
        val uris = ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.parseUris(
            """["content://a/1"," file://b/2 ",""]""",
        )
        assertEquals(listOf("content://a/1", "file://b/2"), uris)
    }

    @Test
    fun parseiaJsonInvalidoComoVazio() {
        assertTrue(ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.parseUris("nao-json").isEmpty())
        assertTrue(ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.parseUris(null).isEmpty())
    }

    @Test
    fun montaMensagemComExcluidosEFalhas() {
        val json = ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.mensagemResultado(
            excluidos = listOf("content://ok"),
            falhas = listOf("content://x"),
        )
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("\"excluidos\":1"))
        assertTrue(json.contains("\"falhas\":1"))
        assertTrue(json.contains("content://ok"))
        assertTrue(json.contains("content://x"))
    }

    @Test
    fun semUrisViraErro() {
        val json = ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.mensagemSemUris()
        assertTrue(json.contains("\"ok\":false"))
    }
}
