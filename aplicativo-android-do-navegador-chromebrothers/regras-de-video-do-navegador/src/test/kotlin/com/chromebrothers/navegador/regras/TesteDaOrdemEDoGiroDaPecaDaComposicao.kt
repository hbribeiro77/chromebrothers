package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaOrdemEDoGiroDaPecaDaComposicao {
    @Test
    fun trazerParaFrenteEEnviarParaTras() {
        val ids = listOf(1, 2, 3)
        assertEquals(listOf(1, 3, 2), OrdemDasPecasDaComposicao.trazerParaFrente(ids, 2))
        assertEquals(listOf(2, 1, 3), OrdemDasPecasDaComposicao.enviarParaTras(ids, 2))
        assertEquals(ids, OrdemDasPecasDaComposicao.trazerParaFrente(ids, 3))
        assertEquals(ids, OrdemDasPecasDaComposicao.enviarParaTras(ids, 1))
    }

    @Test
    fun aPilhaCabeOitoPecasEAListaAndaSemPerderAOrdem() {
        assertEquals(true, PilhaDeCamadasDaComposicao.cabeMaisUma(7))
        assertEquals(false, PilhaDeCamadasDaComposicao.cabeMaisUma(8))
        assertEquals(listOf(3, 1, 2), PilhaDeCamadasDaComposicao.mover(listOf(1, 2, 3), 0, 2))
        assertEquals(listOf(2, 3, 1), PilhaDeCamadasDaComposicao.mover(listOf(1, 2, 3), 2, 0))
        assertEquals(listOf(1, 2, 3), PilhaDeCamadasDaComposicao.mover(listOf(1, 2, 3), 1, 1))
    }

    @Test
    fun oGiroSegueODedoEEncaixaNosQuarentaECinco() {
        assertEquals(0f, GiroDaPecaDaComposicao.angulo(100f, 100f, 100f, 80f), 0.01f)
        assertEquals(90f, GiroDaPecaDaComposicao.angulo(100f, 100f, 130f, 100f), 0.01f)
        assertEquals(0f, GiroDaPecaDaComposicao.angulo(100f, 100f, 101f, 70f), 0.01f)
        assertEquals(30f, GiroDaPecaDaComposicao.angulo(100f, 100f, 120f, 65.359f), 0.5f)
    }

    @Test
    fun oPontoVoltaParaORetanguloDaPeca() {
        val local = GiroDaPecaDaComposicao.pontoLocal(90f, 110f, 100f, 100f, 100f)
        assertEquals(100f, local.x, 0.01f)
        assertEquals(90f, local.y, 0.01f)
        val quieto = GiroDaPecaDaComposicao.pontoLocal(0f, 40f, 70f, 10f, 20f)
        assertEquals(40f, quieto.x, 0.01f)
        assertEquals(70f, quieto.y, 0.01f)
    }
}
