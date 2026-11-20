package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoToqueComSelecaoAtivaDoRecorteNoEditorDeImagem {
    @Test
    fun comSelecaoAtivaPriorizaSelecao() {
        assertTrue(
            ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem.priorizaSelecaoSobrePeca(
                ferramentaDeRecorte = true,
                selecaoAtiva = true,
            ),
        )
    }

    @Test
    fun semSelecaoOuOutraFerramentaNaoPrioriza() {
        assertFalse(
            ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem.priorizaSelecaoSobrePeca(
                ferramentaDeRecorte = true,
                selecaoAtiva = false,
            ),
        )
        assertFalse(
            ToqueComSelecaoAtivaDoRecorteNoEditorDeImagem.priorizaSelecaoSobrePeca(
                ferramentaDeRecorte = false,
                selecaoAtiva = true,
            ),
        )
    }
}
