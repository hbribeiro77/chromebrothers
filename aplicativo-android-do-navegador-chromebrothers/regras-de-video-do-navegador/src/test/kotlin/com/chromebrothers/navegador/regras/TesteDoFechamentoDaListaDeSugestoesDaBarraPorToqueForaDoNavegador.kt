package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoFechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador {
    @Test
    fun fechaQuandoListaVisivelEToqueFora() {
        assertTrue(
            FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador.deveFechar(
                listaVisivel = true,
                toqueNaCaixaDeEndereco = false,
                toqueNaLista = false,
            ),
        )
    }

    @Test
    fun naoFechaNaCaixaNemNaListaNemSeJaEscondida() {
        assertFalse(
            FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador.deveFechar(
                listaVisivel = true,
                toqueNaCaixaDeEndereco = true,
                toqueNaLista = false,
            ),
        )
        assertFalse(
            FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador.deveFechar(
                listaVisivel = true,
                toqueNaCaixaDeEndereco = false,
                toqueNaLista = true,
            ),
        )
        assertFalse(
            FechamentoDaListaDeSugestoesDaBarraPorToqueForaDoNavegador.deveFechar(
                listaVisivel = false,
                toqueNaCaixaDeEndereco = false,
                toqueNaLista = false,
            ),
        )
    }
}
