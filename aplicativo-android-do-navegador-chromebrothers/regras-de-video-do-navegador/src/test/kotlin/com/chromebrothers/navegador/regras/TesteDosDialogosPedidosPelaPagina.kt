package com.chromebrothers.navegador.regras

import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDosDialogosPedidosPelaPagina {
    private val v = ValorDeDataEHoraDoCampoDaPagina

    @Test
    fun leCadaTipoDeCampo() {
        assertEquals(LocalDate.of(2026, 11, 24), v.data("2026-11-24"))
        assertEquals(LocalDate.of(2026, 11, 24), v.data("2026-11-24T10:30"))
        assertEquals(LocalDate.of(2026, 2, 1), v.data("2026-02"))
        assertEquals(LocalDate.of(2025, 12, 29), v.data("2026-W01"))
        assertEquals(LocalTime.of(10, 30), v.hora("2026-11-24T10:30"))
        assertEquals(LocalTime.of(7, 5), v.hora("07:05"))
        assertNull(v.data(""))
        assertNull(v.data(null))
        assertNull(v.data("2025-W53"))
        assertNull(v.hora("2026-11-24"))
    }

    @Test
    fun escreveCadaTipoDeCampo() {
        val dia = LocalDate.of(2027, 1, 1)
        assertEquals("2027-01-01", v.textoDaData(dia))
        assertEquals("2027-01", v.textoDoMes(dia))
        assertEquals("2026-W53", v.textoDaSemana(dia))
        assertEquals("2027-01-01T00:00", v.textoDaDataEHora(dia, LocalTime.MIDNIGHT))
        assertEquals("09:07", v.textoDaHora(LocalTime.of(9, 7)))
    }

    @Test
    fun ofereceParadaSoDoTerceiroDialogoEmDiante() {
        val limite = LimiteDeDialogosSeguidosDaPagina()
        repeat(2) {
            limite.registrar(it * 100L)
            limite.fechou(it * 100L + 50)
        }
        assertFalse(limite.deveOferecerParar())
        limite.registrar(200)
        assertTrue(limite.deveOferecerParar())
        limite.silenciar()
        assertTrue(limite.silenciado)
        limite.paginaNova()
        assertFalse(limite.silenciado)
        assertFalse(limite.deveOferecerParar())
    }

    @Test
    fun caixasEspacadasNaoContamComoSeguidas() {
        val limite = LimiteDeDialogosSeguidosDaPagina()
        repeat(10) {
            limite.registrar(it * 10_000L)
            limite.fechou(it * 10_000L + 5_000)
        }
        assertFalse(limite.deveOferecerParar())
    }
}
