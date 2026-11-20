package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoRetanguloDeRecorteComecaVazioComoOLaco {
    @Test
    fun aoEntrarNaoPreSelecionaSalvoSeInteligenteArmada() {
        assertFalse(RetanguloDeRecorteComecaVazioComoOLaco.devePreSelecionarAoEntrar(selecaoInteligenteArmada = false))
        assertTrue(RetanguloDeRecorteComecaVazioComoOLaco.devePreSelecionarAoEntrar(selecaoInteligenteArmada = true))
    }

    @Test
    fun retanguloInicialVazioNaoCabeComoSelecaoAtiva() {
        val vazio = RetanguloDeRecorteComecaVazioComoOLaco.retanguloInicialVazio()
        assertEquals(0f, vazio.largura)
        assertEquals(0f, vazio.altura)
        assertFalse(
            SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
                ferramentaDeRecorte = true,
                formaRetangulo = true,
                largura = vazio.largura,
                altura = vazio.altura,
            ),
        )
    }
}
