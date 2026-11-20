package com.chromebrothers.navegador.regras

/**
 * Com seleção ativa (laço/retângulo), o toque no quadro trata a seleção —
 * não cai no fluxo de selecionar/mover peça (camada de cima roubava o gesto).
 */
object ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem {
    fun priorizaSelecaoSobrePeca(
        ferramentaDeRecorte: Boolean,
        selecaoAtiva: Boolean,
    ): Boolean = ferramentaDeRecorte && selecaoAtiva
}
