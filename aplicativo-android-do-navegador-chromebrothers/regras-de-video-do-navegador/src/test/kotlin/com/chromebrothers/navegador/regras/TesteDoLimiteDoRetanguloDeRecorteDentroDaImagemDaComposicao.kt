package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDoLimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao {
    @Test
    fun aoMoverContraABordaPreservaTamanhoENaoViraLinha() {
        val limitado = LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
            esquerda = -40f,
            topo = 10f,
            direita = 60f,
            baixo = 90f,
            imagemEsquerda = 0f,
            imagemTopo = 0f,
            imagemLargura = 200f,
            imagemAltura = 100f,
            preservarTamanho = true,
            ladoMinimo = 32f,
        )
        assertEquals(100f, limitado.direita - limitado.esquerda)
        assertEquals(80f, limitado.baixo - limitado.topo)
        assertEquals(0f, limitado.esquerda)
        assertEquals(10f, limitado.topo)
    }

    @Test
    fun aoMoverPassandoDoCantoPreservaTamanhoColadoNaBorda() {
        val limitado = LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
            esquerda = 180f,
            topo = 70f,
            direita = 280f,
            baixo = 150f,
            imagemEsquerda = 0f,
            imagemTopo = 0f,
            imagemLargura = 200f,
            imagemAltura = 100f,
            preservarTamanho = true,
            ladoMinimo = 32f,
        )
        assertEquals(100f, limitado.direita - limitado.esquerda)
        assertEquals(80f, limitado.baixo - limitado.topo)
        assertEquals(100f, limitado.esquerda)
        assertEquals(20f, limitado.topo)
    }

    @Test
    fun aoRedimensionarImpoeLadoMinimo() {
        val limitado = LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
            esquerda = 50f,
            topo = 50f,
            direita = 52f,
            baixo = 51f,
            imagemEsquerda = 0f,
            imagemTopo = 0f,
            imagemLargura = 200f,
            imagemAltura = 200f,
            preservarTamanho = false,
            ladoMinimo = 32f,
        )
        assertTrue(limitado.direita - limitado.esquerda >= 32f)
        assertTrue(limitado.baixo - limitado.topo >= 32f)
    }

    @Test
    fun aoRedimensionarFicaDentroDaImagem() {
        val limitado = LimiteDoRetanguloDeRecorteDentroDaImagemDaComposicao.aplicar(
            esquerda = -10f,
            topo = -5f,
            direita = 250f,
            baixo = 220f,
            imagemEsquerda = 0f,
            imagemTopo = 0f,
            imagemLargura = 200f,
            imagemAltura = 100f,
            preservarTamanho = false,
            ladoMinimo = 32f,
        )
        assertEquals(0f, limitado.esquerda)
        assertEquals(0f, limitado.topo)
        assertEquals(200f, limitado.direita)
        assertEquals(100f, limitado.baixo)
    }
}
