package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RotacaoDaVistaDoEditorDeImagem as Rot
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaRotacaoDaVistaDoEditorDeImagem {

    @Test
    fun normalizaParaMenos180A180() {
        assertEquals(0f, Rot.normalizar(360f))
        assertEquals(-90f, Rot.normalizar(270f))
        assertEquals(180f, Rot.normalizar(-180f))
        assertEquals(10f, Rot.normalizar(730f))
    }

    @Test
    fun diferencaPassaPelaVoltaDe180SemPular() {
        assertEquals(20f, Rot.diferenca(-170f, 170f), 0.001f)
        assertEquals(-20f, Rot.diferenca(170f, -170f), 0.001f)
        assertEquals(5f, Rot.diferenca(35f, 30f), 0.001f)
    }

    @Test
    fun encaixaPertoDosAngulosRetos() {
        assertEquals(90f, Rot.comEncaixe(85f))
        assertEquals(0f, Rot.comEncaixe(-5.5f))
        assertEquals(180f, Rot.comEncaixe(-176f))
        assertEquals(-90f, Rot.comEncaixe(275f))
        assertEquals(30f, Rot.comEncaixe(30f))
        assertEquals(83f, Rot.comEncaixe(83f))
    }

    @Test
    fun sabeQuandoEstaEncaixado() {
        assertTrue(Rot.estaEncaixado(90f))
        assertTrue(Rot.estaEncaixado(-180f))
        assertFalse(Rot.estaEncaixado(45f))
    }

    @Test
    fun rotuloDoChipSoMostraAnguloQuandoGirado() {
        assertEquals("100%", Rot.rotuloDoChip(1f, 0f))
        assertEquals("67% · 35°", Rot.rotuloDoChip(0.67f, 35f))
        assertEquals("100% · 270°", Rot.rotuloDoChip(1f, -90f))
        assertEquals("100%", Rot.rotuloDoChip(1f, 360f))
    }
}
