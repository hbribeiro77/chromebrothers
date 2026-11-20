package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoMenuDoToqueLongoNoLinkDaPagina {
    @Test
    fun linkHttpAbreOMenu() {
        assertEquals(
            "https://exemplo.test/artigo",
            MenuDoToqueLongoNoLinkDaPagina.endereco(
                ehImagem = false,
                link = "https://exemplo.test/artigo",
            ),
        )
        assertEquals(
            "http://exemplo.test/arquivo",
            MenuDoToqueLongoNoLinkDaPagina.endereco(
                ehImagem = false,
                link = " http://exemplo.test/arquivo ",
            ),
        )
    }

    @Test
    fun imagemContinuaNoMenuDeImagem() {
        assertNull(
            MenuDoToqueLongoNoLinkDaPagina.endereco(
                ehImagem = true,
                link = "https://exemplo.test/artigo",
            ),
        )
    }

    @Test
    fun linkQueNaoEPaginaNaoAbreOMenu() {
        assertNull(MenuDoToqueLongoNoLinkDaPagina.endereco(false, "javascript:void(0)"))
        assertNull(MenuDoToqueLongoNoLinkDaPagina.endereco(false, "mailto:a@exemplo.test"))
        assertNull(MenuDoToqueLongoNoLinkDaPagina.endereco(false, ""))
        assertNull(MenuDoToqueLongoNoLinkDaPagina.endereco(false, null))
    }
}
