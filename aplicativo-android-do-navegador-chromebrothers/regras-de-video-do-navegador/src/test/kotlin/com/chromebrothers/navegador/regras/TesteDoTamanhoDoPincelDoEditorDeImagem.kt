package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.TamanhoDoPincelDoEditorDeImagem as Tam
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoTamanhoDoPincelDoEditorDeImagem {

    @Test
    fun extremosDaCurva() {
        assertEquals(Tam.MINIMO_DP, Tam.diametroDp(0f, borracha = false), 0.001f)
        assertEquals(Tam.MAXIMO_DP_PINCEL, Tam.diametroDp(1f, borracha = false), 0.01f)
        assertEquals(Tam.MAXIMO_DP_BORRACHA, Tam.diametroDp(1f, borracha = true), 0.01f)
    }

    @Test
    fun idaEVolta() {
        listOf(0.5f, 3f, 14f, 120f).forEach { d ->
            assertEquals(d, Tam.diametroDp(Tam.valorDoDiametro(d, false), false), d * 0.001f)
        }
    }

    @Test
    fun padraoBateComOTamanhoDeAntes() {
        assertEquals(14f, Tam.diametroDp(Tam.PADRAO_PINCEL, false), 0.01f)
        assertEquals(28f, Tam.diametroDp(Tam.PADRAO_BORRACHA, true), 0.01f)
    }

    @Test
    fun metadeDoControleAindaEPequenaPrecisaoNosFinos() {
        // Curva exponencial: 50% do controle fica bem abaixo de 50% do diâmetro máximo.
        assertTrue(Tam.diametroDp(0.5f, false) < 20f)
        assertTrue(Tam.diametroDp(0.1f, false) < 1.5f)
    }

    @Test
    fun percentualVaiDe1A100() {
        assertEquals(1, Tam.percentual(0f))
        assertEquals(37, Tam.percentual(0.37f))
        assertEquals(100, Tam.percentual(1f))
    }

    @Test
    fun ajusteFinoDesaceleraComADistancia() {
        assertEquals(1f, Tam.fatorDoAjusteFino(0f, 60f))
        assertEquals(0.5f, Tam.fatorDoAjusteFino(60f, 60f), 0.001f)
        assertEquals(0.1f, Tam.fatorDoAjusteFino(10_000f, 60f))
    }
}
