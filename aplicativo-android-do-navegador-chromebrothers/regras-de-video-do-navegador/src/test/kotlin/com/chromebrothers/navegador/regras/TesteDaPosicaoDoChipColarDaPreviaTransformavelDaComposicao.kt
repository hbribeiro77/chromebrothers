package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaPosicaoDoChipColarDaPreviaTransformavelDaComposicao {
    @Test
    fun prefereAbaixoQuandoCabeParaLiberarCaboDeGiro() {
        assertEquals(
            LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO,
            PosicaoDoChipColarDaPreviaTransformavelDaComposicao.decidir(
                bboxTopo = 120f,
                bboxBaixo = 200f,
                alturaVisivel = 800f,
                alturaChip = 48f,
                folga = 12f,
            ),
        )
    }

    @Test
    fun sobeQuandoNaoCabeAbaixo() {
        assertEquals(
            LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA,
            PosicaoDoChipColarDaPreviaTransformavelDaComposicao.decidir(
                bboxTopo = 100f,
                bboxBaixo = 760f,
                alturaVisivel = 800f,
                alturaChip = 48f,
                folga = 12f,
            ),
        )
    }

    @Test
    fun acimaReservaEspacoDoCaboDeGiro() {
        assertEquals(
            48f,
            PosicaoDoChipColarDaPreviaTransformavelDaComposicao.folgaExtraAcimaParaCaboDeGiro(
                lado = LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA,
                reservaDoCabo = 48f,
            ),
        )
        assertEquals(
            0f,
            PosicaoDoChipColarDaPreviaTransformavelDaComposicao.folgaExtraAcimaParaCaboDeGiro(
                lado = LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO,
                reservaDoCabo = 48f,
            ),
        )
    }
}
