package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste {
    private val regra = DecisaoDoToqueLongoEmImagemDaPaginaEntreModalEArraste

    @Test
    fun modalQuandoDedoQuaseParado() {
        assertTrue(
            regra.deveAbrirModalEmVezDeSoltarArraste(
                xInicio = 100f,
                yInicio = 200f,
                xFim = 105f,
                yFim = 204f,
            ),
        )
    }

    @Test
    fun arrasteQuandoPassaDoLimiar() {
        assertFalse(
            regra.deveAbrirModalEmVezDeSoltarArraste(
                xInicio = 100f,
                yInicio = 200f,
                xFim = 100f,
                yFim = 240f,
            ),
        )
    }

    @Test
    fun exatamenteNoLimiarAindaEModal() {
        val limiar = 28f
        assertTrue(
            regra.deveAbrirModalEmVezDeSoltarArraste(
                xInicio = 0f,
                yInicio = 0f,
                xFim = limiar,
                yFim = 0f,
                limiarPx = limiar,
            ),
        )
    }
}
