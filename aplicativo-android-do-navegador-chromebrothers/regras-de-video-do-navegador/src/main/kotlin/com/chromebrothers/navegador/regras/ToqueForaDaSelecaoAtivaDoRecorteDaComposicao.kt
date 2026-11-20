package com.chromebrothers.navegador.regras

/**
 * Toque fora da seleção ativa (e fora de chips/alças) só cancela.
 * Nova seleção começa só num toque seguinte.
 */
object ToqueForaDaSelecaoAtivaDoRecorteDaComposicao {
    fun soCancelaSemIniciarNova(
        selecaoAtiva: Boolean,
        toqueNoInteriorOuChromeDaSelecao: Boolean,
    ): Boolean = selecaoAtiva && !toqueNoInteriorOuChromeDaSelecao
}
