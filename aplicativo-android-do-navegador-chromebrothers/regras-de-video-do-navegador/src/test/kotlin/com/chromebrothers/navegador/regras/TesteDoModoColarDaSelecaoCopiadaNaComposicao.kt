package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoModoColarDaSelecaoCopiadaNaComposicao {
    @Test
    fun destinoImagemDiferenteDaPreviaAceitaFundir() {
        assertTrue(
            ModoColarDaSelecaoCopiadaNaComposicao.destinoAceitaFundir(
                idDestino = 1,
                idPrevia = 9,
                destinoEhImagem = true,
            ),
        )
    }

    @Test
    fun destinoIgualPreviaOuNaoImagemRecusa() {
        assertFalse(
            ModoColarDaSelecaoCopiadaNaComposicao.destinoAceitaFundir(
                idDestino = 9,
                idPrevia = 9,
                destinoEhImagem = true,
            ),
        )
        assertFalse(
            ModoColarDaSelecaoCopiadaNaComposicao.destinoAceitaFundir(
                idDestino = 2,
                idPrevia = 9,
                destinoEhImagem = false,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun destinoFolhaVaziaAceitaParaPromoverNoColar() {
        assertTrue(
            ModoColarDaSelecaoCopiadaNaComposicao.destinoAceitaFundir(
                idDestino = 4,
                idPrevia = 9,
                destinoEhImagem = false,
                destinoEhFolha = true,
            ),
        )
    }

    @Test
    fun foraETrocaDeFerramentaCancelam() {
        assertTrue(ModoColarDaSelecaoCopiadaNaComposicao.toqueForaCancelaModo(true))
        assertFalse(ModoColarDaSelecaoCopiadaNaComposicao.toqueForaCancelaModo(false))
        assertTrue(ModoColarDaSelecaoCopiadaNaComposicao.trocaDeFerramentaCancelaModo(true))
    }

    @Test
    fun previaSomeDaListaEnquantoModoColar() {
        assertTrue(
            ModoColarDaSelecaoCopiadaNaComposicao.deveOcultarPreviaNaListaDeCamadas(
                modoColarAtivo = true,
                idDaCamada = 7,
                idPrevia = 7,
            ),
        )
        assertFalse(
            ModoColarDaSelecaoCopiadaNaComposicao.deveOcultarPreviaNaListaDeCamadas(
                modoColarAtivo = true,
                idDaCamada = 1,
                idPrevia = 7,
            ),
        )
    }

    @Test
    fun gestosDaPreviaNaoEntramNoHistoricoNoModoColar() {
        assertFalse(ModoColarDaSelecaoCopiadaNaComposicao.gestosDaPreviaEntramNoHistorico(true))
        assertTrue(ModoColarDaSelecaoCopiadaNaComposicao.gestosDaPreviaEntramNoHistorico(false))
    }
}
