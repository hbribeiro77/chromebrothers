package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDaEscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers {
    @Test
    fun escolhePrimeiroSlotLivre() {
        assertEquals(
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA,
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(emptySet()),
        )
        assertEquals(
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_SECUNDARIA,
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(
                setOf(EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA),
            ),
        )
        assertEquals(
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_TERCIARIA,
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(
                setOf(
                    EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_PRIMARIA,
                    EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.SLOT_SECUNDARIA,
                ),
            ),
        )
    }

    @Test
    fun recusaQuandoOsTresEstaoOcupados() {
        assertNull(
            EscolhaDaActivityDaNovaJanelaDoNavegadorChromebrothers.proximoSlotLivre(
                setOf(0, 1, 2),
            ),
        )
    }
}
