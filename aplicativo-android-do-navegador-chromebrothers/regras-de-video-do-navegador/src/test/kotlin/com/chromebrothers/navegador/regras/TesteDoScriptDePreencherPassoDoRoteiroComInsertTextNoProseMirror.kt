package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertTrue

class TesteDoScriptDePreencherPassoDoRoteiroComInsertTextNoProseMirror {
    @Test
    fun preencherUsaInsertTextEBuscaProseMirror() {
        val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.preencher(
            seletor = ".ProseMirror",
            texto = "Pergunte ao ChatGPT",
            valor = "ola",
        )
        assertTrue(script.contains("insertText"), script)
        assertTrue(script.contains("ProseMirror"), script)
        assertTrue(script.contains("execCommand"), script)
        assertTrue(script.contains("isContentEditable"), script)
    }
}
