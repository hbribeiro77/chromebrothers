package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaCurvaDePressaoDaCaneta {
    private fun p(x: Float, y: Float) = PontoDaCurvaDePressao(x, y)

    @Test
    fun linearDevolveAMesmaPressao() {
        listOf(0f, 0.2f, 0.5f, 0.9f, 1f).forEach { assertEquals(it, CurvaDePressaoDaCaneta.LINEAR.aplicar(it), 0.001f) }
    }

    @Test
    fun passaPelosPontosENuncaAfinaAoApertarMais() {
        val curva = CurvaDePressaoDaCaneta(listOf(p(0f, 0f), p(0.3f, 0.6f), p(0.6f, 0.65f), p(1f, 1f)))
        assertEquals(0.6f, curva.aplicar(0.3f), 0.001f)
        assertEquals(0.65f, curva.aplicar(0.6f), 0.001f)
        var anterior = -1f
        for (i in 0..100) {
            val y = curva.aplicar(i / 100f)
            assertTrue(y >= anterior - 1e-4f, "caiu em ${i / 100f}")
            anterior = y
        }
    }

    @Test
    fun levePoucaForcaJaEngrossaEFirmePrecisaApertar() {
        assertTrue(CurvaDePressaoDaCaneta.LEVE.aplicar(0.3f) > 0.45f)
        assertTrue(CurvaDePressaoDaCaneta.FIRME.aplicar(0.3f) < 0.15f)
    }

    @Test
    fun textoVaiEVolta() {
        val curva = CurvaDePressaoDaCaneta(listOf(p(0f, 0.1f), p(0.4f, 0.7f), p(1f, 0.9f)))
        assertEquals(curva, CurvaDePressaoDaCaneta.deTexto(curva.paraTexto()))
        assertNull(CurvaDePressaoDaCaneta.deTexto("lixo"))
    }

    @Test
    fun pontasFicamNasBordasEPontoNovoFicaSobreACurva() {
        val movida = CurvaDePressaoDaCaneta.LINEAR.comPontoMovido(0, 0.5f, 0.2f)
        assertEquals(p(0f, 0.2f), movida.pontos.first())
        val (comNovo, indice) = assertNotNull(CurvaDePressaoDaCaneta.LINEAR.comPontoNovo(0.5f))
        assertEquals(3, comNovo.pontos.size)
        assertEquals(0.5f, comNovo.pontos[indice].y, 0.001f)
        assertEquals(2, comNovo.semPonto(indice).pontos.size)
        // Pontas não saem.
        assertEquals(3, comNovo.semPonto(0).pontos.size)
    }

    @Test
    fun ajusteAntigoViraCurva() {
        assertEquals(CurvaDePressaoDaCaneta.LINEAR, CurvaDePressaoDaCaneta.doAjusteAntigo(0f))
        assertTrue(CurvaDePressaoDaCaneta.doAjusteAntigo(1f).aplicar(0.3f) > 0.3f)
    }
}
