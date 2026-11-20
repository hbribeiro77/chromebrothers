package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoLimiteDeItensExibidosNoVisualizadorDeMidiaNaColecao {
    @Test
    fun pastaVirtualCortaEmQuinhentos() {
        val itens = (1..600).map { n ->
            ItemDaPastaEscolhidaPeloComponente(
                nome = "f$n.jpg",
                uri = "content://doc/$n",
                mime = "image/jpeg",
                pasta = false,
                tamanho = 1L,
            )
        }
        val json = PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.montarListagem(itens)
        assertEquals(
            LimiteDeItensExibidosNoVisualizadorDeMidiaNaColecao.LIMITE,
            Regex("\"nome\":\"f\\d+\\.jpg\"").findAll(json).count(),
        )
        assertTrue(json.contains("\"cortados\":100"))
        assertTrue(json.contains("\"totalEncontrado\":600"))
    }
}
