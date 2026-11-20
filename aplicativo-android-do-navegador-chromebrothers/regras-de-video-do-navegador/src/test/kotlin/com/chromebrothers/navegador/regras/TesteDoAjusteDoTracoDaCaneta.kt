package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoAjusteDoTracoDaCaneta {
    private val a = AjusteDoTracoDaCaneta

    @Test
    fun cordaNaoAndaDentroDoRaioEPuxaForaDele() {
        assertEquals(null, a.puxarPelaCorda(0f, 0f, 5f, 0f, raio = 10f))
        val puxado = a.puxarPelaCorda(0f, 0f, 30f, 40f, raio = 10f)!!
        // Distância 50, raio 10: anda 40 na mesma direção e fica a 10 da caneta.
        assertEquals(24f, puxado.first, 0.001f)
        assertEquals(32f, puxado.second, 0.001f)
    }

    @Test
    fun raioCresceComAIntensidade() {
        assertEquals(0f, a.raioDaCordaEmDp(0f))
        assertEquals(32f, a.raioDaCordaEmDp(1f))
        assertTrue(a.raioDaCordaEmDp(0.5f) in 15f..17f)
    }

    @Test
    fun curvaDePressaoMantemOsExtremosEMudaOMeio() {
        assertEquals(0f, a.pressao(0f, 1f))
        assertEquals(1f, a.pressao(1f, -1f))
        assertEquals(0.5f, a.pressao(0.5f, 0f), 0.0001f)
        assertTrue(a.pressao(0.5f, 1f) > 0.6f) // mais leve
        assertTrue(a.pressao(0.5f, -1f) < 0.3f) // mais firme
    }
}
