package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoWarmStartDeComponentesDaColecaoDoNavegador {
    private val regra = WarmStartDeComponentesDaColecaoDoNavegador

    private fun item(
        id: String,
        nome: String = id,
        tags: List<String> = emptyList(),
    ) = ItemDaBibliotecaDeComponentesHtmlDaColecao(
        id = id,
        nome = nome,
        html = "<p>x</p>",
        atualizadoEm = 1L,
        tags = tags,
    )

    @Test
    fun mcpPorIdSempreMarcado() {
        assertTrue(regra.marcadoNoComponente(item("geracao-mcp-imagem")))
        assertTrue(regra.marcadoNoComponente(item("geracao-mcp-video")))
    }

    @Test
    fun visualizadorPeloNome() {
        assertTrue(regra.marcadoNoComponente(item("abc", nome = "Visualizador de mídia")))
        assertTrue(regra.marcadoNoComponente(item("abc", nome = "visualizador de midia — v2")))
    }

    @Test
    fun tagWarmStart() {
        assertTrue(regra.marcadoNoComponente(item("outro", tags = listOf("warm-start"))))
        assertFalse(regra.marcadoNoComponente(item("outro", tags = listOf("galeria"))))
    }

    @Test
    fun preferenciaGlobalDesligadaNaoPreCarrega() {
        val itens = listOf(item("geracao-mcp-imagem"))
        assertTrue(regra.filtrarParaPreCarga(itens, preferenciaGlobalLigada = true).isNotEmpty())
        assertEquals(emptyList(), regra.filtrarParaPreCarga(itens, preferenciaGlobalLigada = false))
        assertFalse(regra.devePreCarregar(itens.first(), preferenciaGlobalLigada = false))
    }
}
