package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaEscritaDeTextoNoEditorDaPaginaParaOChat {
    @Test
    fun scriptLocalizaEditorGenericoEDocsEInsereSemSubstituirTudo() {
        val script = EscritaDeTextoNoEditorDaPaginaParaOChat.script("olá docs")
        assertTrue(script.contains("docs-texteventtarget-iframe"))
        assertTrue(script.contains("kix-appview-editor"))
        assertTrue(script.contains("contenteditable"))
        assertTrue(script.contains("insertText"))
        assertTrue(script.contains("olá docs"))
        assertTrue(script.contains("collapse(false)"))
        assertTrue(script.contains("\"ok\""))
        assertTrue(script.contains("\"sem-editor\""))
    }

    @Test
    fun relatosCobremCodigosConhecidos() {
        assertEquals(
            "Escrevi o texto no editor da página.",
            EscritaDeTextoNoEditorDaPaginaParaOChat.relato(EscritaDeTextoNoEditorDaPaginaParaOChat.OK),
        )
        assertTrue(
            EscritaDeTextoNoEditorDaPaginaParaOChat.relato(EscritaDeTextoNoEditorDaPaginaParaOChat.SEM_EDITOR)
                .contains("Não achei"),
        )
        assertTrue(
            EscritaDeTextoNoEditorDaPaginaParaOChat.relato(EscritaDeTextoNoEditorDaPaginaParaOChat.FALHOU)
                .contains("não aceitou"),
        )
    }
}
