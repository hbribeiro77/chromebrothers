package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoLimiteDeJanelasDocumentCentricDoNavegadorChromebrothers {
    @Test
    fun maximoEhTres() {
        assertEquals(3, LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.MAXIMO)
    }

    @Test
    fun permiteAbrirAteOLimite() {
        assertTrue(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(0))
        assertTrue(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(1))
        assertTrue(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(2))
        assertFalse(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(3))
        assertFalse(LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.podeAbrir(4))
    }

    @Test
    fun mensagemRecusadaCitaOLimite() {
        val texto = LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.mensagemRecusada()
        assertTrue(texto.contains("3"))
        assertTrue(texto.contains("janela", ignoreCase = true))
    }

    @Test
    fun chaveDoExtraDaJanelaEhEstavel() {
        assertEquals(
            "endereco_inicial_da_janela_do_navegador_chromebrothers",
            LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers.EXTRA_ENDERECO_INICIAL,
        )
    }
}
