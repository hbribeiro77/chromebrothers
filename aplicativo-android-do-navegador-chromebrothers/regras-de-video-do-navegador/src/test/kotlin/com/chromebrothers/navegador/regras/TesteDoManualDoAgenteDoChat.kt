package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoManualDoAgenteDoChat {
    @Test
    fun oManualDeRoteirosTrazAsAcoesEARamificacao() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"roteiros"}""")
        assertTrue(texto.contains("aguardar_qualquer"))
        assertTrue(texto.contains("ir_para"))
        assertTrue(texto.contains("{{_ramo}}"))
        assertTrue(texto.contains("clicar_se"))
        assertTrue(texto.contains("chromebrothersAoTerminarRoteiro"))
    }

    @Test
    fun oManualDeComponentesTrazAPonte() {
        val texto = ManualDoAgenteDoChat.texto("""{"topico":"componentes"}""")
        listOf(
            "chromebrothers.gerarPdf", "chromebrothers.enviarAoChat", "chromebrothers.urlDaAbaAtiva",
            "chromebrothers.abrirNaAba", "chromebrothers.rodarRoteiro", "chromebrothers.escolherPasta",
            "chromebrothers.listarPasta", "chromebrothers.guardarImagem", "chromebrothers.abrirVideoNoPlayer",
            "input type=\"file\"",
        ).forEach { assertTrue(texto.contains(it), it) }
    }

    @Test
    fun topicoDesconhecidoListaOsValidos() {
        assertTrue(ManualDoAgenteDoChat.texto("{}").contains("roteiros, componentes"))
    }

    @Test
    fun aPonteEOsRoteirosSaemDoPromptFixoMasOManualEstaNasFerramentas() {
        val fixa = FerramentasDeLeituraDoChatDaOpenRouter.instrucaoFixa()
        assertFalse(fixa.contains("chromebrothers.escolherPasta"))
        assertFalse(fixa.contains("aguardar_sumir"))
        assertTrue(fixa.contains("ler_manual"))
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.json().contains("\"name\":\"ler_manual\""))
        assertTrue(FerramentasDeLeituraDoChatDaOpenRouter.json(somenteLeitura = true).contains("\"name\":\"ler_manual\""))
        assertTrue(ModoConversaDoChat.podeUsar(ManualDoAgenteDoChat.NOME))
    }
}
