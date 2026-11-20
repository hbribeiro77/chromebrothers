package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TesteDaEquivalenciaDeModelosDeVideoEntreMcps {
    @Test
    fun intersecaoDosTresTemWan3EKling() {
        val comuns = EquivalenciaDeModelosDeVideoEntreMcps.intersecao(
            listOf("higgsfield", "sjinn", "topview"),
        )
        assertTrue(comuns.any { it.first == "wan3" })
        assertTrue(comuns.any { it.first == "kling-v3" })
        // Sjinn passou a ter Seedance 2.0 e Kling 4.0 (aceitam vídeo de referência).
        assertEquals(setOf("wan3", "seedance-2-0", "kling-4", "kling-v3"), comuns.map { it.first }.toSet())
        assertEquals("wan3", EquivalenciaDeModelosDeVideoEntreMcps.familiaPadraoPara(listOf("sjinn", "topview", "higgsfield")))
    }

    @Test
    fun intersecaoSjinnTopviewTemVariosComuns() {
        val comuns = EquivalenciaDeModelosDeVideoEntreMcps.intersecao(listOf("sjinn", "topview"))
        assertTrue(comuns.map { it.first }.containsAll(listOf("wan3", "seedance-2-5", "kling-v3", "gemini-omni")))
    }

    @Test
    fun intersecaoHiggsfieldTopviewTemWanESeedance() {
        val ids = EquivalenciaDeModelosDeVideoEntreMcps.intersecao(listOf("higgsfield", "topview")).map { it.first }
        assertTrue(ids.containsAll(listOf("wan3", "wan3-prime", "wan-2-7", "seedance-2-0", "kling-v3")))
    }

    @Test
    fun mapeiaIdsLocais() {
        assertEquals("wan3_0", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("wan3", "higgsfield"))
        assertEquals("wan3", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("wan3", "sjinn"))
        assertEquals("wan-3-0", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("wan3", "topview"))
        assertEquals("kling3_0", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("kling-v3", "higgsfield"))
        assertEquals("kling3", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("kling-v3", "sjinn"))
        assertEquals("kling-v3", EquivalenciaDeModelosDeVideoEntreMcps.idParaMcp("kling-v3", "topview"))
    }

    @Test
    fun pedidoAceitaArrayMcps() {
        val ok = PedidoDeGeracaoDeVideoViaMcpNaColecao.ler(
            """{"mcps":["sjinn","topview"],"prompt":"onda","modelo":"wan3","quantidade":2}""",
        )
        assertIs<ResultadoDoPedidoDeGeracaoDeVideoViaMcp.Ok>(ok)
        assertEquals(listOf("sjinn", "topview"), ok.pedido.mcps)
        assertEquals(2, ok.pedido.quantidade)
        assertEquals("wan3", ok.pedido.modelo)
    }
}
