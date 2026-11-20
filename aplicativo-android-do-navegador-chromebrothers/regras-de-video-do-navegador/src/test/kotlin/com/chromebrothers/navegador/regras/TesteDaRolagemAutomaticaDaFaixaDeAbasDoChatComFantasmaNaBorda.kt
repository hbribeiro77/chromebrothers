package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaRolagemAutomaticaDaFaixaDeAbasDoChatComFantasmaNaBorda {
    private val regra = RolagemAutomaticaDaFaixaDeAbasDoChatComFantasmaNaBorda

    @Test
    fun meioDaFaixaNaoRola() {
        assertEquals(0, regra.pixelsPorQuadro(500f, 1000f, 1f, true, true))
    }

    @Test
    fun bordaEsquerdaRolaParaAEsquerdaEDireitaParaADireita() {
        assertTrue(regra.pixelsPorQuadro(10f, 1000f, 1f, true, true) < 0)
        assertTrue(regra.pixelsPorQuadro(990f, 1000f, 1f, true, true) > 0)
    }

    @Test
    fun maisPertoDaPontaEhMaisRapido() {
        val perto = regra.pixelsPorQuadro(995f, 1000f, 2f, true, true)
        val longe = regra.pixelsPorQuadro(920f, 1000f, 2f, true, true)
        assertTrue(perto > longe)
        assertEquals(28, regra.pixelsPorQuadro(1000f, 1000f, 2f, true, true))
    }

    @Test
    fun naoRolaSeJaEstaNoFimOuForaDaFaixa() {
        assertEquals(0, regra.pixelsPorQuadro(10f, 1000f, 1f, false, true))
        assertEquals(0, regra.pixelsPorQuadro(990f, 1000f, 1f, true, false))
        assertEquals(0, regra.pixelsPorQuadro(-5f, 1000f, 1f, true, true))
        assertEquals(0, regra.pixelsPorQuadro(10f, 0f, 1f, true, true))
    }
}
