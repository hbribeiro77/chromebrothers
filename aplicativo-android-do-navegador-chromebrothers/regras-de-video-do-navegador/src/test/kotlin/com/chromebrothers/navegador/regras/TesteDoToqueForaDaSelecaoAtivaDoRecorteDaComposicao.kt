package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoToqueForaDaSelecaoAtivaDoRecorteDaComposicao {
    @Test
    fun foraDaSelecaoAtivaSoCancela() {
        assertTrue(
            ToqueForaDaSelecaoAtivaDoRecorteDaComposicao.soCancelaSemIniciarNova(
                selecaoAtiva = true,
                toqueNoInteriorOuChromeDaSelecao = false,
            ),
        )
    }

    @Test
    fun dentroOuSemSelecaoNaoESoCancelar() {
        assertFalse(
            ToqueForaDaSelecaoAtivaDoRecorteDaComposicao.soCancelaSemIniciarNova(
                selecaoAtiva = true,
                toqueNoInteriorOuChromeDaSelecao = true,
            ),
        )
        assertFalse(
            ToqueForaDaSelecaoAtivaDoRecorteDaComposicao.soCancelaSemIniciarNova(
                selecaoAtiva = false,
                toqueNoInteriorOuChromeDaSelecao = false,
            ),
        )
    }
}
