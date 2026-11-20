package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDecisaoDeGravarRascunhoDaEdicaoDeImagem {
    @Test
    fun soGravaQuandoHouveUmPassoNestaAbertura() {
        assertFalse(DecisaoDeGravarRascunhoDaEdicaoDeImagem.deveGravar(0))
        assertTrue(DecisaoDeGravarRascunhoDaEdicaoDeImagem.deveGravar(1))
    }
}
