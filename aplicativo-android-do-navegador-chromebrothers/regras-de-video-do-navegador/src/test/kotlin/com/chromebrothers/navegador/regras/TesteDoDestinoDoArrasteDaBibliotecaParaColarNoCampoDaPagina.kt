package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoDestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina {
    private val regra = DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina

    @Test
    fun verdeQuandoSobreGeradorMcpMesmoSemEditavel() {
        assertTrue(regra.corIndicaPodeSoltar(sobreGeradorMcp = true, editavelProximoNaPagina = false))
    }

    @Test
    fun verdeQuandoHaEditavelProximoNaPagina() {
        assertTrue(regra.corIndicaPodeSoltar(sobreGeradorMcp = false, editavelProximoNaPagina = true))
    }

    @Test
    fun amareloQuandoNemMcpNemEditavel() {
        assertFalse(regra.corIndicaPodeSoltar(sobreGeradorMcp = false, editavelProximoNaPagina = false))
    }

    @Test
    fun colarNaPaginaSoSeVerdeMesmoComPontoNaPagina() {
        assertFalse(
            DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina.deveColarNaPaginaAoSoltar(
                editavelProximoNaPagina = false,
                pontoSobrePagina = true,
            ),
        )
        assertTrue(
            DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina.deveColarNaPaginaAoSoltar(
                editavelProximoNaPagina = true,
                pontoSobrePagina = false,
            ),
        )
        assertTrue(
            DestinoDoArrasteDaBibliotecaParaColarNoCampoDaPagina.deveColarNaPaginaAoSoltar(
                editavelProximoNaPagina = true,
                pontoSobrePagina = true,
            ),
        )
    }

    @Test
    fun reconheceResultadoOkComOuSemAspas() {
        assertTrue(regra.ehResultadoOk("ok"))
        assertTrue(regra.ehResultadoOk("\"ok\""))
        assertTrue(regra.ehResultadoOk(" OK "))
        assertFalse(regra.ehResultadoOk("sem-alvo"))
        assertFalse(regra.ehResultadoOk("\"sem-alvo\""))
        assertFalse(regra.ehResultadoOk("null"))
        assertFalse(regra.ehResultadoOk(""))
    }

    @Test
    fun scriptDeSondaConverteViewParaCssENaoFoca() {
        val script = regra.scriptParaSondar(120.5, 340.25, 1080.0, 1920.0, 220.0)
        assertTrue(script.contains("elementFromPoint"))
        assertTrue(script.contains("contenteditable"))
        assertTrue(script.contains("innerWidth"))
        assertTrue(script.contains("120.5"))
        assertTrue(script.contains("340.25"))
        assertTrue(script.contains("1080"))
        assertTrue(script.contains("prompt-textarea"))
        assertTrue(script.contains(regra.OK))
        assertTrue(script.contains(regra.SEM_ALVO))
        assertFalse(script.contains(".focus("))
    }

    @Test
    fun scriptDeFocoIncluiFocusNoAlvo() {
        val script = regra.scriptParaFocar(10.0, 20.0, 800.0, 600.0, 100.0)
        assertTrue(script.contains("elementFromPoint"))
        assertTrue(script.contains(".focus("))
        assertTrue(script.contains(regra.OK))
    }

    @Test
    fun constantesDeResultadoEstaveis() {
        assertEquals("ok", regra.OK)
        assertEquals("sem-alvo", regra.SEM_ALVO)
    }
}
