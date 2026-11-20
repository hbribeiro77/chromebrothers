package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Acao
import com.chromebrothers.navegador.regras.AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Modo
import com.chromebrothers.navegador.regras.AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando.Onde
import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaAberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando {
    private val regra = AberturaPadraoDoComponenteDaColecaoNoChatOuFlutuando

    @Test
    fun fechadoOuOcultoSegueOPadraoDoUsuario() {
        assertEquals(Acao.ABRIR_NO_CHAT, regra.decidir(Modo.NO_CHAT, false, Onde.FECHADO))
        assertEquals(Acao.ABRIR_NO_CHAT, regra.decidir(Modo.NO_CHAT, false, Onde.OCULTO))
        assertEquals(Acao.ABRIR_FLUTUANDO, regra.decidir(Modo.FLUTUANDO, false, Onde.FECHADO))
    }

    @Test
    fun jaAbertoSoGanhaFocoOndeEsta() {
        assertEquals(Acao.FOCAR_NO_CHAT, regra.decidir(Modo.FLUTUANDO, false, Onde.NO_CHAT))
        assertEquals(Acao.FOCAR_FLUTUANDO, regra.decidir(Modo.NO_CHAT, false, Onde.FLUTUANDO))
    }

    @Test
    fun comEditorDeImagemAbertoSempreFlutua() {
        assertEquals(Acao.TIRAR_DO_CHAT_E_FLUTUAR, regra.decidir(Modo.NO_CHAT, true, Onde.NO_CHAT))
        assertEquals(Acao.ABRIR_FLUTUANDO, regra.decidir(Modo.NO_CHAT, true, Onde.FECHADO))
        assertEquals(Acao.ABRIR_FLUTUANDO, regra.decidir(Modo.NO_CHAT, true, Onde.OCULTO))
        assertEquals(Acao.FOCAR_FLUTUANDO, regra.decidir(Modo.NO_CHAT, true, Onde.FLUTUANDO))
    }

    @Test
    fun abrirJuntoComOChatSoParaQuemEstaNoChatEAindaNaoAbriu() {
        assertEquals(true, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, true, false, Onde.FECHADO))
        assertEquals(true, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, true, false, Onde.OCULTO))
        assertEquals(false, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, false, false, Onde.FECHADO))
        assertEquals(false, regra.deveAbrirJuntoComOChat(Modo.FLUTUANDO, true, false, Onde.FECHADO))
        assertEquals(false, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, true, false, Onde.FLUTUANDO))
        assertEquals(false, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, true, false, Onde.NO_CHAT))
        assertEquals(false, regra.deveAbrirJuntoComOChat(Modo.NO_CHAT, true, true, Onde.FECHADO))
    }

    @Test
    fun chaveDesconhecidaViraFlutuando() {
        assertEquals(Modo.NO_CHAT, Modo.daChave("chat"))
        assertEquals(Modo.FLUTUANDO, Modo.daChave(null))
        assertEquals(Modo.FLUTUANDO, Modo.daChave("???"))
    }
}
