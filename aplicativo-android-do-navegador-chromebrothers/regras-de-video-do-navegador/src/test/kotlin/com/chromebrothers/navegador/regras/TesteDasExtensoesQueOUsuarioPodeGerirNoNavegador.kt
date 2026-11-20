package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDasExtensoesQueOUsuarioPodeGerirNoNavegador {
    @Test
    fun aExtensaoEmbutidaDoAplicativoNaoApareceNaLista() {
        assertFalse(
            ExtensoesQueOUsuarioPodeGerirNoNavegador.visivel(
                id = "chromebrothers@navegador",
                embutida = true,
                idEmbutido = "chromebrothers@navegador",
            ),
        )
    }

    @Test
    fun umaExtensaoInstaladaPeloUsuarioApareceNaLista() {
        assertTrue(
            ExtensoesQueOUsuarioPodeGerirNoNavegador.visivel(
                id = "uBlock0@raymondhill.net",
                embutida = false,
                idEmbutido = "chromebrothers@navegador",
            ),
        )
    }
}
