package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoEnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox {
    @Test
    fun arquivoDaLojaEhInstalacao() {
        assertTrue(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "https://addons.mozilla.org/firefox/downloads/file/5034826/ublock_origin-1.75.0.xpi",
            ),
        )
        assertTrue(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "https://addons.mozilla.org/firefox/downloads/file/1/addon.xpi?src=search",
            ),
        )
    }

    @Test
    fun atalhoDaVersaoMaisRecenteEhInstalacao() {
        assertTrue(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "https://addons.mozilla.org/firefox/downloads/latest/ublock-origin/latest.xpi",
            ),
        )
    }

    @Test
    fun fichaDaExtensaoNaoEhInstalacao() {
        assertFalse(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "https://addons.mozilla.org/pt-BR/firefox/addon/ublock-origin/",
            ),
        )
    }

    @Test
    fun outroSiteOuEnderecoSemCadeadoNaoEhInstalacao() {
        assertFalse(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "https://exemplo.test/firefox/downloads/file/1/addon.xpi",
            ),
        )
        assertFalse(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.eh(
                "http://addons.mozilla.org/firefox/downloads/file/1/addon.xpi",
            ),
        )
    }

    @Test
    fun cliqueNaLojaBaixaEORedirecionamentoTambem() {
        val arquivo = "https://addons.mozilla.org/firefox/downloads/file/1/addon.xpi"
        assertTrue(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.deveBaixar(
                arquivo,
                temGestoDoUsuario = true,
                ehRedirecionamento = false,
            ),
        )
        assertTrue(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.deveBaixar(
                arquivo,
                temGestoDoUsuario = false,
                ehRedirecionamento = true,
            ),
        )
        assertFalse(
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.deveBaixar(
                arquivo,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
            ),
        )
    }

    @Test
    fun lojaAbreNaPaginaEmPortugues() {
        assertEquals(
            "https://addons.mozilla.org/pt-BR/firefox/extensions/",
            EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox.PAGINA,
        )
    }
}
