package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaSelecaoAtivaDoLacoDaComposicao {
    @Test
    fun cadaAcaoTemOEfeitoEsperado() {
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.COPIAR_SEM_BURACO,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.COPIAR),
        )
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.DESTACAR_COM_BURACO,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.MOVER),
        )
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.EXCLUIR_COM_BURACO,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.EXCLUIR),
        )
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.INVERTER_MASCARA,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.INVERTER),
        )
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.REDUZIR_MOLDURA_A_SELECAO,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.RECORTAR),
        )
        assertEquals(
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.CONFIRMAR_COLAR_FUNDIR,
            EfeitoDaAcaoDaSelecaoAtivaDoLaco.da(AcaoDaSelecaoAtivaDoLaco.COLAR),
        )
    }

    @Test
    fun inverterAlternaOEstadoDaMascara() {
        assertTrue(InverterSelecaoAtivaDoLaco.aposInverter(invertidaAgora = false))
        assertFalse(InverterSelecaoAtivaDoLaco.aposInverter(invertidaAgora = true))
    }

    @Test
    fun recortarExigeConfirmacaoNoModal() {
        assertTrue(RecortarDaSelecaoAtivaDoLaco.exigeConfirmacao())
    }

    @Test
    fun cancelarModalMantemSelecaoAtiva() {
        assertTrue(RecortarDaSelecaoAtivaDoLaco.mantemSelecaoAoCancelarModal())
        assertFalse(RecortarDaSelecaoAtivaDoLaco.mantemSelecaoAoConfirmar())
    }

    @Test
    fun barraFicaAcimaQuandoCabeEAbaixoQuandoNaoCabe() {
        assertEquals(
            LadoDaBarraFlutuanteDaSelecaoAtiva.ACIMA,
            PosicaoDaBarraFlutuanteDaSelecaoAtiva.decidir(
                bboxTopo = 120f,
                bboxBaixo = 200f,
                alturaVisivel = 800f,
                alturaBarra = 48f,
                folga = 12f,
            ),
        )
        assertEquals(
            LadoDaBarraFlutuanteDaSelecaoAtiva.ABAIXO,
            PosicaoDaBarraFlutuanteDaSelecaoAtiva.decidir(
                bboxTopo = 20f,
                bboxBaixo = 80f,
                alturaVisivel = 800f,
                alturaBarra = 48f,
                folga = 12f,
            ),
        )
    }
}
