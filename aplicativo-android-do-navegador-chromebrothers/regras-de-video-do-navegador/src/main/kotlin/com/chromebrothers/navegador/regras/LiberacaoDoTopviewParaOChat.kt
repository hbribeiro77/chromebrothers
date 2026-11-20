package com.chromebrothers.navegador.regras

object LiberacaoDoTopviewParaOChat {
    fun estaLiberado(liberado: Boolean): Boolean = liberado

    fun liberar(): Boolean = true

    fun revogar(): Boolean = false

    fun segueSemPerguntar(pedido: PedidoDeAcaoConfirmadaDoChat, liberado: Boolean): Boolean {
        if (!liberado) return false
        return pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview
    }

    fun ehPedidoTopview(pedido: PedidoDeAcaoConfirmadaDoChat): Boolean =
        pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview
}
