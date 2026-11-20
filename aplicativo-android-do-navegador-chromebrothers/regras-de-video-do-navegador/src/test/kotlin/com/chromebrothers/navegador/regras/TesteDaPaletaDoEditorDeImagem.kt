package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaPaletaDoEditorDeImagem {
    @Test
    fun aPaletaServeParaAFolhaOTracoEOTexto() {
        assertEquals(
            listOf(
                PaletaDoEditorDeImagem.BRANCO,
                PaletaDoEditorDeImagem.PRETO,
                PaletaDoEditorDeImagem.VERMELHO,
                PaletaDoEditorDeImagem.AZUL,
                PaletaDoEditorDeImagem.AMARELO,
                PaletaDoEditorDeImagem.VERDE,
            ),
            PaletaDoEditorDeImagem.cores.toList(),
        )
        assertEquals(PaletaDoEditorDeImagem.BRANCO, PaletaDoEditorDeImagem.corDaFolhaInicial)
        assertEquals(2, PaletaDoEditorDeImagem.indice(PaletaDoEditorDeImagem.VERMELHO))
        assertEquals(2, PaletaDoEditorDeImagem.indice(0x00E53935))
        assertEquals(2, PaletaDoEditorDeImagem.posicao(PaletaDoEditorDeImagem.VERMELHO))
        assertEquals(-1, PaletaDoEditorDeImagem.posicao(0xFF112233.toInt()))
    }

    @Test
    fun oTracoMarcadorFicaEmQuarentaPorCento() {
        val marcado = PaletaDoEditorDeImagem.comOpacidade(PaletaDoEditorDeImagem.AZUL, marcador = true)
        val solido = PaletaDoEditorDeImagem.comOpacidade(PaletaDoEditorDeImagem.AZUL, marcador = false)
        assertEquals(102, marcado ushr 24)
        assertEquals(PaletaDoEditorDeImagem.AZUL and 0x00FFFFFF, marcado and 0x00FFFFFF)
        assertEquals(255, solido ushr 24)
    }
}
