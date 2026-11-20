package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDosItensDoMenuOverflowDosComponentesGravadosDaColecao {
    @Test
    fun listaVaziaNaoGeraItens() {
        assertTrue(ItensDoMenuOverflowDosComponentesGravadosDaColecao.montar(emptyList()).isEmpty())
    }

    @Test
    fun usaNomeEOrdenaPorMaisRecenteELimita() {
        val itens = (1..10).map { n ->
            ItemDaBibliotecaDeComponentesHtmlDaColecao(
                id = "id-$n",
                nome = "Comp $n",
                html = "<p>$n</p>",
                atualizadoEm = n.toLong(),
            )
        }
        val montados = ItensDoMenuOverflowDosComponentesGravadosDaColecao.montar(itens)
        assertEquals(ItensDoMenuOverflowDosComponentesGravadosDaColecao.LIMITE, montados.size)
        assertEquals("id-10", montados.first().id)
        assertEquals("Comp 10", montados.first().rotulo)
        assertEquals("id-3", montados.last().id)
    }

    @Test
    fun nomeEmBrancoCaiNoId() {
        val montados = ItensDoMenuOverflowDosComponentesGravadosDaColecao.montar(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao(
                    id = "abc",
                    nome = "   ",
                    html = "<p>x</p>",
                    atualizadoEm = 1L,
                ),
            ),
        )
        assertEquals("abc", montados.single().rotulo)
    }
}
