package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoComportamentoDoTecladoPorSiteDoNavegador {
    @Test
    fun hospedeiroNormalizaSemWww() {
        assertEquals(
            "chatgpt.com",
            ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro("https://www.chatgpt.com/c/abc"),
        )
        assertEquals(
            "higgsfield.ai",
            ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro("https://higgsfield.ai/app"),
        )
        assertEquals("", ComportamentoDoTecladoPorSiteDoNavegador.hospedeiro(""))
    }

    @Test
    fun modoPadraoEAutomaticoQuandoNaoHaOverride() {
        assertEquals(
            ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            ComportamentoDoTecladoPorSiteDoNavegador.modoPara(emptyMap(), "chatgpt.com"),
        )
    }

    @Test
    fun guardaERemoveOverrideAutomatico() {
        val comSempre = ComportamentoDoTecladoPorSiteDoNavegador.guardar(
            emptyMap(),
            "chatgpt.com",
            ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
        )
        assertEquals(
            ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
            ComportamentoDoTecladoPorSiteDoNavegador.modoPara(comSempre, "chatgpt.com"),
        )
        val limpo = ComportamentoDoTecladoPorSiteDoNavegador.guardar(
            comSempre,
            "chatgpt.com",
            ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
        )
        assertTrue(limpo.isEmpty())
    }

    @Test
    fun serializaELerMapaDeModos() {
        val mapa = mapOf(
            "chatgpt.com" to ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
            "example.com" to ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR,
        )
        val bruto = ComportamentoDoTecladoPorSiteDoNavegador.gravar(mapa)
        val lido = ComportamentoDoTecladoPorSiteDoNavegador.ler(bruto)
        assertEquals(ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR, lido["chatgpt.com"])
        assertEquals(ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR, lido["example.com"])
        assertTrue(ComportamentoDoTecladoPorSiteDoNavegador.ler("").isEmpty())
        assertTrue(ComportamentoDoTecladoPorSiteDoNavegador.ler("lixo").isEmpty())
    }

    @Test
    fun comFocoNoChromeNaoDeslocaAPaginaSoAplicaClipping() {
        val ajuste = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 280f,
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO,
            focoNoChromeNativo = true,
        )
        assertEquals(0f, ajuste.deslocamentoY)
        assertEquals(280, ajuste.clippingInferiorPx)
        val semFolga = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 0f,
            focoNoChromeNativo = true,
        )
        assertEquals(0f, semFolga.deslocamentoY)
        assertEquals(0, semFolga.clippingInferiorPx)
        val naoDeslocar = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR,
            folgaDoTecladoPx = 280f,
            focoNoChromeNativo = true,
        )
        assertEquals(0f, naoDeslocar.deslocamentoY)
        assertEquals(0, naoDeslocar.clippingInferiorPx)
    }

    @Test
    fun ajusteVisualAutomaticoCombinaPanInteligenteEClipping() {
        val comMedida = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 400f,
            deslocamentoAutomatico = -120f,
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.PRONTA,
        )
        assertEquals(-120f, comMedida.deslocamentoY)
        assertEquals(400, comMedida.clippingInferiorPx)
        val aguardando = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 400f,
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.AGUARDANDO,
        )
        assertEquals(0f, aguardando.deslocamentoY)
        // Sem medida: pan cego (ChatGPT e a maioria). X/Twitter não — esconde a busca.
        val falhouChat = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 400f,
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.FALHOU,
            hospedeiro = "chatgpt.com",
        )
        assertEquals(-400f, falhouChat.deslocamentoY)
        assertEquals(400, falhouChat.clippingInferiorPx)
        val falhouX = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.AUTOMATICO,
            folgaDoTecladoPx = 400f,
            estadoDaMedidaDoFoco = EstadoDaMedidaDoFocoParaOTeclado.FALHOU,
            hospedeiro = "x.com",
        )
        assertEquals(0f, falhouX.deslocamentoY)
        assertEquals(400, falhouX.clippingInferiorPx)
        assertTrue(ComportamentoDoTecladoPorSiteDoNavegador.evitaPanCegoQuandoMedidaFalhar("twitter.com"))
        assertTrue(!ComportamentoDoTecladoPorSiteDoNavegador.evitaPanCegoQuandoMedidaFalhar("chatgpt.com"))
    }

    @Test
    fun ajusteVisualSempreDeslocaAViewSemClipping() {
        val ajuste = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
            folgaDoTecladoPx = 400f,
        )
        assertEquals(-400f, ajuste.deslocamentoY)
        assertEquals(0, ajuste.clippingInferiorPx)
        val parado = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.SEMPRE_DESLOCAR,
            folgaDoTecladoPx = 0f,
        )
        assertEquals(0f, parado.deslocamentoY)
        assertEquals(0, parado.clippingInferiorPx)
    }

    @Test
    fun ajusteVisualNaoDeslocaZeraTudo() {
        val ajuste = ComportamentoDoTecladoPorSiteDoNavegador.ajusteVisual(
            modo = ModoDoTecladoSobreAPaginaDoNavegador.NAO_DESLOCAR,
            folgaDoTecladoPx = 400f,
        )
        assertEquals(0f, ajuste.deslocamentoY)
        assertEquals(0, ajuste.clippingInferiorPx)
    }
}
