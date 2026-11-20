package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoTracoNaCamadaDaComposicao {
    @Test
    fun aPinturaVaiNaCamadaAtivaQuandoElaAceitaTraco() {
        assertEquals(true, DestinoDoTracoNaCamadaAtiva.aceitaPintura(temCamadaAtivaCompativel = true))
        assertEquals(false, DestinoDoTracoNaCamadaAtiva.aceitaPintura(temCamadaAtivaCompativel = false))
    }

    @Test
    fun alternarVisibilidadeDaCamadaInverteOEstado() {
        assertEquals(false, VisibilidadeDaCamadaDaComposicao.aposAlternar(visivelAgora = true))
        assertEquals(true, VisibilidadeDaCamadaDaComposicao.aposAlternar(visivelAgora = false))
    }

    @Test
    fun oPontoNaPecaParadaFicaRelativoAoCanto() {
        val ponto = TracoNaCamadaDaComposicao.pontoNormalizado(
            esquerda = 10f,
            topo = 20f,
            largura = 100f,
            altura = 50f,
            angulo = 0f,
            x = 30f,
            y = 40f,
        )
        assertEquals(0.2f, ponto?.x)
        assertEquals(0.4f, ponto?.y)
    }

    @Test
    fun oPontoDaImagemViraPontoDoQuadroInteiro() {
        val ponto = TracoNaCamadaDaComposicao.paraOQuadro(
            u = 0.2f,
            v = 0.4f,
            esquerda = 10f,
            topo = 20f,
            largura = 100f,
            altura = 50f,
            larguraDoQuadro = 200f,
            alturaDoQuadro = 100f,
        )
        assertEquals(0.15f, ponto?.x)
        assertEquals(0.4f, ponto?.y)
    }

    @Test
    fun oPontoForaDaPecaNaoEntraNoTraco() {
        assertEquals(
            false,
            TracoNaCamadaDaComposicao.cabeNoRetangulo(1.2f, 0.4f),
        )
        assertEquals(
            true,
            TracoNaCamadaDaComposicao.cabeNoRetangulo(0.2f, 0.4f),
        )
    }

    @Test
    fun oRecorteDeslocaOTracoParaONovoRetangulo() {
        val ponto = TracoNaCamadaDaComposicao.remapear(
            u = 0.5f,
            v = 0.5f,
            antigaEsquerda = 0f,
            antigaTopo = 0f,
            antigaLargura = 100f,
            antigaAltura = 100f,
            novaEsquerda = 0f,
            novaTopo = 0f,
            novaLargura = 40f,
            novaAltura = 40f,
            deslocamentoX = -20f,
            deslocamentoY = -30f,
        )
        assertEquals(0.75f, ponto?.x)
        assertEquals(0.5f, ponto?.y)
        assertEquals(2.5f, TracoNaCamadaDaComposicao.larguraRemapeada(1f, 100f, 40f))
    }

    @Test
    fun umRetanguloSemTamanhoNaoRemapeia() {
        assertNull(
            TracoNaCamadaDaComposicao.remapear(
                u = 0.5f,
                v = 0.5f,
                antigaEsquerda = 0f,
                antigaTopo = 0f,
                antigaLargura = 0f,
                antigaAltura = 10f,
                novaEsquerda = 0f,
                novaTopo = 0f,
                novaLargura = 10f,
                novaAltura = 10f,
                deslocamentoX = 0f,
                deslocamentoY = 0f,
            ),
        )
    }
}
