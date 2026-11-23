package com.chromebrothers.navegador.regras

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaPaginaDeErroDeCarregamentoDoNavegador {
    private val regra = PaginaDeErroDeCarregamentoDoNavegador

    @Test
    fun todoTipoTemTituloExplicacaoEDica() {
        TipoDeErroDeCarregamento.entries.forEach { tipo ->
            val t = regra.texto(tipo, "exemplo.com")
            assertTrue(t.titulo.isNotBlank() && t.explicacao.isNotBlank() && t.dica.isNotBlank(), "$tipo")
        }
    }

    @Test
    fun siteNaoEncontradoCitaOEnderecoETemTentarDeNovo() {
        val html = regra.html(TipoDeErroDeCarregamento.ENDERECO_NAO_ENCONTRADO, "https://www.dominio-errado.com/a", escura = true)
        assertTrue(html.contains("Site não encontrado"))
        assertTrue(html.contains("dominio-errado.com"))
        assertTrue(html.contains("href=\"https://www.dominio-errado.com/a\">Tentar de novo"))
        assertTrue(html.contains("#202124"), "tema escuro")
    }

    @Test
    fun certificadoInvalidoESitePerigosoSoOferecemVoltar() {
        listOf(TipoDeErroDeCarregamento.CERTIFICADO_INVALIDO, TipoDeErroDeCarregamento.SITE_PERIGOSO).forEach { tipo ->
            val html = regra.html(tipo, "https://expired.badssl.com/", escura = false)
            assertFalse(html.contains("Tentar de novo"), "$tipo")
            assertTrue(html.contains("Voltar"), "$tipo")
        }
    }

    @Test
    fun enderecoComHtmlEScriptNaoEntraCruNaPagina() {
        val malicioso = "https://x.com/\"><script>alert(1)</script>"
        val html = regra.html(TipoDeErroDeCarregamento.DESCONHECIDO, malicioso, escura = false)
        assertFalse(html.contains("<script>"))
        val js = regra.html(TipoDeErroDeCarregamento.DESCONHECIDO, "javascript:alert(1)", escura = false)
        assertFalse(js.contains("href=\"javascript:alert"), "só http(s) vira link de tentar de novo")
    }

    @Test
    fun paginaVaiComoDataUriEmUtf8() {
        val uri = regra.enderecoDaPagina(TipoDeErroDeCarregamento.SEM_INTERNET, "https://a.com/", escura = false)
        assertTrue(uri.startsWith("data:text/html;charset=utf-8;base64,"))
        val html = String(Base64.getDecoder().decode(uri.substringAfter("base64,")), Charsets.UTF_8)
        assertTrue(html.contains("Sem conexão com a internet"))
        assertEquals("a.com", regra.hostDe("https://www.a.com/x"))
    }
}
