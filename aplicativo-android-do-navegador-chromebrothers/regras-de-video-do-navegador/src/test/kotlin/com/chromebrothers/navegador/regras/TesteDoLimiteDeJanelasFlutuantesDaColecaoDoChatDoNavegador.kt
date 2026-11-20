package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoLimiteDeJanelasFlutuantesDaColecaoDoChatDoNavegador {
    @Test
    fun permiteAteTresERecusaAQuarta() {
        assertEquals(3, LimiteDeJanelasFlutuantesDaColecaoDoChat.MAXIMO)
        assertEquals(10, LimiteDeJanelasFlutuantesDaColecaoDoChat.MAXIMO_TOTAL)
        assertTrue(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeAbrir(0))
        assertTrue(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeAbrir(2))
        assertFalse(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeAbrir(3))
        assertFalse(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeAbrir(4))
        assertTrue(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeCriarTotal(9))
        assertFalse(LimiteDeJanelasFlutuantesDaColecaoDoChat.podeCriarTotal(10))
    }

    @Test
    fun mensagemDeRecusaCitaOMaximo() {
        val mensagem = LimiteDeJanelasFlutuantesDaColecaoDoChat.mensagemRecusada()
        assertTrue(mensagem.contains("3"))
        assertTrue(mensagem.contains("simultaneamente") || mensagem.contains("simultâneas"))
    }

    @Test
    fun instrucaoDocumentaOLimiteDeTresJanelas() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: teste")
        assertTrue(texto.contains("3"))
        assertTrue(
            texto.contains("simultaneamente") ||
                texto.contains("simultâneas") ||
                texto.contains("ao mesmo tempo"),
        )
        assertTrue(texto.contains("bolha") || texto.contains("minimiza"))
    }
}
