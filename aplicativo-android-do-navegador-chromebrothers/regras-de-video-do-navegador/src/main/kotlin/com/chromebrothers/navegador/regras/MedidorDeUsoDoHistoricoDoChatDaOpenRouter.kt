package com.chromebrothers.navegador.regras

enum class FaixaDoMedidorDoHistoricoDoChat {
    OK,
    ATENCAO,
    CHEIO,
}

object MedidorDeUsoDoHistoricoDoChatDaOpenRouter {
    fun percentual(bytesTotais: Long, teto: Long): Int {
        if (teto <= 0L) return 100
        if (bytesTotais <= 0L) return 0
        if (bytesTotais >= teto) return 100
        return ((bytesTotais * 100) / teto).toInt()
    }

    fun faixa(percentual: Int): FaixaDoMedidorDoHistoricoDoChat = when {
        percentual >= 100 -> FaixaDoMedidorDoHistoricoDoChat.CHEIO
        percentual >= 80 -> FaixaDoMedidorDoHistoricoDoChat.ATENCAO
        else -> FaixaDoMedidorDoHistoricoDoChat.OK
    }
}
