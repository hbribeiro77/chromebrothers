package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaLeituraDeMidiaEEnderecosDaPaginaParaOChat {
    @Test
    fun aMidiaPedidaListaOArquivoEDeixaOPedacoDeFora() {
        val texto = TextoDaMidiaObservadaParaOChat.descrever(
            listOf(
                PedidoDeMidiaObservado(
                    "https://cdn.exemplo/aula.mp4",
                    TipoDePedidoDeMidiaParaDownload.ARQUIVO,
                    ordem = 2,
                    tamanho = 8_000_000,
                ),
                PedidoDeMidiaObservado(
                    "https://cdn.exemplo/pedaco.ts",
                    TipoDePedidoDeMidiaParaDownload.TRECHO,
                    ordem = 1,
                ),
            ),
        )
        assertTrue(texto.contains("- video https://cdn.exemplo/aula.mp4"))
        assertFalse(texto.contains("pedaco.ts"))
        assertEquals(
            "Nenhuma mídia encontrada nesta página.",
            TextoDaMidiaObservadaParaOChat.relato("", ""),
        )
    }

    @Test
    fun osEnderecosVisiveisGuardamHttpEDescartamOResto() {
        val bruto = """
            imagem	https://cdn.exemplo/capa.jpg	Wan3 Video
            imagem	https://cdn.exemplo/capa.jpg	de novo
            video	javascript:alert(1)	ruim
            link	blob:https://site/abc	oculto
            link	https://cdn.exemplo/arquivo	sem extensao
        """.trimIndent()
        val itens = EnderecosVisiveisDaPaginaParaOChat.ler(bruto)
        assertEquals(2, itens.size)
        assertEquals("imagem", itens[0].tipo)
        assertEquals("Wan3 Video", itens[0].rotulo)
        assertEquals("link", itens[1].tipo)
        val midia = EnderecosVisiveisDaPaginaParaOChat.descreverMidia(bruto)
        assertTrue(midia.contains("capa.jpg"))
        assertFalse(midia.contains("arquivo"))
        val enderecos = EnderecosVisiveisDaPaginaParaOChat.descreverEnderecos(bruto)
        assertTrue(enderecos.contains("https://cdn.exemplo/arquivo"))
    }

    @Test
    fun asDuasLeiturasEntramNaInstrucaoNestaOrdem() {
        val corpo = FerramentasDeLeituraDoChatDaOpenRouter.json()
        assertTrue(corpo.contains("listar_midia_da_pagina"))
        assertTrue(corpo.contains("ler_enderecos_da_pagina"))
        assertFalse(corpo.contains("navegar"))
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn")
        val midia = texto.indexOf("leia primeiro a mídia")
        val enderecos = texto.indexOf("endereços da página")
        assertTrue(midia >= 0 && enderecos > midia)
        assertEquals(
            "Lendo a mídia da página…",
            FerramentasDeLeituraDoChatDaOpenRouter.rotuloEmAndamento(FerramentasDeLeituraDoChatDaOpenRouter.MIDIA),
        )
        assertTrue(EnderecosVisiveisDaPaginaParaOChat.scriptDaMidia().contains("currentSrc"))
        assertTrue(EnderecosVisiveisDaPaginaParaOChat.scriptDosEnderecos().contains("href"))
    }
}
