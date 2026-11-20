package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao.Gesto
import kotlin.test.Test
import kotlin.test.assertEquals
import com.chromebrothers.navegador.regras.RegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao as Regras

class TesteDasRegrasDoSwipeEntreVideosNoPlayerEmbutidoDoVisorDaColecao {
    private fun decidir(
        dx: Float,
        dy: Float = 0f,
        decorridoMs: Long = 100L,
        scrubAtivo: Boolean = false,
        ponteiros: Int = 1,
    ) = Regras.decidir(
        dx = dx,
        dy = dy,
        folga = 16f,
        decorridoMs = decorridoMs,
        esperaDoToqueLongoMs = 400L,
        scrubAtivo = scrubAtivo,
        ponteiros = ponteiros,
    )

    @Test
    fun arrasteHorizontalRapidoViraSwipe() {
        assertEquals(Gesto.SWIPE, decidir(dx = -60f, dy = 10f))
        assertEquals(Gesto.SWIPE, decidir(dx = 60f))
    }

    @Test
    fun dentroDaFolgaAindaNaoDecideParaToqueEToqueDuploSeguiremNoPlayer() {
        assertEquals(Gesto.INDEFINIDO, decidir(dx = 8f, dy = 4f))
    }

    @Test
    fun arrasteDepoisDoToqueLongoFicaComOScrubFino() {
        assertEquals(Gesto.DEIXAR_PARA_O_PLAYER, decidir(dx = 80f, decorridoMs = 450L))
        assertEquals(Gesto.DEIXAR_PARA_O_PLAYER, decidir(dx = 80f, scrubAtivo = true))
    }

    @Test
    fun arrasteVerticalOuComDoisDedosNaoTrocaDeVideo() {
        assertEquals(Gesto.DEIXAR_PARA_O_PLAYER, decidir(dx = 30f, dy = 60f))
        assertEquals(Gesto.DEIXAR_PARA_O_PLAYER, decidir(dx = 80f, ponteiros = 2))
    }

    @Test
    fun textoDaPosicao() {
        assertEquals("3 de 7", Regras.textoDaPosicao(2, 7))
        assertEquals("", Regras.textoDaPosicao(0, 0))
    }
}
