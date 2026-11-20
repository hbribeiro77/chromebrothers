package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDasVariaveisDoRoteiroDeAutomacaoDaPagina {
    @Test
    fun extraiNomesDePlaceholders() {
        assertEquals(
            listOf("cpf", "nome"),
            VariaveisDoRoteiroDeAutomacaoDaPagina.nomesEm("{{cpf}} e {{ nome }} e {{cpf}}"),
        )
    }

    @Test
    fun substituiValoresConhecidos() {
        val out = VariaveisDoRoteiroDeAutomacaoDaPagina.substituir(
            "oi {{nome}}, cpf {{cpf}}",
            mapOf("nome" to "Ana", "cpf" to "123"),
        )
        assertEquals("oi Ana, cpf 123", out)
    }

    @Test
    fun listaFaltando() {
        assertEquals(
            listOf("x"),
            VariaveisDoRoteiroDeAutomacaoDaPagina.faltando("a {{x}} b {{y}}", mapOf("y" to "1")),
        )
    }

    @Test
    fun nomesNosPassosIncluemPreencherEAbrir() {
        val passos = listOf(
            PassoDoRoteiroDeAutomacao.Abrir("https://x.test/{{slug}}"),
            PassoDoRoteiroDeAutomacao.Preencher("#q", "", "busca {{termo}}"),
            PassoDoRoteiroDeAutomacao.Extrair("#r", "", "resultado"),
        )
        assertEquals(
            listOf("slug", "termo"),
            VariaveisDoRoteiroDeAutomacaoDaPagina.nomesNosPassos(passos),
        )
    }

    @Test
    fun roundtripPassosNovos() {
        val json = """
            {"nome":"N","passos":[
              {"acao":"aguardar_sumir","seletor":"#loading","timeout_ms":3000},
              {"acao":"aguardar_url","contem":"/ok","timeout_ms":4000},
              {"acao":"assertar","texto":"Pronto","contem":"Pronto"},
              {"acao":"extrair","seletor":"#cod","variavel":"codigo"},
              {"acao":"preencher","seletor":"#v","valor":"{{codigo}}"}
            ]}
        """.trimIndent()
        val ok = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(json) as ResultadoDoRoteiroDeAutomacao.Ok
        assertEquals(5, ok.roteiro.passos.size)
        assertTrue(ok.roteiro.passos[0] is PassoDoRoteiroDeAutomacao.AguardarSumir)
        assertTrue(ok.roteiro.passos[1] is PassoDoRoteiroDeAutomacao.AguardarUrl)
        assertTrue(ok.roteiro.passos[2] is PassoDoRoteiroDeAutomacao.Assertar)
        assertTrue(ok.roteiro.passos[3] is PassoDoRoteiroDeAutomacao.Extrair)
        val escrito = RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(ok.roteiro)
        val deNovo = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(escrito) as ResultadoDoRoteiroDeAutomacao.Ok
        assertEquals(5, deNovo.roteiro.passos.size)
        assertEquals(
            "codigo",
            (deNovo.roteiro.passos[3] as PassoDoRoteiroDeAutomacao.Extrair).variavel,
        )
    }
}
