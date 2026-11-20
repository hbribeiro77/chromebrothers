package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoCliqueNoTextoVisivelDaPaginaParaOChat {
    @Test
    fun oCliqueProcuraOTextoENaoExecutaCodigoLivre() {
        val script = CliqueNoTextoVisivelDaPaginaParaOChat.script("Tools")
        assertTrue(script.contains("\"Tools\""))
        assertTrue(script.contains(".click()"))
        assertFalse(script.contains("eval"))
        val escapado = CliqueNoTextoVisivelDaPaginaParaOChat.script("a\"b")
        assertTrue(escapado.contains("\\\"") || escapado.contains("\\u0022"))
        assertFalse(escapado.contains("\"a\"b\""))
    }

    @Test
    fun oPedidoDeCliqueEsperaConfirmacao() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "deepseek/deepseek-v4.1-flash-1017",
            listOf(MensagemDeChat("user", "abre o menu")),
            comFerramentas = true,
        )
        assertTrue(corpo.contains(AcoesConfirmadasDoChatDaOpenRouter.CLICAR))
        assertFalse(corpo.contains("javascript"))
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.CLICAR,
            """{"texto":"Tools"}""",
        )
        val pedido = (resultado as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertEquals("Clicar em Tools?", AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido))
        val vazio = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.CLICAR,
            """{"texto":" "}""",
        )
        assertEquals("Faltou o texto.", (vazio as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem)
    }
}
