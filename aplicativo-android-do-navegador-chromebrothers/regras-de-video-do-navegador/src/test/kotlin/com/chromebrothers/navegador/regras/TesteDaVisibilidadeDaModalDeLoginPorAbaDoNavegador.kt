package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaVisibilidadeDaModalDeLoginPorAbaDoNavegador {
    @Test
    fun soApareceNaAbaQueAbriu() {
        assertTrue(VisibilidadeDaModalDeLoginPorAbaDoNavegador.deveExibir(1L, 1L))
        assertFalse(VisibilidadeDaModalDeLoginPorAbaDoNavegador.deveExibir(2L, 1L))
        assertFalse(VisibilidadeDaModalDeLoginPorAbaDoNavegador.deveExibir(1L, null))
        assertFalse(VisibilidadeDaModalDeLoginPorAbaDoNavegador.deveExibir(null, 1L))
    }
}
