package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasAcoesConfirmadasDoChatDaOpenRouter {
    @Test
    fun oPedidoDoAgenteIncluiAsTresAcoesSemJavascript() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "deepseek/deepseek-v4.1-flash-1017",
            listOf(MensagemDeChat("user", "abre o site")),
            comFerramentas = true,
        )
        AcoesConfirmadasDoChatDaOpenRouter.nomes.forEach { nome ->
            assertTrue(corpo.contains(nome))
        }
        assertFalse(corpo.contains("navegar"))
        assertFalse(corpo.contains("javascript"))
        assertFalse(corpo.contains("cookie"))
        assertFalse(corpo.contains("senha"))
    }

    @Test
    fun abrirExigeEnderecoHttpECriarAceitaAbaEmBranco() {
        val abrir = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO,
            """{"endereco":"example.com"}""",
        )
        val pedido = (abrir as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertEquals(PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("example.com"), pedido)
        assertEquals("Abrir na aba atual?\nexample.com", AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido))

        val semEndereco = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO,
            "{}",
        )
        assertEquals(
            "Faltou o endereço.",
            (semEndereco as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem,
        )

        val script = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ABRIR_ENDERECO,
            """{"endereco":"javascript://exemplo"}""",
        )
        assertEquals(
            "Só abro endereços http ou https.",
            (script as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem,
        )

        val emBranco = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.CRIAR_ABA,
            "{}",
        )
        val criar = (emBranco as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertEquals("Criar uma aba em branco?", AcoesConfirmadasDoChatDaOpenRouter.pergunta(criar))
    }

    @Test
    fun preencherUsaSoOTextoDoCampoVinculado() {
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.PREENCHER_CAMPO,
            """{"texto":"olá\nlinha"}""",
        )
        val pedido = (resultado as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.PreencherCampo
        assertEquals("olá\nlinha", pedido.texto)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido).startsWith("Preencher o campo da página?"))

        val vazio = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.PREENCHER_CAMPO,
            """{"texto":""}""",
        )
        assertEquals("Faltou o texto.", (vazio as ResultadoDaInterpretacaoDaAcaoDoChat.Recusada).mensagem)
    }

    @Test
    fun escreverNaPaginaUsaTextoEPergunta() {
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.ESCREVER_NA_PAGINA,
            """{"texto":"parágrafo no docs"}""",
        )
        val pedido = (resultado as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
            as PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina
        assertEquals("parágrafo no docs", pedido.texto)
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido).startsWith("Escrever isto no editor da página?"))
        assertTrue(AcoesConfirmadasDoChatDaOpenRouter.ESCREVER_NA_PAGINA in AcoesConfirmadasDoChatDaOpenRouter.nomes)
    }
}
