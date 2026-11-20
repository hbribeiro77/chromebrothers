package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaEscolhaDeCandidataPelaIaDoGravadorDeRoteiro {
    private val candidatas = listOf(
        CandidataDoElementoParaORoteiro("id #entrar", "#entrar", "", matches = 1),
        CandidataDoElementoParaORoteiro("texto «OK»", "", "OK", matches = 3),
    )

    @Test
    fun interpretaIndice() {
        val r = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.interpretar(
            """{"indice":0}""",
            candidatas,
        )
        assertTrue(r is ResultadoDaEscolhaDeCandidataPelaIa.Candidata)
        assertEquals("#entrar", (r as ResultadoDaEscolhaDeCandidataPelaIa.Candidata).candidata.seletor)
    }

    @Test
    fun interpretaSeletorComCercaMarkdown() {
        val r = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.interpretar(
            """```json
            {"seletor":"button.login","texto":"Entrar"}
            ```""",
            candidatas,
        )
        assertTrue(r is ResultadoDaEscolhaDeCandidataPelaIa.Candidata)
        val c = (r as ResultadoDaEscolhaDeCandidataPelaIa.Candidata).candidata
        assertEquals("button.login", c.seletor)
        assertEquals("Entrar", c.texto)
        assertTrue(c.rotulo.startsWith("IA ·"))
    }

    @Test
    fun rejeitaIndiceFora() {
        val r = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.interpretar(
            """{"indice":9}""",
            candidatas,
        )
        assertTrue(r is ResultadoDaEscolhaDeCandidataPelaIa.Erro)
    }

    @Test
    fun montaMensagensComIntencao() {
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(tag = "button", texto = "X")
        val msgs = EscolhaDeCandidataPelaIaDoGravadorDeRoteiro.mensagens(
            intencao = "botão entrar",
            url = "https://a.test",
            marca = marca,
            candidatas = candidatas,
            trechoDom = "<button id=entrar>Entrar</button>",
        )
        assertEquals(2, msgs.size)
        assertEquals("system", msgs[0].papel)
        assertTrue(msgs[1].conteudo.contains("botão entrar"))
        assertTrue(msgs[1].conteudo.contains("#entrar"))
    }
}
