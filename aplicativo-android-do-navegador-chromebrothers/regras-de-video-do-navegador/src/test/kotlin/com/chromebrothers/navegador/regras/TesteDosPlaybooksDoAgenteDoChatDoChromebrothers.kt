package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertTrue

class TesteDosPlaybooksDoAgenteDoChatDoChromebrothers {
    @Test
    fun textoTemProcessoEDominio() {
        val texto = PlaybooksDoAgenteDoChatDoChromebrothers.texto()
        assertTrue(texto.contains("Playbooks"))
        assertTrue(texto.contains("Pedido vago"))
        assertTrue(texto.contains("Diagnosticar"))
        assertTrue(texto.contains("Verificar"))
        assertTrue(texto.contains("Explorar página"))
        assertTrue(texto.contains("Coleção"))
        assertTrue(texto.contains("Gerar mídia"))
        assertTrue(texto.contains("Roteiro"))
    }

    @Test
    fun instrucaoDoAgenteIncluiPlaybooks() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: teste")
        assertTrue(texto.contains(PlaybooksDoAgenteDoChatDoChromebrothers.MARCA))
        assertTrue(texto.contains("Pedido vago"))
        assertTrue(texto.contains("Explorar página"))
    }
}
