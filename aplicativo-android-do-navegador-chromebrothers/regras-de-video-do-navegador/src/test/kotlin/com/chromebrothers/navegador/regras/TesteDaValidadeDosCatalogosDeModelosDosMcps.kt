package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaValidadeDosCatalogosDeModelosDosMcps {
    private val v = ValidadeDosCatalogosDeModelosDosMcps
    private val hora = 60 * 60 * 1000L

    @Test
    fun valeSeisHoras() {
        val t0 = 1_000_000_000L
        assertTrue(v.vencido(0L, t0))
        assertFalse(v.vencido(t0, t0 + 5 * hora))
        assertFalse(v.vencido(t0, t0 + 6 * hora - 1))
        assertTrue(v.vencido(t0, t0 + 6 * hora))
    }

    @Test
    fun relogioParaTrasConsideraVencido() {
        assertTrue(v.vencido(10 * hora, 9 * hora))
    }

    @Test
    fun soGuardaSucessoComModelos() {
        assertTrue(v.valeGuardar("""{"ok":true,"itens":[{"id":"a","nome":"A"}]}"""))
        assertFalse(v.valeGuardar("""{"ok":true,"itens":[]}"""))
        assertFalse(v.valeGuardar("""{"ok":false,"erro":"x"}"""))
    }
}
