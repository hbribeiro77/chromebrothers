package com.chromebrothers.navegador.regras

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaPontaDoTracoEDaSetaNaComposicao {
    @Test
    fun oMarcadorELargoRetoETranslucidoEASetaUsaACaneta() {
        assertEquals(3.2f, GeometriaDaPontaDoTraco.multiplicadorDeLargura(PontaDoTracoDaComposicao.MARCADOR))
        assertEquals(1f, GeometriaDaPontaDoTraco.multiplicadorDeLargura(PontaDoTracoDaComposicao.CANETA))
        assertEquals(1f, GeometriaDaPontaDoTraco.multiplicadorDeLargura(PontaDoTracoDaComposicao.SETA))
        assertEquals(true, GeometriaDaPontaDoTraco.translucida(PontaDoTracoDaComposicao.MARCADOR))
        assertEquals(false, GeometriaDaPontaDoTraco.translucida(PontaDoTracoDaComposicao.CANETA))
        assertEquals(false, GeometriaDaPontaDoTraco.translucida(PontaDoTracoDaComposicao.SETA))
        assertEquals(true, GeometriaDaPontaDoTraco.reta(PontaDoTracoDaComposicao.MARCADOR))
        assertEquals(false, GeometriaDaPontaDoTraco.reta(PontaDoTracoDaComposicao.CANETA))
    }

    @Test
    fun aCabecaDaSetaFicaAtrasDaPontaESomeQuandoOTracoECurto() {
        val asas = SetaSobreAFolha.asas(0f, 0f, 100f, 0f, 10f)
        assertTrue(asas != null)
        val cabeca = asas!!
        assertTrue(cabeca.esquerdaX < 100f)
        assertTrue(cabeca.direitaX < 100f)
        assertEquals(cabeca.esquerdaX, cabeca.direitaX, 0.01f)
        assertEquals(0f, cabeca.esquerdaY + cabeca.direitaY, 0.01f)
        assertTrue(abs(cabeca.esquerdaY) > 1f)
        assertNull(SetaSobreAFolha.asas(0f, 0f, 20f, 0f, 10f))
    }

    @Test
    fun oRiscoComecaNoToqueETerminaNaSeta() {
        val flecha = SetaSobreAFolha.flechaDoArraste(100f, 50f, 160f, 50f, 10f)
        assertTrue(flecha != null)
        assertEquals(100f, flecha!!.caudaX, 0.01f)
        assertEquals(50f, flecha.caudaY, 0.01f)
        assertEquals(160f, flecha.pontaX, 0.01f)
        assertEquals(50f, flecha.pontaY, 0.01f)
        assertTrue(flecha.pontaX > flecha.caudaX)
        assertNull(SetaSobreAFolha.flechaDoArraste(100f, 50f, 110f, 50f, 10f))
    }

    @Test
    fun oNanquimAfinaNoToqueLeveEEngrossaNaPressao() {
        val nanquimLeve = PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.NANQUIM, 0f)
        val canetaLeve = PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.CANETA, 0f)
        val nanquimForte = PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.NANQUIM, 1f)
        val canetaForte = PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.CANETA, 1f)
        assertTrue(nanquimLeve < canetaLeve / 2f)
        assertTrue(nanquimForte > canetaForte)
        assertEquals(255, PressaoDaPontaDoTraco.alpha(PontaDoTracoDaComposicao.NANQUIM, 1f))
        assertEquals(false, GeometriaDaPontaDoTraco.reta(PontaDoTracoDaComposicao.NANQUIM))
    }

    @Test
    fun oLapisEMaisFinoEEscureceComAPressao() {
        assertTrue(GeometriaDaPontaDoTraco.multiplicadorDeLargura(PontaDoTracoDaComposicao.LAPIS) < 1f)
        assertEquals(false, GeometriaDaPontaDoTraco.reta(PontaDoTracoDaComposicao.LAPIS))
        val leve = PressaoDaPontaDoTraco.alpha(PontaDoTracoDaComposicao.LAPIS, 0f)
        val forte = PressaoDaPontaDoTraco.alpha(PontaDoTracoDaComposicao.LAPIS, 1f)
        assertTrue(leve < forte)
        assertTrue(forte < 255)
        assertTrue(
            PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.LAPIS, 1f) <
                PressaoDaPontaDoTraco.fatorDeLargura(PontaDoTracoDaComposicao.CANETA, 1f),
        )
    }
}
