package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoToqueAposMoverComMolduraDeTransformarDaPeca {
    @Test
    fun toqueNaPecaOuChromeFazGestoDaPeca() {
        assertEquals(
            DestinoDoToqueAposMoverComMolduraDeTransformar.GESTO_DA_PECA,
            ToqueAposMoverComMolduraDeTransformarDaPeca.destino(
                pecaEmModoTransformar = true,
                toqueNaPecaOuChrome = true,
            ),
        )
    }

    @Test
    fun toqueForaDaPecaSelecionadaEncerraMesmoSobreOutraCamada() {
        assertEquals(
            DestinoDoToqueAposMoverComMolduraDeTransformar.ENCERRAR_MOLDURA,
            ToqueAposMoverComMolduraDeTransformarDaPeca.destino(
                pecaEmModoTransformar = true,
                toqueNaPecaOuChrome = false,
            ),
        )
    }

    @Test
    fun toqueDoChipMoverNaoEncerraMolduraAntesDeSoltar() {
        assertEquals(
            DestinoDoToqueAposMoverComMolduraDeTransformar.FLUXO_NORMAL,
            ToqueAposMoverComMolduraDeTransformarDaPeca.destino(
                pecaEmModoTransformar = true,
                toqueNaPecaOuChrome = false,
                aguardandoSoltarAposArmarTransformar = true,
            ),
        )
        assertTrue(
            ToqueAposMoverComMolduraDeTransformarDaPeca.deveConsumirToqueEnquantoAguardaSoltarAposArmar(
                aguardandoSoltarAposArmarTransformar = true,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.deveConsumirToqueEnquantoAguardaSoltarAposArmar(
                aguardandoSoltarAposArmarTransformar = false,
            ),
        )
    }

    @Test
    fun canetaNoModoTransformarNaoCancelaOGestoACadaMove() {
        assertTrue(
            ToqueAposMoverComMolduraDeTransformarDaPeca.canetaSegueGestoSemCancelar(
                pecaEmModoTransformar = true,
                selecaoAtivaDeRecorte = false,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.canetaSegueGestoSemCancelar(
                pecaEmModoTransformar = false,
                selecaoAtivaDeRecorte = false,
            ),
        )
    }

    @Test
    fun semModoTransformarOToqueSegueOFluxoNormal() {
        assertEquals(
            DestinoDoToqueAposMoverComMolduraDeTransformar.FLUXO_NORMAL,
            ToqueAposMoverComMolduraDeTransformarDaPeca.destino(
                pecaEmModoTransformar = false,
                toqueNaPecaOuChrome = false,
            ),
        )
    }

    @Test
    fun modoTransformarBloqueiaPinturaENovoRecorteInclusiveNaCaneta() {
        assertTrue(
            ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                pecaEmModoTransformar = true,
                selecaoAtivaDeRecorte = false,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                pecaEmModoTransformar = true,
                selecaoAtivaDeRecorte = true,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.prendeCanetaEDedoNoGestoDaPeca(
                pecaEmModoTransformar = false,
                selecaoAtivaDeRecorte = false,
            ),
        )
    }

    @Test
    fun encerrarMolduraEntraNoHistoricoParaUndo() {
        assertTrue(
            ToqueAposMoverComMolduraDeTransformarDaPeca.encerrarEntraNoHistorico(
                pecaEmModoTransformar = true,
                idDaPecaSelecionada = 7,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.encerrarEntraNoHistorico(
                pecaEmModoTransformar = false,
                idDaPecaSelecionada = 7,
            ),
        )
        assertFalse(
            ToqueAposMoverComMolduraDeTransformarDaPeca.encerrarEntraNoHistorico(
                pecaEmModoTransformar = true,
                idDaPecaSelecionada = -1,
            ),
        )
    }
}
