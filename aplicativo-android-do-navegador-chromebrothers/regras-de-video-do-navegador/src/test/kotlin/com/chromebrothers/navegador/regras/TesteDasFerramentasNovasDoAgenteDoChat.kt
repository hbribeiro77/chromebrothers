package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDasFerramentasNovasDoAgenteDoChat {
    @Test
    fun scriptsEscapamSeletorEValor() {
        val script = ElementosDaPaginaPeloSeletorParaOChat.scriptDePreencher("input[name=\"q\"]", "oi \"lá\"\n", 2)
        assertTrue(script.contains("(\"input[name=\\\"q\\\"]\", 2, \"oi \\\"lá\\\"\\n\")"))
        assertTrue(ElementosDaPaginaPeloSeletorParaOChat.scriptDeClicar(".btn", 0).endsWith("(\".btn\", 0)"))
        assertTrue(ElementosDaPaginaPeloSeletorParaOChat.scriptDeTecla("Enter", "", 0).contains("(\"\", 0, \"Enter\")"))
    }

    @Test
    fun relatosDosResultados() {
        val r = ElementosDaPaginaPeloSeletorParaOChat
        assertEquals("Feito em .a.", r.relato("ok:1", ".a"))
        assertTrue(r.relato("ok:3", ".a").contains("havia 3"))
        assertTrue(r.relato("sem-elemento", ".a").contains("consultar_elementos"))
        assertTrue(r.relato("sem-opcao:A | B", "select").contains("Opções: A | B"))
        assertEquals("Marquei #c.", r.relato("ok-marcado:true", "#c"))
        assertTrue(r.relato("indice-fora:2", ".a").contains("Só há 2"))
    }

    @Test
    fun leituraEmSegundoPlanoPagina() {
        val texto = "x".repeat(LeituraEmSegundoPlanoParaOChat.LIMITE_DO_BLOCO + 10)
        val primeiro = LeituraEmSegundoPlanoParaOChat.relato("T", "https://a.com", texto, 0, "")
        assertTrue(primeiro.contains("a aba do usuário não mudou"))
        assertTrue(primeiro.contains("offset=${LeituraEmSegundoPlanoParaOChat.LIMITE_DO_BLOCO}"))
        val segundo = LeituraEmSegundoPlanoParaOChat.relato("T", "https://a.com", texto, LeituraEmSegundoPlanoParaOChat.LIMITE_DO_BLOCO, "")
        assertTrue(segundo.endsWith("x".repeat(10)))
        assertTrue(LeituraEmSegundoPlanoParaOChat.relato("", "", "", 0, "tempo esgotado").contains("tempo esgotado"))
    }

    @Test
    fun edicaoPorTrechosTudoOuNada() {
        val html = "<h1>Oi</h1><p>a</p><p>a</p>"
        val ok = EdicaoPorTrechosDoHtml.aplicar(html, listOf(TrocaDeTrechoDoHtml("<h1>Oi</h1>", "<h1>Olá</h1>")))
        assertIs<ResultadoDaEdicaoPorTrechos.Ok>(ok)
        assertEquals("<h1>Olá</h1><p>a</p><p>a</p>", ok.html)
        val duplicado = EdicaoPorTrechosDoHtml.aplicar(html, listOf(TrocaDeTrechoDoHtml("<p>a</p>", "<p>b</p>")))
        assertIs<ResultadoDaEdicaoPorTrechos.Recusada>(duplicado)
        assertTrue(duplicado.mensagem.contains("2 vezes"))
        val ausente = EdicaoPorTrechosDoHtml.aplicar(
            html,
            listOf(TrocaDeTrechoDoHtml("<h1>Oi</h1>", "<h1>X</h1>"), TrocaDeTrechoDoHtml("nada", "y")),
        )
        assertIs<ResultadoDaEdicaoPorTrechos.Recusada>(ausente)
        assertTrue(ausente.mensagem.startsWith("Troca 2"))
    }

    @Test
    fun leAsTrocasDoJson() {
        val trocas = EdicaoPorTrechosDoHtml.ler("""{"trocas":[{"de":"<b>\"a\"</b>","para":"<i>b</i>"}]}""")
        assertEquals(listOf(TrocaDeTrechoDoHtml("<b>\"a\"</b>", "<i>b</i>")), trocas)
    }
}
