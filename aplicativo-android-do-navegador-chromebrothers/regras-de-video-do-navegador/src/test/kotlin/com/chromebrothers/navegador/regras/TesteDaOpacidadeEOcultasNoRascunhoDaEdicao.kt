package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TesteDaOpacidadeEOcultasNoRascunhoDaEdicao {
    private fun documento(opacidades: Map<Int, Float>, ocultas: Set<Int>) = DocumentoDoRascunhoDaEdicaoDeImagem(
        largura = 400,
        altura = 300,
        proximoId = 4,
        ferramenta = FerramentaGravadaDoRascunhoDaEdicao.VERMELHO,
        imagens = emptyList(),
        textos = emptyList(),
        tracos = emptyList(),
        camadasVazias = listOf(CamadaVaziaDoRascunhoDaEdicao(2, 0f, 0f, 400f, 300f), CamadaVaziaDoRascunhoDaEdicao(3, 0f, 0f, 400f, 300f)),
        opacidades = opacidades,
        ocultas = ocultas,
    )

    @Test
    fun opacidadeECamadaOcultaVoltamIguais() {
        val texto = TextoDoRascunhoDaEdicaoDeImagem.escrever(documento(mapOf(2 to 0.35f, 3 to 1f), setOf(3)))
        assertTrue(texto.contains("opacidade 2 0.3500"))
        assertTrue(!texto.contains("opacidade 3"))
        assertTrue(texto.contains("oculta 3"))
        val lido = assertNotNull(TextoDoRascunhoDaEdicaoDeImagem.ler(texto))
        assertEquals(mapOf(2 to 0.35f), lido.opacidades)
        assertEquals(setOf(3), lido.ocultas)
    }

    @Test
    fun rascunhoAntigoSemAsLinhasNovasAbreTudoOpacoEVisivel() {
        val antigo = "v1\nmoldura 400 300\nproximo 3\nferramenta VERMELHO\nfolha -1\ncamada 2 0.0000 0.0000 400.0000 300.0000"
        val lido = assertNotNull(TextoDoRascunhoDaEdicaoDeImagem.ler(antigo))
        assertTrue(lido.opacidades.isEmpty())
        assertTrue(lido.ocultas.isEmpty())
    }
}
