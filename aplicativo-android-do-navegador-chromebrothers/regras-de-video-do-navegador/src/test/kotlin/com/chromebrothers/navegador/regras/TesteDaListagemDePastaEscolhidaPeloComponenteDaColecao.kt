package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaListagemDePastaEscolhidaPeloComponenteDaColecao {
    @Test
    fun montaJsonComPastaEItens() {
        val json = ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemOk(
            nomePasta = "Download",
            uriPasta = "content://tree/download",
            itens = listOf(
                ItemDaPastaEscolhidaPeloComponente(
                    nome = "foto.jpg",
                    uri = "content://doc/foto",
                    mime = "image/jpeg",
                    pasta = false,
                    tamanho = 12L,
                ),
                ItemDaPastaEscolhidaPeloComponente(
                    nome = "docs",
                    uri = "content://doc/docs",
                    mime = "",
                    pasta = true,
                    tamanho = 0L,
                ),
            ),
        )
        assertTrue(json.contains("\"ok\":true"))
        assertTrue(json.contains("\"nome\":\"Download\""))
        assertTrue(json.contains("\"uri\":\"content://tree/download\""))
        assertTrue(json.contains("\"nome\":\"foto.jpg\""))
        assertTrue(json.contains("\"pasta\":true"))
        assertTrue(json.contains("\"mime\":\"image/jpeg\""))
    }

    @Test
    fun escapaAspasNoNome() {
        val json = ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemOk(
            nomePasta = "Meu \"Download\"",
            uriPasta = "content://x",
            itens = emptyList(),
        )
        assertTrue(json.contains("Meu \\\"Download\\\""))
    }

    @Test
    fun mensagemCancelada() {
        val json = ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemCancelada()
        assertTrue(json.contains("\"ok\":false"))
        assertFalse(json.contains("\"ok\":true"))
    }

    @Test
    fun instrucaoDocumentaEscolherPasta() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")  // a ponte chromebrothers.* agora vive no manual sob demanda
        assertTrue(texto.contains("chromebrothers.escolherPasta"))
        assertTrue(texto.contains("chromebrothers.listarPasta") || texto.contains("listarPasta"))
        assertTrue(texto.contains("content://"))
        assertTrue(texto.contains("img") || texto.contains("src"))
    }

    @Test
    fun limitaQuantidadeDeItens() {
        val itens = (1..200).map { n ->
            ItemDaPastaEscolhidaPeloComponente(
                nome = "a$n",
                uri = "content://$n",
                mime = "text/plain",
                pasta = false,
                tamanho = 1L,
            )
        }
        val json = ListagemDePastaEscolhidaPeloComponenteDaColecao.mensagemOk(
            nomePasta = "X",
            uriPasta = "content://x",
            itens = itens,
        )
        assertEquals(
            ListagemDePastaEscolhidaPeloComponenteDaColecao.LIMITE_DE_ITENS,
            Regex("\"nome\":\"a\\d+\"").findAll(json).count(),
        )
        assertTrue(json.contains("\"cortados\":"))
    }
}
