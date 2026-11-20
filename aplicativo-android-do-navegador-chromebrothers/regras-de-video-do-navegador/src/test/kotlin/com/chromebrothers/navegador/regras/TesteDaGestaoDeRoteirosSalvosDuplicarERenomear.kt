package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class TesteDaGestaoDeRoteirosSalvosDuplicarERenomear {
    private val original = RoteiroDeAutomacaoDaPagina(
        id = "topview",
        nome = "Topview",
        passos = listOf(PassoDoRoteiroDeAutomacao.Abrir("https://www.topview.ai/")),
    )

    @Test
    fun duplicarGeraNovoIdENomeComMesmosPassos() {
        val copia = GestaoDeRoteirosSalvosDeAutomacaoDaPagina.duplicar(
            original,
            idsExistentes = setOf("topview"),
        )
        assertEquals("Cópia de Topview", copia.nome)
        assertNotEquals("topview", copia.id)
        assertEquals(1, copia.passos.size)
        assertTrue(copia.passos[0] is PassoDoRoteiroDeAutomacao.Abrir)
    }

    @Test
    fun renomearMantemIdEPassos() {
        val novo = GestaoDeRoteirosSalvosDeAutomacaoDaPagina.renomear(original, "Topview Plus")
            ?: error("renomear deveria retornar roteiro")
        assertEquals("topview", novo.id)
        assertEquals("Topview Plus", novo.nome)
        assertEquals(original.passos, novo.passos)
    }

    @Test
    fun renomearRejeitaNomeVazio() {
        assertEquals(
            null,
            GestaoDeRoteirosSalvosDeAutomacaoDaPagina.renomear(original, "  "),
        )
    }
}
