package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter {
    @Test
    fun faixasDoMedidor() {
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.OK, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(50))
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.ATENCAO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(80))
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.CHEIO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(100))
    }

    @Test
    fun faixaAbaixoDeOitentaENoNoventaENove() {
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.OK, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(79))
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.ATENCAO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(99))
        assertEquals(FaixaDoMedidorDoHistoricoDoChat.CHEIO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(101))
    }

    @Test
    fun percentualVaiDeZeroACemETetoInvalidoValeCem() {
        assertEquals(0, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(0L, 200L))
        assertEquals(50, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(100L, 200L))
        assertEquals(33, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(1L, 3L))
        assertEquals(100, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(200L, 200L))
        assertEquals(100, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(500L, 200L))
        assertEquals(100, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(1L, 0L))
        assertEquals(100, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(1L, -5L))
        assertEquals(0, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(-1L, 100L))
    }
}
