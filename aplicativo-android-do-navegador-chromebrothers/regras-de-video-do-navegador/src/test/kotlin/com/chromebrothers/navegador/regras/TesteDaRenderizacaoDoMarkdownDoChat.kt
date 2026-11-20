package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaRenderizacaoDoMarkdownDoChat {
    @Test
    fun negritoCodigoTituloListaELinkHttp() {
        val trechos = RenderizacaoDoMarkdownDoChat.renderizar(
            "# Título\n**negrito** e `codigo`\n- item\n[site](https://exemplo.test/a)",
        )
        assertEquals(EstiloDoTrechoDoMarkdownDoChat.TITULO, trechos.first().estilo)
        assertEquals("Título", trechos.first().texto)
        assertTrue(trechos.any { it.texto == "negrito" && it.estilo == EstiloDoTrechoDoMarkdownDoChat.NEGRITO })
        assertTrue(trechos.any { it.texto == "codigo" && it.estilo == EstiloDoTrechoDoMarkdownDoChat.CODIGO })
        assertTrue(trechos.any { it.texto == "• " })
        val link = trechos.first { it.texto == "site" }
        assertEquals("https://exemplo.test/a", link.endereco)
    }

    @Test
    fun linkQueNaoEHttpFicaTextoEMarcaAbertaTambem() {
        val cru = RenderizacaoDoMarkdownDoChat.renderizar("[x](javascript:alert(1)) **ainda")
        assertTrue(cru.none { it.endereco.isNotEmpty() })
        assertTrue(cru.joinToString("") { it.texto }.contains("**ainda"))
        val bloco = RenderizacaoDoMarkdownDoChat.renderizar("```\nlinha\n```")
        assertEquals(EstiloDoTrechoDoMarkdownDoChat.CODIGO, bloco.single().estilo)
        assertEquals("linha", bloco.single().texto)
    }
}
