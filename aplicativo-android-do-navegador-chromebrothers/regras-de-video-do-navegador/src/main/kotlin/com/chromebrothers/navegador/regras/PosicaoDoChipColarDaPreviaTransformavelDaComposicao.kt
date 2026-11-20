package com.chromebrothers.navegador.regras

/**
 * Chip Colar da prévia: prefere abaixo do bbox para não cobrir o cabo de giro
 * (sempre no centro acima da peça). Se só couber acima, sobe além da reserva do cabo.
 */
object PosicaoDoChipColarDaPreviaTransformavelDaComposicao {
    fun decidir(
        bboxTopo: Float,
        bboxBaixo: Float,
        alturaVisivel: Float,
        alturaChip: Float,
        folga: Float,
    ): LadoDaBarraFlutuanteDaSelecaoAtiva {
        if (bboxBaixo + folga + alturaChip <= alturaVisivel) {
            return LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO
        }
        return LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA
    }

    fun folgaExtraAcimaParaCaboDeGiro(
        lado: LadoDaBarraFlutuanteDaSelecaoAtiva,
        reservaDoCabo: Float,
    ): Float = if (lado == LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA) reservaDoCabo else 0f
}
