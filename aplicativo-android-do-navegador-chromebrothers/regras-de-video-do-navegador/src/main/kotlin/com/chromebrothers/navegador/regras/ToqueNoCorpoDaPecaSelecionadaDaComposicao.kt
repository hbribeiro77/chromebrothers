package com.chromebrothers.navegador.regras

/**
 * Toque no corpo da peça selecionada (moldura / prévia do Colar) usa o retângulo dela.
 * Não exige que ela seja a peça no topo sob o ponto: a prévia do Colar fica entre a
 * origem e camadas superiores, e o hit pelo topo cancelava o gesto como “fora”.
 */
object ToqueNoCorpoDaPecaSelecionadaDaComposicao {
    fun acertaCorpo(
        idSelecionado: Int,
        pontoDentroDoRetanguloDaSelecionada: Boolean,
    ): Boolean = idSelecionado != -1 && pontoDentroDoRetanguloDaSelecionada
}
