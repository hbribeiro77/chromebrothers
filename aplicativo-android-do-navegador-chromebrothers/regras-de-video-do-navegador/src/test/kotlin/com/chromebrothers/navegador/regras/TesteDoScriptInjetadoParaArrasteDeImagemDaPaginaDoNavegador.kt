package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertTrue

class TesteDoScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador {
    @Test
    fun scriptDisparaAvisosDeInicioMoverEFim() {
        val script = ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.texto()
        assertTrue(script.contains(ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_INICIO), script)
        assertTrue(script.contains(ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_MOVER), script)
        assertTrue(script.contains(ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.AVISO_FIM), script)
        assertTrue(script.contains("chromebrothers-aviso"), script)
        assertTrue(script.contains("300"), script)
        assertTrue(script.contains("contextmenu"), script)
    }

    @Test
    fun usaUmOuvinteNoDocumentoSemObservarODom() {
        val script = ScriptInjetadoParaArrasteDeImagemDaPaginaDoNavegador.texto()
        assertTrue(!script.contains("MutationObserver"), "sem observar o DOM (engasgava feeds)")
        assertTrue(!script.contains("img.addEventListener"), "sem ouvinte por imagem")
        assertTrue(script.contains("document.addEventListener(\"touchstart\""))
        // O único touchmove não-passivo é o do arraste em andamento.
        assertTrue(script.split("passive: false").size == 2)
    }
}
