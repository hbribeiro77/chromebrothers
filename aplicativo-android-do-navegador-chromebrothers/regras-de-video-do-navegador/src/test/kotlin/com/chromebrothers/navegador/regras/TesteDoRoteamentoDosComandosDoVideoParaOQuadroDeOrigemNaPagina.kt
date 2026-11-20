package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoRoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina {
    private val regra = RoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina

    @Test
    fun extraiOIdDosComandosDoPlayer() {
        assertEquals("vk3x9a-1", regra.idDoVideoNoComando("window.chromebrothersEntrarNoPlayer(\"vk3x9a-1\")"))
        assertEquals("vk3x9a-2", regra.idDoVideoNoComando("window.chromebrothersDefinirPosicao(\"vk3x9a-2\", 12.5)"))
        assertEquals("v1", regra.idDoVideoNoComando("window.chromebrothersCapturarQuadro(\"v1\", 960)"))
    }

    @Test
    fun scriptsQueNaoSaoComandoDeVideoNaoTemId() {
        assertNull(regra.idDoVideoNoComando("document.title"))
        assertNull(regra.idDoVideoNoComando("window.chromebrothersDefinirCapturaDeMidia(true)"))
        assertNull(regra.idDoVideoNoComando("alert(window.chromebrothersEntrarNoPlayer(\"v1\"))"))
    }

    @Test
    fun soOsAvisosDeVideoRegistramOQuadro() {
        assertEquals("vab-1", regra.idDoVideoNoAviso("onVideoPlaying", "vab-1"))
        assertEquals("vab-1", regra.idDoVideoNoAviso("onVideoBounds", "vab-1"))
        assertNull(regra.idDoVideoNoAviso("onVideoStopped", "vab-1"))
        assertNull(regra.idDoVideoNoAviso("onMediaUrl", "https://x"))
        assertNull(regra.idDoVideoNoAviso("onVideoPlaying", ""))
    }
}
