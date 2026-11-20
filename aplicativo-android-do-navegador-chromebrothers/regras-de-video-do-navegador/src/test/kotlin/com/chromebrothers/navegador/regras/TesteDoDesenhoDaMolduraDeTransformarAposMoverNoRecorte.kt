package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoDesenhoDaMolduraDeTransformarAposMoverNoRecorte {
    @Test
    fun aposMoverComNovaCamadaDesenhaMolduraMesmoNaFerramentaDeRecorte() {
        assertTrue(
            DesenhoDaMolduraDeTransformarNoEditorDeImagem.sobreFerramentaDeRecorte(
                pecaEmModoTransformar = true,
                selecaoAtivaDeRecorte = false,
            ),
        )
    }

    @Test
    fun comSelecaoDeRecorteAtivaNaoMisturaMolduraDeTransformar() {
        assertFalse(
            DesenhoDaMolduraDeTransformarNoEditorDeImagem.sobreFerramentaDeRecorte(
                pecaEmModoTransformar = true,
                selecaoAtivaDeRecorte = true,
            ),
        )
    }

    @Test
    fun semModoTransformarNaoDesenhaMolduraNoRecorte() {
        assertFalse(
            DesenhoDaMolduraDeTransformarNoEditorDeImagem.sobreFerramentaDeRecorte(
                pecaEmModoTransformar = false,
                selecaoAtivaDeRecorte = false,
            ),
        )
    }
}
