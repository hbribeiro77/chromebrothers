package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaPosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo {
    private val regra = PosicaoDaJanelaFlutuanteAoAbrirPelaBolhaNoCantoMaisProximo

    @Test
    fun bolhaNoAltoAEsquerdaViraOCantoSuperiorEsquerdo() {
        assertEquals(100f to 80f, regra.calcular(100f, 80f, 50f, 400f, 300f, 2000f, 1200f))
    }

    @Test
    fun bolhaEmbaixoADireitaViraOCantoInferiorDireito() {
        assertEquals(1250f to 650f, regra.calcular(1600f, 900f, 50f, 400f, 300f, 2000f, 1200f))
    }

    @Test
    fun bolhaColadaNaBordaMantemAJanelaDentroDaTela() {
        assertEquals(0f to 900f, regra.calcular(0f, 1180f, 50f, 400f, 300f, 2000f, 1200f))
    }
}
