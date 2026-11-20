package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoEnvioDeTextoParaOCampoDaPagina {
    @Test
    fun oVinculoDoCampoEDoEnvioSaoPedidosAParte() {
        val script = EnvioDeTextoParaOCampoDaPagina.instalador()
        assertTrue(script.contains("#1A73E8"))
        assertTrue(script.contains("#F9AB00"))
        assertTrue(script.contains("pointerdown"))
        assertTrue(script.contains("composedPath"))
        assertFalse(script.contains("document.activeElement"))
        assertTrue(EnvioDeTextoParaOCampoDaPagina.armarCampo().contains("__cbAguardandoVinculo = \"campo\""))
        assertTrue(EnvioDeTextoParaOCampoDaPagina.armarEnvio().contains("__cbAguardandoVinculo = \"envio\""))
        val estado = EnvioDeTextoParaOCampoDaPagina.estado()
        assertTrue(estado.contains("aguardando-campo"))
        assertTrue(estado.contains("aguardando-envio"))
        assertTrue(estado.contains("nenhum"))
    }

    @Test
    fun escreverClicaNoBotaoDeEnvioVinculado() {
        val script = EnvioDeTextoParaOCampoDaPagina.escrever("oi \"lá\"\nmais")
        assertTrue(script.contains("oi \\\"lá\\\"\\nmais"))
        assertFalse(script.contains("\nmais"))
        assertTrue(script.contains("__cbBotaoDeEnvio"))
        assertTrue(script.contains(".click()"))
        assertTrue(script.contains("PointerEvent"))
        assertTrue(script.contains("KeyboardEvent"))
        assertTrue(script.contains("\"Enter\""))
        assertTrue(script.contains("else teclarEnter(campo)"))
        assertTrue(script.contains("sem-campo"))
    }

    @Test
    fun espelharEscreveSemApertarOBotao() {
        val script = EnvioDeTextoParaOCampoDaPagina.espelhar("oi \"lá\"\nmais")
        assertTrue(script.contains("oi \\\"lá\\\"\\nmais"))
        assertFalse(script.contains("\nmais"))
        assertTrue(script.contains("sem-campo"))
        assertTrue(script.contains("insertText"))
        assertTrue(script.contains("insertHTML"))
        assertTrue(script.contains("blur()"))
        assertFalse(script.contains(".click()"))
        assertFalse(script.contains("__cbBotaoDeEnvio"))
        assertTrue(script.contains("beforeinput"))
        assertTrue(script.contains("deleteContent"))
        assertTrue(script.contains("if (campo.dispatchEvent(antes))"))
        assertTrue(script.contains("insertReplacementText"))
    }

    @Test
    fun lerTrazOTextoDaCaixaVinculada() {
        val script = EnvioDeTextoParaOCampoDaPagina.ler()
        assertTrue(script.contains("__cbCampoDeTexto"))
        assertTrue(script.contains("sem-campo"))
        assertTrue(script.contains("innerText"))
        assertTrue(script.contains("campo.value"))
        assertFalse(script.contains(".click()"))
    }

    @Test
    fun reaplicarProcuraAMarcaSalva() {
        val script = EnvioDeTextoParaOCampoDaPagina.reaplicar(
            MarcaDoElementoVinculado("caixa", "mensagem", "Escreva", "TEXTAREA", ""),
            MarcaDoElementoVinculado("", "", "Enviar", "BUTTON", "button"),
        )
        assertTrue(script.contains("caixa"))
        assertTrue(script.contains("Enviar"))
        assertTrue(script.contains("getElementById"))
        assertTrue(script.contains("contenteditable"))
        assertTrue(script.contains("tagName.toUpperCase()"))
    }

    @Test
    fun oCampoNaoUsaOTextoDigitadoNemIdQueMuda() {
        val script = EnvioDeTextoParaOCampoDaPagina.instalador()
        assertTrue(script.contains("aria-placeholder"))
        assertTrue(script.contains("rotuloEstavel"))
        assertFalse(script.contains("(elemento.innerText || elemento.value || \"\").replace"))
        assertEquals("", EnvioDeTextoParaOCampoDaPagina.idConfiavel(":r1:"))
        assertEquals("", EnvioDeTextoParaOCampoDaPagina.idConfiavel("ember12"))
        assertEquals("", EnvioDeTextoParaOCampoDaPagina.idConfiavel("8c2a1d90-1b34-4e6a-9c10-11aa22bb33cc"))
        assertEquals("APjFqb", EnvioDeTextoParaOCampoDaPagina.idConfiavel("APjFqb"))
        assertEquals("q", EnvioDeTextoParaOCampoDaPagina.idConfiavel("q"))
    }
}
