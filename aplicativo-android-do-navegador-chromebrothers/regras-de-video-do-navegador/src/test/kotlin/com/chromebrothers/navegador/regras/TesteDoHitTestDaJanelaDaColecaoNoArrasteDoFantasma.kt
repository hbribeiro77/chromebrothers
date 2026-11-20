package com.chromebrothers.navegador.regras

import com.chromebrothers.navegador.regras.HitTestDaJanelaDaColecaoNoArrasteDoFantasma.Alvo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoHitTestDaJanelaDaColecaoNoArrasteDoFantasma {
    private val regras = HitTestDaJanelaDaColecaoNoArrasteDoFantasma

    @Test
    fun flutuanteUsaOPainelEAncoradaUsaOConteudoDaGaveta() {
        assertEquals(Alvo.PAINEL_FLUTUANTE, regras.alvo(ancoradaNaGaveta = false))
        assertEquals(Alvo.CONTEUDO_NA_GAVETA, regras.alvo(ancoradaNaGaveta = true))
    }

    @Test
    fun flutuanteExigePainelVisivelComTamanho() {
        assertTrue(
            regras.podeTestar(
                ancoradaNaGaveta = false,
                painelVisivel = true,
                painelLargura = 100,
                painelAltura = 80,
                conteudoVisivel = false,
                conteudoLargura = 0,
                conteudoAltura = 0,
            ),
        )
        assertFalse(
            regras.podeTestar(
                ancoradaNaGaveta = false,
                painelVisivel = false,
                painelLargura = 100,
                painelAltura = 80,
                conteudoVisivel = true,
                conteudoLargura = 200,
                conteudoAltura = 400,
            ),
        )
    }

    @Test
    fun ancoradaTestaOConteudoMesmoComPainelGone() {
        assertTrue(
            regras.podeTestar(
                ancoradaNaGaveta = true,
                painelVisivel = false,
                painelLargura = 0,
                painelAltura = 0,
                conteudoVisivel = true,
                conteudoLargura = 320,
                conteudoAltura = 900,
            ),
        )
        assertFalse(
            regras.podeTestar(
                ancoradaNaGaveta = true,
                painelVisivel = false,
                painelLargura = 0,
                painelAltura = 0,
                conteudoVisivel = false,
                conteudoLargura = 320,
                conteudoAltura = 900,
            ),
        )
    }

    @Test
    fun pontoDentroDoRetanguloDaVista() {
        assertTrue(regras.pontoDentro(x = 150f, y = 200f, esquerda = 100f, topo = 100f, largura = 200, altura = 300))
        assertFalse(regras.pontoDentro(x = 50f, y = 200f, esquerda = 100f, topo = 100f, largura = 200, altura = 300))
        assertFalse(regras.pontoDentro(x = 150f, y = 50f, esquerda = 100f, topo = 100f, largura = 200, altura = 300))
    }
}
