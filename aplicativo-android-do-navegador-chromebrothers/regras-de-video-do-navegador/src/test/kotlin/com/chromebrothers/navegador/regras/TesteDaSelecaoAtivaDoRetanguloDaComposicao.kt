package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaSelecaoAtivaDoRetanguloDaComposicao {
    @Test
    fun ativaQuandoRecorteRetanguloECabe() {
        assertTrue(
            SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
                ferramentaDeRecorte = true,
                formaRetangulo = true,
                largura = 100f,
                altura = 80f,
            ),
        )
    }

    @Test
    fun inativaSeNaoERecorteOuNaoERetanguloOuNaoCabe() {
        assertFalse(
            SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
                ferramentaDeRecorte = false,
                formaRetangulo = true,
                largura = 100f,
                altura = 80f,
            ),
        )
        assertFalse(
            SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
                ferramentaDeRecorte = true,
                formaRetangulo = false,
                largura = 100f,
                altura = 80f,
            ),
        )
        assertFalse(
            SelecaoAtivaDoRetanguloDaComposicao.estaAtiva(
                ferramentaDeRecorte = true,
                formaRetangulo = true,
                largura = 10f,
                altura = 10f,
            ),
        )
    }

    @Test
    fun acoesCompartilhamEfeitosComOLaco() {
        assertTrue(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.COPIAR) ==
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.COPIAR_SEM_BURACO,
        )
        assertTrue(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.EXCLUIR) ==
                EfeitoDaAcaoDaSelecaoAtivaDoLaco.EXCLUIR_COM_BURACO,
        )
    }
}
