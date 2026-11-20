package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoToqueNoCorpoDaPecaSelecionadaDaComposicao {
    @Test
    fun previaDoColarSobCamadaDeCimaAindaContaComoToqueNaSelecionada() {
        // pecaEm no topo seria a camada de cima (id 9); a prévia é id 2.
        assertTrue(
            ToqueNoCorpoDaPecaSelecionadaDaComposicao.acertaCorpo(
                idSelecionado = 2,
                pontoDentroDoRetanguloDaSelecionada = true,
            ),
        )
    }

    @Test
    fun foraDoRetanguloDaSelecionadaNaoContaMesmoComOutraCamada() {
        assertFalse(
            ToqueNoCorpoDaPecaSelecionadaDaComposicao.acertaCorpo(
                idSelecionado = 2,
                pontoDentroDoRetanguloDaSelecionada = false,
            ),
        )
    }

    @Test
    fun semSelecaoNaoAcerta() {
        assertFalse(
            ToqueNoCorpoDaPecaSelecionadaDaComposicao.acertaCorpo(
                idSelecionado = -1,
                pontoDentroDoRetanguloDaSelecionada = true,
            ),
        )
    }
}
