package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TesteDasCandidatasDoElementoParaORoteiroDeAutomacao {
    @Test
    fun priorizaIdNameAriaTextoENth() {
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(
            id = "q",
            name = "q",
            ariaLabel = "Pesquisar",
            placeholder = "Pesquisar no Google",
            texto = "Pesquisar",
            tag = "input",
            indiceNaTag = 0,
        )
        val lista = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        assertTrue(lista.size >= 4)
        assertTrue(lista.any { it.seletor == "#q" })
        assertTrue(lista.any { it.seletor == """[name="q"]""" })
        assertTrue(lista.any { it.texto == "Pesquisar" })
        assertTrue(lista.any { it.seletor.contains("nth-of-type") || it.seletor.contains("input") })
    }

    @Test
    fun semIdUsaNameETexto() {
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(
            id = "",
            name = "btnK",
            ariaLabel = "",
            placeholder = "",
            texto = "Google Search",
            tag = "input",
            indiceNaTag = 2,
        )
        val lista = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        assertEquals("""[name="btnK"]""", lista.first().seletor)
        assertTrue(lista.any { it.texto == "Google Search" })
    }

    @Test
    fun priorizaDataTestIdEContextoDeForm() {
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(
            id = "email",
            name = "email",
            tag = "input",
            type = "email",
            testId = "login-email",
            formId = "login",
            classes = "flex mt-2 campo-email",
        )
        val lista = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        assertEquals("""[data-testid="login-email"]""", lista.first().seletor)
        assertTrue(lista.any { it.seletor == """form#login #email""" })
        assertTrue(lista.any { it.seletor == "input.campo-email" })
    }

    @Test
    fun incluiCandidatasDoPaiClicavel() {
        val pai = MarcaDoElementoParaORoteiroDeAutomacao(
            tag = "button",
            type = "submit",
            texto = "Entrar",
            formId = "login",
        )
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(
            tag = "span",
            texto = "Entrar",
            pai = pai,
        )
        val lista = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        assertTrue(lista.any { it.rotulo.startsWith("pai ·") })
        assertTrue(lista.any { it.seletor.contains("submit") })
    }

    @Test
    fun parseiaMarcaRicaComPai() {
        val json = """
            {"id":"x","name":"n","aria":"a","placeholder":"p","texto":"T","tag":"span","indice":1,
             "type":"","role":"","href":"","testid":"tid","dataqa":"qa","classes":"meu-botao",
             "formId":"f","inNav":"false","inHeader":"false","label":"",
             "pai":{"id":"","name":"","aria":"Entrar","placeholder":"","texto":"Entrar","tag":"button",
                    "indice":0,"type":"submit","role":"","href":"","testid":"","dataqa":"","classes":"",
                    "formId":"f","inNav":"false","inHeader":"false","label":""}}
        """.trimIndent()
        val marca = CandidatasDoElementoParaORoteiroDeAutomacao.lerMarca(json)
        assertNotNull(marca)
        assertEquals("tid", marca.testId)
        assertEquals("qa", marca.dataQa)
        assertEquals("f", marca.formId)
        assertNotNull(marca.pai)
        assertEquals("button", marca.pai?.tag)
        assertEquals("submit", marca.pai?.type)
    }

    @Test
    fun scriptDePreviewDestacaSeletor() {
        val script = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.destacarCandidata("#q", "")
        assertTrue(script.contains("#q"))
        assertTrue(script.contains("outline"))
    }

    @Test
    fun scriptDeContagemIncluiPares() {
        val script = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.contarCandidatas(
            listOf("#q" to "", "" to "Entrar"),
        )
        assertTrue(script.contains("#q"))
        assertTrue(script.contains("Entrar"))
        assertEquals(listOf(1, 2, 0), ScriptsDoPickDeElementoParaOGravadorDeRoteiro.lerContagens("[1,2,0]"))
    }

    @Test
    fun rotuloComMatches() {
        val c = CandidataDoElementoParaORoteiro("id #q", "#q", "").comMatches(1)
        assertTrue(c.rotulo.contains("única"))
        assertEquals(1, c.matches)
    }

    @Test
    fun scriptDeNavegarAlvoIncluiDirecoes() {
        val cima = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.navegarAlvo("up")
        assertTrue(cima.contains("__cbGravadorAlvo"))
        assertTrue(cima.contains("'up'"))
        val pai = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.navegarAlvo("pai")
        assertTrue(pai.contains("'pai'"))
        assertTrue(pai.contains("parentElement"))
    }

    @Test
    fun pickSobeAteContentEditableEProseMirror() {
        val script = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.marcaNoPonto(10.0, 20.0, 800, 600)
        assertTrue(script.contains("__cbEditavel"))
        assertTrue(script.contains("ProseMirror"))
        assertTrue(script.contains("__cbCssDoToque"))
        assertTrue(script.contains("role=\"textbox\""))
        val fallback = ScriptsDoPickDeElementoParaOGravadorDeRoteiro.marcaDoMelhorEditorVisivel()
        assertTrue(fallback.contains("marcaDoMelhorEditorVisivel") || fallback.contains("ProseMirror"))
        assertTrue(fallback.contains("__cbEmpacota"))
    }

    @Test
    fun priorizaClasseProseMirrorNasCandidatas() {
        val marca = MarcaDoElementoParaORoteiroDeAutomacao(
            tag = "div",
            role = "textbox",
            ariaLabel = "Pergunte ao ChatGPT",
            classes = "flex ProseMirror mt-2",
        )
        val lista = CandidatasDoElementoParaORoteiroDeAutomacao.ranquear(marca)
        assertTrue(lista.any { it.seletor == """[aria-label="Pergunte ao ChatGPT"]""" })
        assertTrue(lista.any { it.seletor == "div.ProseMirror" })
        assertTrue(lista.any { it.seletor == """[role="textbox"]""" })
    }
}
