package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat.AcaoAoSoltarABolha
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoGestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat {
    private val regra = GestoDaBolhaDoComponenteComAlvoDeFecharEFixacaoNoChat

    @Test
    fun alvoDeFecharPegaODedoDentroDoImaENaoForaDele() {
        assertTrue(regra.sobreOAlvoDeFechar(100f, 100f, 100f, 100f, 40f))
        assertTrue(regra.sobreOAlvoDeFechar(160f, 100f, 100f, 100f, 40f))
        assertFalse(regra.sobreOAlvoDeFechar(170f, 100f, 100f, 100f, 40f))
        assertFalse(regra.sobreOAlvoDeFechar(100f, 100f, 100f, 100f, 0f))
    }

    @Test
    fun toqueCurtoAbreEArrasteSimplesSoMove() {
        assertEquals(AcaoAoSoltarABolha.ABRIR, regra.acaoAoSoltar(false, false, false, false, false))
        assertEquals(AcaoAoSoltarABolha.SO_MOVER, regra.acaoAoSoltar(true, false, false, false, false))
    }

    @Test
    fun toqueLongoSobreOAlvoFechaESemMoverNaoFazNada() {
        assertEquals(AcaoAoSoltarABolha.FECHAR, regra.acaoAoSoltar(true, true, false, true, false))
        assertEquals(AcaoAoSoltarABolha.NADA, regra.acaoAoSoltar(false, true, false, false, false))
    }

    @Test
    fun faixaArmadaFixaEDepoisDeFixarOSoltarEIgnorado() {
        assertEquals(AcaoAoSoltarABolha.FIXAR_NO_CHAT, regra.acaoAoSoltar(true, false, false, false, true))
        assertEquals(AcaoAoSoltarABolha.NADA, regra.acaoAoSoltar(true, true, true, true, true))
    }
}
