package com.chromebrothers.navegador.regras

/**
 * Preferência do chip Mover:
 * - marcada: ao encerrar a moldura, a peça continua como camada
 * - desmarcada: ao encerrar, funde de volta na origem (mesma camada)
 * Nos dois casos o Mover cria peça com moldura de transformar.
 */
object DecisaoDoMoverDaSelecaoAtivaDaComposicao {
    fun criaNovaCamada(preferenciaMarcada: Boolean): Boolean = preferenciaMarcada

    fun fundirAoEncerrarTransformar(preferenciaNovaCamada: Boolean): Boolean = !preferenciaNovaCamada
}
