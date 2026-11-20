package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaPosicaoDaJanelaDoCampoDeTexto {
    @Test
    fun aJanelaParaNaBordaDaTela() {
        val canto = PosicaoDaJanelaDoCampoDeTexto.limitar(
            x = -20f,
            y = 500f,
            largura = 100f,
            altura = 80f,
            limiteLargura = 400f,
            limiteAltura = 300f,
        )
        assertEquals(0f, canto.first)
        assertEquals(220f, canto.second)
    }

    @Test
    fun aLarguraParaNaBordaENoMinimo() {
        assertEquals(
            400f,
            PosicaoDaJanelaDoCampoDeTexto.largura(
                atual = 360f,
                delta = 80f,
                esquerda = 100f,
                minimo = 280f,
                limiteLargura = 500f,
            ),
        )
        assertEquals(
            280f,
            PosicaoDaJanelaDoCampoDeTexto.largura(
                atual = 320f,
                delta = -80f,
                esquerda = 0f,
                minimo = 280f,
                limiteLargura = 800f,
            ),
        )
    }

    @Test
    fun aAlturaParaNaBordaENoMinimo() {
        assertEquals(
            240f,
            PosicaoDaJanelaDoCampoDeTexto.altura(
                atual = 200f,
                delta = 80f,
                topo = 60f,
                minimo = 160f,
                limiteAltura = 300f,
            ),
        )
        assertEquals(
            160f,
            PosicaoDaJanelaDoCampoDeTexto.altura(
                atual = 200f,
                delta = -80f,
                topo = 0f,
                minimo = 160f,
                limiteAltura = 600f,
            ),
        )
    }

    @Test
    fun oTecladoLevaAJanelaParaABarraQuandoCobre() {
        assertEquals(
            80f,
            PosicaoDaJanelaDoCampoDeTexto.topoSeOTecladoCobrir(
                topoAtual = 400f,
                altura = 200f,
                topoDaBarra = 80f,
                limiteAltura = 500f,
            ),
        )
        assertEquals(
            40f,
            PosicaoDaJanelaDoCampoDeTexto.topoSeOTecladoCobrir(
                topoAtual = 40f,
                altura = 100f,
                topoDaBarra = 80f,
                limiteAltura = 500f,
            ),
        )
    }

    @Test
    fun aAlturaCabeEntreABarraEOTeclado() {
        assertEquals(
            300f,
            PosicaoDaJanelaDoCampoDeTexto.alturaEntreABarraEOTeclado(
                desejada = 460f,
                topoDaBarra = 100f,
                limiteAltura = 400f,
                minima = 200f,
            ),
        )
        assertEquals(
            200f,
            PosicaoDaJanelaDoCampoDeTexto.alturaEntreABarraEOTeclado(
                desejada = 200f,
                topoDaBarra = 100f,
                limiteAltura = 800f,
                minima = 160f,
            ),
        )
    }
}
