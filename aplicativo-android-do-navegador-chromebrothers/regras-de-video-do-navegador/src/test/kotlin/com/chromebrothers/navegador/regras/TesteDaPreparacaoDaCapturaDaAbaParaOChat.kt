package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaPreparacaoDaCapturaDaAbaParaOChat {
    @Test
    fun reduzOLadoMaiorSemEsticarParaCima() {
        assertEquals(800 to 600, PreparacaoDaCapturaDaAbaParaOChat.medidasReduzidas(800, 600))
        val (largura, altura) = PreparacaoDaCapturaDaAbaParaOChat.medidasReduzidas(2560, 1440)
        assertEquals(PreparacaoDaCapturaDaAbaParaOChat.LADO_MAXIMO, largura)
        assertEquals(720, altura)
    }

    @Test
    fun oRelatoIncluiOArquivoLocalParaAPaginaEColecao() {
        val json = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(json.contains(FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA))
        assertTrue(
            FerramentasDeLeituraDoChatDaOpenRouter.CAPTURAR_ABA in
                FerramentasDeLeituraDoChatDaOpenRouter.permitidas,
        )
        val relato = PreparacaoDaCapturaDaAbaParaOChat.relato(
            100,
            50,
            "file:///data/captura-da-aba.jpg",
        )
        assertTrue(relato.contains("100x50"))
        assertTrue(relato.contains("file:///data/captura-da-aba.jpg"))
        assertTrue(relato.contains("coleção") || relato.contains("colecao") || relato.contains("página") || relato.contains("pagina") || relato.contains("Use este endereço"))
    }
}
