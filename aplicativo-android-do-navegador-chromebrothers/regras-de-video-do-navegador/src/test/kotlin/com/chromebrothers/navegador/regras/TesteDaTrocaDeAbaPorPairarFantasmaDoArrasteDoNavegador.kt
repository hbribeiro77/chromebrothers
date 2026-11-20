package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaTrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador {
    private val regra = TrocaDeAbaPorPairarFantasmaDoArrasteDoNavegador

    @Test
    fun comecaAPairarQuandoDedoSobreOutraAba() {
        val novo = regra.atualizarEstado(
            idSobODedo = 2L,
            idDaAbaAtiva = 1L,
            estado = EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(),
            agoraMs = 1000L,
        )
        assertEquals(2L, novo.idDaAba)
        assertEquals(1000L, novo.desdeMs)
    }

    @Test
    fun zeraQuandoSobreAbaAtivaOuFora() {
        val pairando = EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(idDaAba = 2L, desdeMs = 500L)
        assertNull(
            regra.atualizarEstado(2L, idDaAbaAtiva = 2L, estado = pairando, agoraMs = 800L).idDaAba,
        )
        assertNull(
            regra.atualizarEstado(null, idDaAbaAtiva = 1L, estado = pairando, agoraMs = 800L).idDaAba,
        )
    }

    @Test
    fun mantemRelogioSeContinuaNaMesmaAba() {
        val pairando = EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(idDaAba = 2L, desdeMs = 500L)
        val novo = regra.atualizarEstado(2L, idDaAbaAtiva = 1L, estado = pairando, agoraMs = 900L)
        assertEquals(500L, novo.desdeMs)
    }

    @Test
    fun trocaSoAposAtraso() {
        val estado = EstadoDoPairarAbaNoArrasteDoFantasmaDoNavegador(idDaAba = 2L, desdeMs = 1000L)
        assertFalse(regra.deveTrocarAgora(estado, idDaAbaAtiva = 1L, agoraMs = 1699L))
        assertTrue(regra.deveTrocarAgora(estado, idDaAbaAtiva = 1L, agoraMs = 1700L))
    }
}
