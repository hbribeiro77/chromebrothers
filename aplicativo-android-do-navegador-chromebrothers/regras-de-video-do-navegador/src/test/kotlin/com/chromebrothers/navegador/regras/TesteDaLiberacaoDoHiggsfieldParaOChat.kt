package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaLiberacaoDoHiggsfieldParaOChat {
    @Test
    fun liberarERevogarAlternamAFlag() {
        assertFalse(LiberacaoDoHiggsfieldParaOChat.estaLiberado(false))
        assertTrue(LiberacaoDoHiggsfieldParaOChat.estaLiberado(true))
        assertTrue(LiberacaoDoHiggsfieldParaOChat.liberar())
        assertFalse(LiberacaoDoHiggsfieldParaOChat.revogar())
    }

    @Test
    fun segueSemPerguntarSoComFlagEPedidoHiggsfield() {
        val imagem = PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield("gato", "Nano Banana 2.1")
        val video = PedidoDeAcaoConfirmadaDoChat.GerarVideoHiggsfield("onda", "")
        val clicar = PedidoDeAcaoConfirmadaDoChat.Clicar("Tools")
        assertFalse(LiberacaoDoHiggsfieldParaOChat.segueSemPerguntar(imagem, liberado = false))
        assertTrue(LiberacaoDoHiggsfieldParaOChat.segueSemPerguntar(imagem, liberado = true))
        assertTrue(LiberacaoDoHiggsfieldParaOChat.segueSemPerguntar(video, liberado = true))
        assertFalse(LiberacaoDoHiggsfieldParaOChat.segueSemPerguntar(clicar, liberado = true))
    }
}
