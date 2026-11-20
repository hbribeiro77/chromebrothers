package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoAlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador {
    @Test
    fun margensSeguemACaixaNoMeioDaBarra() {
        // barra 1000; caixa começa em 180 com largura 500 → fim 320
        val m = AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.margens(
            esquerdaDaCaixaNoPai = 180,
            larguraDaCaixa = 500,
            larguraDoPai = 1000,
        )
        assertEquals(180, m.inicio)
        assertEquals(320, m.fim)
    }

    @Test
    fun paiOuCaixaInvalidosZeramMargens() {
        assertEquals(
            AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.Margens(0, 0),
            AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.margens(10, 100, 0),
        )
    }

    @Test
    fun topoDescontaPaddingDoPaiDasBarrasDoSistema() {
        // barra: topo 166, altura 98 → fundo 264; padding status 53 → margin 211
        assertEquals(
            211,
            AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.topoAbaixoDaAncora(
                topoDaAncoraNaJanela = 166,
                alturaDaAncora = 98,
                paddingTopoDoPai = 53,
            ),
        )
        assertEquals(
            264,
            AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.topoAbaixoDaAncora(
                topoDaAncoraNaJanela = 166,
                alturaDaAncora = 98,
                paddingTopoDoPai = 0,
            ),
        )
    }

    @Test
    fun margensDescontamPaddingHorizontalDoPai() {
        val m = AlinhamentoDaListaDeSugestoesComACaixaDeEnderecoDoNavegador.margens(
            esquerdaDaCaixaNoPai = 180,
            larguraDaCaixa = 500,
            larguraDoPai = 1000,
            paddingEsquerdoDoPai = 20,
            paddingDireitoDoPai = 20,
        )
        assertEquals(160, m.inicio)
        assertEquals(300, m.fim)
    }
}
