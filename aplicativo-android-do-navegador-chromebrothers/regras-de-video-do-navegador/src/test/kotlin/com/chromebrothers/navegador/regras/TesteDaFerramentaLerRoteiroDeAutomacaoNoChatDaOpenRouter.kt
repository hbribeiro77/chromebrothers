package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaFerramentaLerRoteiroDeAutomacaoNoChatDaOpenRouter {
    @Test
    fun jsonEPermitidasIncluemLerRoteiro() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(json.contains("ler_roteiro_de_automacao"))
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.LER_ROTEIRO_DE_AUTOMACAO in
                FerramentasDeLeituraDoChatDaOpenRouter.permitidas,
        )
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: x")
                .contains("ler_roteiro_de_automacao"),
        )
        assertEquals(
            "Lendo o roteiro de automação…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(
                FerramentasDeLeituraDoChatDaOpenRouter.LER_ROTEIRO_DE_AUTOMACAO,
            ),
        )
        assertEquals(
            "Leu o roteiro de automação",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloVisivel(
                FerramentasDeLeituraDoChatDaOpenRouter.LER_ROTEIRO_DE_AUTOMACAO,
            ),
        )
    }

    @Test
    fun chaveAceitaIdOuNomeERecusaVazio() {
        val sem = FerramentasDeLeituraDoChatDaOpenRouter.chaveDoRoteiro("{}")
        assertTrue(sem is ResultadoDaChaveDoRoteiroParaOChat.Recusado)

        val porId = FerramentasDeLeituraDoChatDaOpenRouter.chaveDoRoteiro("""{"id":"topview"}""")
        assertEquals(
            "topview",
            (porId as ResultadoDaChaveDoRoteiroParaOChat.Ok).chave,
        )

        val porNome = FerramentasDeLeituraDoChatDaOpenRouter.chaveDoRoteiro("""{"nome":"Topview"}""")
        assertEquals(
            "Topview",
            (porNome as ResultadoDaChaveDoRoteiroParaOChat.Ok).chave,
        )
    }

    @Test
    fun textoDoRoteiroDevolveJsonCompletoOuErro() {
        val roteiro = RoteiroDeAutomacaoDaPagina(
            id = "topview",
            nome = "Topview",
            passos = listOf(
                PassoDoRoteiroDeAutomacao.Abrir("https://www.topview.ai/"),
                PassoDoRoteiroDeAutomacao.Clicar(texto = "Credits"),
            ),
        )
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.textoDoRoteiroLido(
            "topview",
            listOf(roteiro),
        )
        assertTrue(texto.contains("\"id\":\"topview\""))
        assertTrue(texto.contains("\"acao\":\"abrir\""))
        assertTrue(texto.contains("Credits"))

        assertEquals(
            "Não achei o roteiro «sumiu».",
            FerramentasDeLeituraDoChatDaOpenRouter.textoDoRoteiroLido("sumiu", listOf(roteiro)),
        )
    }
}
