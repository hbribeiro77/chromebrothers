package com.chromebrothers.navegador.regras

/** Critério de “seleção ativa” do retângulo — espelha o laço fechado. */
object SelecaoAtivaDoRetanguloDaComposicao {
    fun estaAtiva(
        ferramentaDeRecorte: Boolean,
        formaRetangulo: Boolean,
        largura: Float,
        altura: Float,
    ): Boolean {
        return ferramentaDeRecorte &&
            formaRetangulo &&
            DecisaoDoRetanguloSobreAImagemDaComposicao.cabe(largura, altura)
    }
}
