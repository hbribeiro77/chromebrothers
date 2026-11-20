package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoRoteiroDeAutomacaoDaPaginaDoNavegador {
    @Test
    fun parseiaRoteiroMinimoComPassos() {
        val json = """
            {
              "nome": "Login demo",
              "passos": [
                {"acao":"abrir","url":"https://exemplo.test/login"},
                {"acao":"aguardar","texto":"Entrar","timeout_ms":5000},
                {"acao":"clicar","texto":"Entrar"},
                {"acao":"preencher","seletor":"#email","valor":"a@b.com"},
                {"acao":"esperar_ms","ms":500},
                {"acao":"rolar","pixels":400}
              ]
            }
        """.trimIndent()
        val roteiro = when (val r = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(json)) {
            is ResultadoDoRoteiroDeAutomacao.Ok -> r.roteiro
            is ResultadoDoRoteiroDeAutomacao.Erro -> error(r.mensagem)
        }
        assertEquals("Login demo", roteiro.nome)
        assertEquals(6, roteiro.passos.size)
        assertTrue(roteiro.passos[0] is PassoDoRoteiroDeAutomacao.Abrir)
        assertTrue(roteiro.passos[1] is PassoDoRoteiroDeAutomacao.Aguardar)
        assertTrue(roteiro.passos[2] is PassoDoRoteiroDeAutomacao.Clicar)
        assertTrue(roteiro.passos[3] is PassoDoRoteiroDeAutomacao.Preencher)
        assertTrue(roteiro.passos[4] is PassoDoRoteiroDeAutomacao.EsperarMs)
        assertTrue(roteiro.passos[5] is PassoDoRoteiroDeAutomacao.Rolar)
    }

    @Test
    fun rejeitaSemNomeOuSemPassos() {
        assertTrue(
            RoteiroDeAutomacaoDaPaginaDoNavegador.ler("""{"nome":"","passos":[]}""")
                is ResultadoDoRoteiroDeAutomacao.Erro,
        )
        assertTrue(
            RoteiroDeAutomacaoDaPaginaDoNavegador.ler("""{"nome":"x","passos":[]}""")
                is ResultadoDoRoteiroDeAutomacao.Erro,
        )
    }

    @Test
    fun serializaDeVoltaParaJsonEstavel() {
        val original = """{"nome":"A","id":"abc","passos":[{"acao":"abrir","url":"https://a.test"}]}"""
        val ok = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(original) as ResultadoDoRoteiroDeAutomacao.Ok
        val escrito = RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(ok.roteiro)
        val deNovo = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(escrito) as ResultadoDoRoteiroDeAutomacao.Ok
        assertEquals(ok.roteiro.nome, deNovo.roteiro.nome)
        assertEquals(ok.roteiro.id, deNovo.roteiro.id)
        assertEquals(1, deNovo.roteiro.passos.size)
    }

    @Test
    fun scriptDeAguardarPorTexto() {
        val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.aguardar(
            seletor = "",
            texto = "Entrar",
        )
        assertTrue(script.contains("Entrar"))
        assertTrue(script.contains("ok") || script.contains("\"ok\""))
    }

    @Test
    fun parseiaClicarSeComSeNao() {
        val json = """
            {"nome":"Busca","passos":[
              {"acao":"clicar_se","seletor":"#rso a","indice":0,"contem":"Wikipedia","se_nao":"seguir"}
            ]}
        """.trimIndent()
        val ok = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(json) as ResultadoDoRoteiroDeAutomacao.Ok
        val passo = ok.roteiro.passos.single() as PassoDoRoteiroDeAutomacao.ClicarSe
        assertEquals("#rso a", passo.seletor)
        assertEquals(0, passo.indice)
        assertEquals("Wikipedia", passo.contem)
        assertEquals("seguir", passo.seNao)
    }

    @Test
    fun scriptDeClicarSeIncluiIndiceEContem() {
        val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.clicarSe(
            seletor = "#rso a",
            indice = 0,
            contem = "Wikipedia",
        )
        assertTrue(script.contains("#rso a"))
        assertTrue(script.contains("Wikipedia"))
        assertTrue(script.contains("nao_bateu"))
    }

    @Test
    fun scriptDeClicarPontuaCandidatosEReportaMatches() {
        val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.clicar(
            seletor = "",
            texto = "Unlimited",
        )
        assertTrue(script.contains("Unlimited"))
        assertTrue(script.contains("pontuar"))
        assertTrue(script.contains("matches"))
        assertTrue(script.contains("role=\"radio\""))
    }

    @Test
    fun rotuloDoPassoClicarUsaTexto() {
        val rotulo = RoteiroDeAutomacaoDaPaginaDoNavegador.rotuloDoPasso(
            PassoDoRoteiroDeAutomacao.Clicar(texto = "Unlimited mode"),
        )
        assertEquals("clicar Unlimited mode", rotulo)
    }

    @Test
    fun parseiaAguardarQualquerMarcarEIrPara() {
        val json = """
            {"nome":"Topview ramo","passos":[
              {"acao":"aguardar_qualquer","timeout_ms":12000,"opcoes":[
                {"id":"livre","texto":"Generate"},
                {"id":"creditos","texto":"credits"}
              ]},
              {"acao":"ir_para","destino":"fluxo_livre","se":"{{_ramo}}=livre"},
              {"acao":"ir_para","destino":"fluxo_creditos"},
              {"acao":"marcar","id":"fluxo_livre"},
              {"acao":"marcar","id":"fluxo_creditos"}
            ]}
        """.trimIndent()
        val ok = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(json) as ResultadoDoRoteiroDeAutomacao.Ok
        val aq = ok.roteiro.passos[0] as PassoDoRoteiroDeAutomacao.AguardarQualquer
        assertEquals("_ramo", aq.variavel)
        assertEquals(2, aq.opcoes.size)
        assertEquals("livre", aq.opcoes[0].id)
        val ir = ok.roteiro.passos[1] as PassoDoRoteiroDeAutomacao.IrPara
        assertEquals("fluxo_livre", ir.destino)
        assertEquals("{{_ramo}}=livre", ir.se)
        val escrito = RoteiroDeAutomacaoDaPaginaDoNavegador.escrever(ok.roteiro)
        val deNovo = RoteiroDeAutomacaoDaPaginaDoNavegador.ler(escrito) as ResultadoDoRoteiroDeAutomacao.Ok
        assertEquals(5, deNovo.roteiro.passos.size)
    }

    @Test
    fun condicaoIrParaComparaAposVariaveis() {
        assertTrue(
            RoteiroDeAutomacaoDaPaginaDoNavegador.condicaoIrParaBate(
                "{{_ramo}}=livre",
                mapOf("_ramo" to "livre"),
            ),
        )
        assertTrue(
            !RoteiroDeAutomacaoDaPaginaDoNavegador.condicaoIrParaBate(
                "{{_ramo}}=livre",
                mapOf("_ramo" to "creditos"),
            ),
        )
        assertTrue(
            RoteiroDeAutomacaoDaPaginaDoNavegador.condicaoIrParaBate("", emptyMap()),
        )
    }

    @Test
    fun indiceDoDestinoPorMarcarEPasso() {
        val passos = listOf(
            PassoDoRoteiroDeAutomacao.Abrir("https://a.test"),
            PassoDoRoteiroDeAutomacao.Marcar("fluxo_a"),
            PassoDoRoteiroDeAutomacao.Clicar(texto = "x"),
        )
        assertEquals(
            1,
            RoteiroDeAutomacaoDaPaginaDoNavegador.indiceDoDestino(passos, 0, "fluxo_a"),
        )
        assertEquals(
            2,
            RoteiroDeAutomacaoDaPaginaDoNavegador.indiceDoDestino(passos, 3, ""),
        )
    }

    @Test
    fun scriptAguardarQualquerIncluiIds() {
        val script = ScriptsDosPassosDoRoteiroDeAutomacaoDaPagina.aguardarQualquer(
            listOf(
                OpcaoDoAguardarQualquer("livre", texto = "Generate"),
                OpcaoDoAguardarQualquer("creditos", texto = "credits"),
            ),
        )
        assertTrue(script.contains("livre"))
        assertTrue(script.contains("Generate"))
        assertTrue(script.contains("creditos"))
    }
}
