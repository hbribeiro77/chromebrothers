package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoVinculoDoCampoPeloChatDaOpenRouter {
    private val argumentos = """
        {"campo":{"id":"prompt","nome":"q","rotulo":"Prompt","tag":"textarea","papel":""},
         "envio":{"id":"","nome":"","rotulo":"Executar","tag":"button","papel":"button"}}
    """.trimIndent()

    @Test
    fun leCaixaEBotaoEPedeConfirmacaoForaDoSiteLiberado() {
        val lido = VinculoDoCampoParaOChat.ler(argumentos) as ResultadoDoVinculoPedidoPeloChat.Pronto
        assertEquals("prompt", lido.campo?.id)
        assertEquals("TEXTAREA", lido.campo?.tag)
        assertEquals("BUTTON", lido.envio?.tag)
        val pedido = PedidoDeAcaoConfirmadaDoChat.VincularCampo(lido.campo, lido.envio)
        assertEquals("Vincular a caixa Prompt e o botão Executar?", AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido))
        assertFalse(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                pedido,
                "https://outro.test",
                listOf("sjinn.ai"),
            ),
        )
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                pedido,
                "https://www.sjinn.ai/app",
                listOf("sjinn.ai"),
            ),
        )
        assertEquals(
            "",
            LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(
                pedido,
                EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL,
            ),
        )
    }

    @Test
    fun aListaEAVInculoEntramNaInstrucaoSemAPalavraProibida() {
        val itens = CamposVinculaveisDaPaginaParaOChat.ler(
            "campo\tprompt\tq\tPrompt\tTEXTAREA\t\nenvio\t\t\tExecutar\tBUTTON\tbutton\ncampo\tjavascript:alert(1)\t\t\t\t",
        )
        assertEquals(2, itens.size)
        val texto = CamposVinculaveisDaPaginaParaOChat.descrever(itens)
        assertTrue(texto.contains("rotulo=Prompt"))
        assertTrue(texto.contains("rotulo=Executar"))
        val vinculo = DescricaoDoVinculoDoCampoParaOChat.escrever(
            VinculoDoCampoNoSite("sjinn.ai", itens[0].marca, itens[1].marca),
        )
        assertEquals("Vínculo deste site: caixa Prompt. botão Executar.", vinculo)
        val instrucao = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn", vinculo = vinculo)
        assertTrue(instrucao.contains("Vínculo deste site:"))
        assertTrue(instrucao.contains("listar_campos_da_pagina").not())
        assertTrue(instrucao.contains("leia os campos da página"))
        assertTrue(CamposVinculaveisDaPaginaParaOChat.script().contains("textarea"))
        assertFalse(AcoesConfirmadasDoChatDaOpenRouter.definicoes().joinToString().contains("navegar"))
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.definicoes().any { it.contains("vincular_campo_do_site") })
        val vazio = VinculoDoCampoParaOChat.ler("""{"titulo":"oi"}""")
        assertEquals("Faltou a caixa e o botão.", (vazio as ResultadoDoVinculoPedidoPeloChat.Recusado).mensagem)
    }
}
