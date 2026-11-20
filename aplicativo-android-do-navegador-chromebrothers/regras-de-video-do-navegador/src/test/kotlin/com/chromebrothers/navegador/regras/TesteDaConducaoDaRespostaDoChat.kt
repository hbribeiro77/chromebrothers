package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaConducaoDaRespostaDoChat {
    @Test
    fun orientacaoJuntaAsMensagensComOAviso() {
        val mensagem = OrientacaoDuranteARespostaDoChat.mensagem(listOf(" usa fundo escuro ", "", "e 3 colunas"))!!
        assertEquals("user", mensagem.papel)
        assertTrue(mensagem.conteudo.startsWith(OrientacaoDuranteARespostaDoChat.PREFIXO))
        assertTrue(mensagem.conteudo.endsWith("usa fundo escuro\n\ne 3 colunas"))
        assertNull(OrientacaoDuranteARespostaDoChat.mensagem(listOf(" ", "")))
    }

    @Test
    fun fechaAsChamadasSemResultado() {
        val historico = listOf(
            MensagemDeChat("user", "faz"),
            MensagemDeChat(
                "assistant",
                "",
                chamadas = listOf(
                    ChamadaDeFerramenta("a", "ler_texto_visivel", "{}"),
                    ChamadaDeFerramenta("b", "capturar_aba", "{}"),
                ),
            ),
            MensagemDeChat(papel = "tool", conteudo = "texto", idDaChamada = "a"),
        )
        val fechado = FechamentoDasChamadasPendentesDoChat.fechar(historico)
        assertEquals(4, fechado.size)
        assertEquals("b", fechado.last().idDaChamada)
        assertEquals(FechamentoDasChamadasPendentesDoChat.RESULTADO, fechado.last().conteudo)
        assertEquals(fechado, FechamentoDasChamadasPendentesDoChat.fechar(fechado))
    }

    @Test
    fun historicoSemChamadasFicaIgual() {
        val historico = listOf(MensagemDeChat("user", "oi"), MensagemDeChat("assistant", "olá"))
        assertEquals(historico, FechamentoDasChamadasPendentesDoChat.fechar(historico))
    }
}
