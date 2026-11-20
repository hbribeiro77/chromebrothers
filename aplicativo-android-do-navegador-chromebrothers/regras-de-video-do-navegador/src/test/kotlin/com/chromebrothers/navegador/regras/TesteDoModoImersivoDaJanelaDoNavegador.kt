package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoModoImersivoDaJanelaDoNavegador {
    private fun esconde(menu: Boolean, editor: Boolean, site: Boolean) =
        ModoImersivoDaJanelaDoNavegador.deveEsconderAsBarras(menu, editor, site)

    @Test
    fun `sem nenhum motivo as barras ficam`() {
        assertFalse(esconde(menu = false, editor = false, site = false))
    }

    @Test
    fun `qualquer motivo esconde as barras`() {
        assertTrue(esconde(menu = true, editor = false, site = false))
        assertTrue(esconde(menu = false, editor = true, site = false))
        assertTrue(esconde(menu = false, editor = false, site = true))
    }

    @Test
    fun `player sobreposto da pagina esconde as barras como o player nativo`() {
        assertTrue(ModoImersivoDaJanelaDoNavegador.deveEsconderAsBarras(false, false, false, playerDaPaginaAberto = true))
    }
}
