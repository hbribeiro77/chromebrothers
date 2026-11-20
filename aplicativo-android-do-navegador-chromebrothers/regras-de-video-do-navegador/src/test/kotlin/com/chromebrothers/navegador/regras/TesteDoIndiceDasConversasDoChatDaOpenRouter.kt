package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoIndiceDasConversasDoChatDaOpenRouter {
    @Test
    fun criarAtualizarExcluirEBytes() {
        var idx = emptyList<EntradaDoIndiceDaConversaDoChat>()
        idx = IndiceDasConversasDoChatDaOpenRouter.criar(idx, "a", "T1", 10L, 100L)
        assertEquals(1, idx.size)
        assertEquals(100L, IndiceDasConversasDoChatDaOpenRouter.bytesTotais(idx))
        idx = IndiceDasConversasDoChatDaOpenRouter.atualizar(idx, "a", atualizadaEm = 20L, bytes = 200L, resumida = true)
        assertEquals(200L, idx.single().bytesAproximados)
        assertTrue(idx.single().resumida)
        idx = IndiceDasConversasDoChatDaOpenRouter.excluir(idx, "a")
        assertTrue(idx.isEmpty())
    }

    @Test
    fun roundtripJsonDoIndice() {
        val original = listOf(EntradaDoIndiceDaConversaDoChat("id1", "Oi", 1L, 2L, 3L, false))
        val json = IndiceDasConversasDoChatDaOpenRouter.escrever(original)
        assertEquals(original, IndiceDasConversasDoChatDaOpenRouter.ler(json))
    }

    @Test
    fun criarColocaANovaConversaNoInicio() {
        var idx = IndiceDasConversasDoChatDaOpenRouter.criar(emptyList(), "a", "A", 1L, 10L)
        idx = IndiceDasConversasDoChatDaOpenRouter.criar(idx, "b", "B", 2L, 20L)
        assertEquals(listOf("b", "a"), idx.map { it.id })
        assertEquals("B", idx.first().titulo)
        assertEquals(2L, idx.first().criadaEm)
        assertEquals(2L, idx.first().atualizadaEm)
        assertFalse(idx.first().resumida)
        assertEquals(30L, IndiceDasConversasDoChatDaOpenRouter.bytesTotais(idx))
    }

    @Test
    fun atualizarMantemTituloCriadaEmEResumidaQuandoOmitidos() {
        val idx = IndiceDasConversasDoChatDaOpenRouter.criar(emptyList(), "a", "T1", 10L, 100L)
        val atualizado = IndiceDasConversasDoChatDaOpenRouter.atualizar(
            idx,
            "a",
            atualizadaEm = 20L,
            bytes = 200L,
        )
        assertEquals("T1", atualizado.single().titulo)
        assertEquals(10L, atualizado.single().criadaEm)
        assertEquals(20L, atualizado.single().atualizadaEm)
        assertEquals(false, atualizado.single().resumida)
        val ausente = IndiceDasConversasDoChatDaOpenRouter.atualizar(
            idx,
            "z",
            atualizadaEm = 99L,
            bytes = 1L,
        )
        assertEquals(idx, ausente)
    }

    @Test
    fun excluirRemoveSoOIdPedido() {
        var idx = IndiceDasConversasDoChatDaOpenRouter.criar(emptyList(), "a", "A", 1L, 10L)
        idx = IndiceDasConversasDoChatDaOpenRouter.criar(idx, "b", "B", 2L, 20L)
        idx = IndiceDasConversasDoChatDaOpenRouter.excluir(idx, "a")
        assertEquals(listOf("b"), idx.map { it.id })
        assertEquals(20L, IndiceDasConversasDoChatDaOpenRouter.bytesTotais(idx))
    }

    @Test
    fun escreverUsaAChaveConversasEEscapaTitulo() {
        val original = listOf(
            EntradaDoIndiceDaConversaDoChat("id\"1", "Oi\n\"mundo\"", 1L, 2L, 3L, true),
        )
        val json = IndiceDasConversasDoChatDaOpenRouter.escrever(original)
        assertTrue(json.contains("\"conversas\""))
        assertEquals(original, IndiceDasConversasDoChatDaOpenRouter.ler(json))
    }

    @Test
    fun roundtripComInstanteEBytesMaioresQueInt() {
        val original = listOf(
            EntradaDoIndiceDaConversaDoChat(
                id = "id",
                titulo = "t",
                criadaEm = Int.MAX_VALUE.toLong() + 10L,
                atualizadaEm = 50L * 1024 * 1024,
                bytesAproximados = 200L * 1024 * 1024,
                resumida = true,
            ),
        )
        assertEquals(
            original,
            IndiceDasConversasDoChatDaOpenRouter.ler(
                IndiceDasConversasDoChatDaOpenRouter.escrever(original),
            ),
        )
    }

    @Test
    fun jsonInvalidoDoIndiceDevolveListaVazia() {
        assertEquals(emptyList(), IndiceDasConversasDoChatDaOpenRouter.ler("{}"))
        assertEquals(emptyList(), IndiceDasConversasDoChatDaOpenRouter.ler("lixo"))
    }

    @Test
    fun jsonTruncadoSemFecharOObjetoRaizDevolveListaVazia() {
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler("{\"conversas\":[{\"id\":\"x\",\"titulo\":\"T\"}]"),
        )
    }

    @Test
    fun objetoSemIdOuComIdEmBrancoInvalidaODocumentoInteiro() {
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler("""{"conversas":[{}]}"""),
        )
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler("""{"conversas":[{"id":"","titulo":"T"}]}"""),
        )
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler("""{"conversas":[{"id":"   ","titulo":"T"}]}"""),
        )
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler("""{"conversas":[{"id":"x"}]}"""),
        )
        assertEquals(
            emptyList(),
            IndiceDasConversasDoChatDaOpenRouter.ler(
                """{"conversas":[{"id":"ok","titulo":"A","criadaEm":1,"atualizadaEm":2,"bytesAproximados":3},{}]}""",
            ),
        )
        assertEquals(
            listOf(EntradaDoIndiceDaConversaDoChat("x", "T", 1L, 2L, 3L)),
            IndiceDasConversasDoChatDaOpenRouter.ler(
                """{"conversas":[{"id":"x","titulo":"T","criadaEm":1,"atualizadaEm":2,"bytesAproximados":3}]}""",
            ),
        )
    }

    @Test
    fun tetosDeQuantidadeBytesTotaisEConversa() {
        assertEquals(40, IndiceDasConversasDoChatDaOpenRouter.MAXIMO_DE_CONVERSAS)
        assertEquals(200L * 1024 * 1024, IndiceDasConversasDoChatDaOpenRouter.TETO_DE_BYTES_TOTAIS)
        assertEquals(50L * 1024 * 1024, IndiceDasConversasDoChatDaOpenRouter.TETO_DURO_POR_CONVERSA)

        val noMaximo = List(IndiceDasConversasDoChatDaOpenRouter.MAXIMO_DE_CONVERSAS) { indice ->
            EntradaDoIndiceDaConversaDoChat("id$indice", "t", 1L, 1L, 1L)
        }
        assertTrue(IndiceDasConversasDoChatDaOpenRouter.ultrapassouQuantidade(noMaximo))
        assertFalse(IndiceDasConversasDoChatDaOpenRouter.ultrapassouQuantidade(noMaximo.drop(1)))

        val tetoTotal = IndiceDasConversasDoChatDaOpenRouter.TETO_DE_BYTES_TOTAIS
        val cheio = listOf(EntradaDoIndiceDaConversaDoChat("x", "t", 1L, 1L, tetoTotal))
        val quase = listOf(EntradaDoIndiceDaConversaDoChat("x", "t", 1L, 1L, tetoTotal - 1L))
        assertTrue(IndiceDasConversasDoChatDaOpenRouter.ultrapassouBytesTotais(cheio))
        assertFalse(IndiceDasConversasDoChatDaOpenRouter.ultrapassouBytesTotais(quase))

        val tetoDuro = IndiceDasConversasDoChatDaOpenRouter.TETO_DURO_POR_CONVERSA
        assertFalse(IndiceDasConversasDoChatDaOpenRouter.ultrapassouTetoDuroDaConversa(tetoDuro))
        assertTrue(IndiceDasConversasDoChatDaOpenRouter.ultrapassouTetoDuroDaConversa(tetoDuro + 1L))
    }
}
