package com.chromebrothers.navegador.regras

object LiberacaoDoHiggsfieldParaOChat {
    fun estaLiberado(liberado: Boolean): Boolean = liberado

    fun liberar(): Boolean = true

    fun revogar(): Boolean = false

    fun segueSemPerguntar(pedido: PedidoDeAcaoConfirmadaDoChat, liberado: Boolean): Boolean {
        if (!liberado) return false
        return pedido is PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield ||
            pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield
    }

    fun ehPedidoHiggsfield(pedido: PedidoDeAcaoConfirmadaDoChat): Boolean {
        return pedido is PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield ||
            pedido is PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield
    }
}
