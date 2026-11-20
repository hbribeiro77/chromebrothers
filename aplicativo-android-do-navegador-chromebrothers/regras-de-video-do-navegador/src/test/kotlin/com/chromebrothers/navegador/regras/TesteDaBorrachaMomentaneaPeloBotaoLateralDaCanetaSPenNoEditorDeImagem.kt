package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaBorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem {
    @Test
    fun botaoLateralDetectaMascarasDoMotionEvent() {
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.BOTAO_STYLUS_PRIMARIO,
            ),
        )
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.BOTAO_STYLUS_SECUNDARIO,
            ),
        )
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(
                BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.BOTAO_SECUNDARIO,
            ),
        )
        assertFalse(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.botaoLateralPressionado(0),
        )
    }

    @Test
    fun tracoEhBorrachaComFerramentaPontaFisicaOuBotaoLateral() {
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.tracoEhBorracha(
                ferramentaEhBorrachaNaBarra = true,
                pontaFisicaEhBorracha = false,
                botaoLateralPressionado = false,
            ),
        )
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.tracoEhBorracha(
                ferramentaEhBorrachaNaBarra = false,
                pontaFisicaEhBorracha = true,
                botaoLateralPressionado = false,
            ),
        )
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.tracoEhBorracha(
                ferramentaEhBorrachaNaBarra = false,
                pontaFisicaEhBorracha = false,
                botaoLateralPressionado = true,
            ),
        )
        assertFalse(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.tracoEhBorracha(
                ferramentaEhBorrachaNaBarra = false,
                pontaFisicaEhBorracha = false,
                botaoLateralPressionado = false,
            ),
        )
    }

    @Test
    fun aoSoltarBotaoRestauraUltimoPincelSeBarraNaoEstavaEmBorracha() {
        assertEquals(
            "caneta",
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.pincelAposSoltarBotao(
                ferramentaDaBarraEhBorracha = false,
                idDoUltimoPincel = "caneta",
            ),
        )
        assertEquals(
            null,
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.pincelAposSoltarBotao(
                ferramentaDaBarraEhBorracha = true,
                idDoUltimoPincel = "caneta",
            ),
        )
    }

    @Test
    fun detectaSoltarBotaoTambemPelaTransicaoDeEstado() {
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.soltouBotaoLateral(
                estavaPressionado = true,
                estaPressionado = false,
            ),
        )
        assertFalse(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.soltouBotaoLateral(
                estavaPressionado = false,
                estaPressionado = false,
            ),
        )
        assertFalse(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem.soltouBotaoLateral(
                estavaPressionado = true,
                estaPressionado = true,
            ),
        )
    }

    @Test
    fun comBotaoOuPontaBorrachaACanetaPriorizaTracoMesmoComSelecao() {
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem
                .canetaPriorizaTracoSobreSelecaoOuTransformar(
                    botaoLateralPressionado = true,
                    pontaFisicaEhBorracha = false,
                ),
        )
        assertTrue(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem
                .canetaPriorizaTracoSobreSelecaoOuTransformar(
                    botaoLateralPressionado = false,
                    pontaFisicaEhBorracha = true,
                ),
        )
        assertFalse(
            BorrachaMomentaneaPeloBotaoLateralDaCanetaSPenNoEditorDeImagem
                .canetaPriorizaTracoSobreSelecaoOuTransformar(
                    botaoLateralPressionado = false,
                    pontaFisicaEhBorracha = false,
                ),
        )
    }
}
