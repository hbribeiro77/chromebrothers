package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.MensagemDeFalhaDeRedeNaGeracaoMcp as Msg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaMensagemDeFalhaDeRedeNaGeracaoMcp {

    @Test
    fun hostSemTarefaPedeTentarDeNovo() {
        val m = Msg.amigavel("Unable to resolve host \"api.topview.ai\": No address associated with hostname", temTarefa = false)
        assertTrue(m.startsWith(Msg.SEM_TAREFA))
        assertTrue(m.contains("api.topview.ai"))
    }

    @Test
    fun timeoutComTarefaPedeVerificar() {
        assertTrue(Msg.amigavel("timeout", temTarefa = true).startsWith(Msg.COM_TAREFA))
    }

    @Test
    fun erroDoServicoFicaComoVeio() {
        assertEquals("Credit not enough", Msg.amigavel("Credit not enough", temTarefa = true))
    }

    @Test
    fun naoTraduzDuasVezes() {
        val uma = Msg.amigavel("Failed to connect to x", temTarefa = false)
        assertEquals(uma, Msg.amigavel(uma, temTarefa = false))
    }
}
