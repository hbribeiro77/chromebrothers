package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoToqueNasAlcasDoRetanguloDeRecorteDaComposicao {
    @Test
    fun retanguloInicialCobreAImagemInteira() {
        val area = RetanguloInicialDoRecorteSobreAImagemDaComposicao.de(
            esquerda = 100f,
            topo = 40f,
            largura = 200f,
            altura = 80f,
        )
        assertEquals(100f, area.esquerda)
        assertEquals(40f, area.topo)
        assertEquals(300f, area.direita)
        assertEquals(120f, area.baixo)
    }

    @Test
    fun retanguloInicialDaMolduraCobreOQuadroInteiro() {
        val area = RetanguloInicialDoRecorteSobreAImagemDaComposicao.daMoldura(1920f, 1080f)
        assertEquals(0f, area.esquerda)
        assertEquals(0f, area.topo)
        assertEquals(1920f, area.direita)
        assertEquals(1080f, area.baixo)
    }

    @Test
    fun selecaoInteligentePegaAImagemDeCimaNoPonto() {
        val imagens = listOf(
            AreaDaImagemParaSelecaoInteligenteDoRecorte(1, 0f, 0f, 200f, 200f),
            AreaDaImagemParaSelecaoInteligenteDoRecorte(2, 50f, 50f, 100f, 100f),
        )
        assertEquals(2, SelecaoInteligenteDoRecorteSobreAComposicao.idDaImagemNoPonto(80f, 80f, imagens))
        assertEquals(1, SelecaoInteligenteDoRecorteSobreAComposicao.idDaImagemNoPonto(10f, 10f, imagens))
        assertNull(SelecaoInteligenteDoRecorteSobreAComposicao.idDaImagemNoPonto(400f, 400f, imagens))
    }

    @Test
    fun toqueNoCentroMoveMesmoComAlcanceMaiorQueASelecao() {
        val tipo = ToqueNasAlcasDoRetanguloDeRecorteDaComposicao.tipo(
            x = 150f,
            y = 100f,
            esquerda = 100f,
            topo = 80f,
            direita = 200f,
            baixo = 120f,
            alcance = 80f,
        )
        assertEquals(AlcaDoRetanguloDeRecorteDaComposicao.MOVER, tipo)
    }

    @Test
    fun toqueNoCantoNoroesteRedimensiona() {
        val tipo = ToqueNasAlcasDoRetanguloDeRecorteDaComposicao.tipo(
            x = 102f,
            y = 81f,
            esquerda = 100f,
            topo = 80f,
            direita = 300f,
            baixo = 220f,
            alcance = 28f,
        )
        assertEquals(AlcaDoRetanguloDeRecorteDaComposicao.NOROESTE, tipo)
    }

    @Test
    fun toqueForaNaoPegaAlca() {
        assertNull(
            ToqueNasAlcasDoRetanguloDeRecorteDaComposicao.tipo(
                x = 10f,
                y = 10f,
                esquerda = 100f,
                topo = 80f,
                direita = 300f,
                baixo = 220f,
                alcance = 28f,
            ),
        )
    }
}
