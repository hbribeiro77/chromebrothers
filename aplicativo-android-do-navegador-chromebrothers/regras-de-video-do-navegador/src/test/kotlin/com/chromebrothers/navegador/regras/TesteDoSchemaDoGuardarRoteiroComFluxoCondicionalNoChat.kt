package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertTrue

class TesteDoSchemaDoGuardarRoteiroComFluxoCondicionalNoChat {
    @Test
    fun schemaDoGuardarRoteiroExpoeOpcoesDestinoSeEIdDoMarcar() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        val marca = "\"name\":\"guardar_roteiro_de_automacao\""
        val inicio = json.indexOf(marca)
        assertTrue(inicio >= 0, "guardar_roteiro ausente no json de tools")
        // A descrição é longa; o schema fica depois — janela ampla.
        val trecho = json.substring(inicio, (inicio + 12_000).coerceAtMost(json.length))
        assertTrue(trecho.contains("\"opcoes\""), "faltou opcoes no schema")
        assertTrue(trecho.contains("\"destino\""), "faltou destino no schema")
        assertTrue(trecho.contains("\"se\""), "faltou se no schema")
        assertTrue(trecho.contains("aguardar_qualquer"), "faltou aguardar_qualquer")
        assertTrue(trecho.contains("ir_para"), "faltou ir_para")
        // id do marcar precisa estar nas properties do passo (não só no id do roteiro).
        assertTrue(trecho.contains("marcar: id da ancora") || trecho.contains("\"id\":{\"type\":\"string\",\"description\":\"marcar"), "faltou id do marcar no passo")
        assertTrue(
            PlaybooksDoAgenteDoChatDoChromebrothers.texto().contains("aguardar_qualquer"),
        )
        assertTrue(
            ManualDoAgenteDoChat.roteiros().contains("{{_ramo}}=livre"),
        )
    }
}
