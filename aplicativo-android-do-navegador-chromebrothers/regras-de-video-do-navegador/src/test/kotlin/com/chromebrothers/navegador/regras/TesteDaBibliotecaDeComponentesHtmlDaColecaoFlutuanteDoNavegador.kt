package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaBibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador {
    @Test
    fun guardarCriaNoTopoComNomeEHtmlAparados() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        val resultado = biblioteca.guardar(
            nome = "  Nota  ",
            html = "  <p>Oi</p>  ",
            idExistente = null,
            agora = 10L,
            novoId = { "id-1" },
            descricao = "  um cartão  ",
            tags = listOf(" aula ", "", "pdf"),
        ) as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertTrue(resultado.criou)
        assertEquals("id-1", resultado.item.id)
        assertEquals("Nota", resultado.item.nome)
        assertEquals("<p>Oi</p>", resultado.item.html)
        assertEquals(10L, resultado.item.atualizadoEm)
        assertEquals("um cartão", resultado.item.descricao)
        assertEquals(listOf("aula", "pdf"), resultado.item.tags)
        assertEquals(listOf("id-1"), biblioteca.listar().map { it.id })
        assertEquals(resultado.item, biblioteca.buscar("id-1"))
    }

    @Test
    fun componenteMaisNovoFicaNaFrente() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Antiga", "<p>a</p>", null, 1L) { "antiga" }
        biblioteca.guardar("Nova", "<p>b</p>", null, 2L) { "nova" }
        assertEquals(listOf("nova", "antiga"), biblioteca.listar().map { it.id })
    }

    @Test
    fun atualizarPorIdSubstituiConteudoEMoveParaOTopo() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar(
            "Velha",
            "<p>1</p>",
            null,
            1L,
            "nota antiga",
            listOf("a"),
        ) { "pag-1" }
        biblioteca.guardar("Outra", "<p>2</p>", null, 2L) { "pag-2" }
        var chamouNovoId = false
        val resultado = biblioteca.guardar(
            nome = " Nova ",
            html = " <p>3</p> ",
            idExistente = "pag-1",
            agora = 9L,
            novoId = {
                chamouNovoId = true
                "nao-usar"
            },
        ) as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertFalse(resultado.criou)
        assertFalse(chamouNovoId)
        assertEquals("pag-1", resultado.item.id)
        assertEquals("Nova", resultado.item.nome)
        assertEquals("<p>3</p>", resultado.item.html)
        assertEquals(9L, resultado.item.atualizadoEm)
        assertEquals("nota antiga", resultado.item.descricao)
        assertEquals(listOf("a"), resultado.item.tags)
        assertEquals(listOf("pag-1", "pag-2"), biblioteca.listar().map { it.id })
    }

    @Test
    fun atualizarPodeTrocarDescricaoELimparTags() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Velha", "<p>1</p>", null, 1L, "antes", listOf("a")) { "pag-1" }
        val resultado = biblioteca.guardar(
            nome = "Velha",
            html = "<p>1</p>",
            idExistente = "pag-1",
            agora = 2L,
            novoId = { "nao" },
            descricao = "",
            tags = emptyList(),
        ) as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertEquals("", resultado.item.descricao)
        assertEquals(emptyList(), resultado.item.tags)
    }

    @Test
    fun idInexistenteCriaItemNovo() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        val resultado = biblioteca.guardar("T", "<p>x</p>", "sumiu", 3L) { "gerado" }
            as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertTrue(resultado.criou)
        assertEquals("gerado", resultado.item.id)
        assertNull(biblioteca.buscar("sumiu"))
    }

    @Test
    fun criacaoPassaDeQuarentaItens() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        repeat(41) { indice ->
            val ok = biblioteca.guardar("T$indice", "<p>$indice</p>", null, indice.toLong()) { "id-$indice" }
            assertTrue(ok is ResultadoDaBibliotecaDeComponentesHtml.Ok, "criação $indice")
        }
        assertEquals(41, biblioteca.listar().size)
        assertEquals("id-40", biblioteca.buscar("id-40")!!.id)
    }

    @Test
    fun nomeHtmlDescricaoETagsInvalidosSaoRecusadosEOsLimitesExatosPassam() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        var ids = 0
        fun recusa(
            nome: String,
            html: String,
            descricao: String? = null,
            tags: List<String>? = null,
        ): String {
            val resultado = biblioteca.guardar(nome, html, null, 1L, descricao, tags) {
                ids += 1
                "id-$ids"
            }
            return (resultado as ResultadoDaBibliotecaDeComponentesHtml.Recusada).mensagem
        }
        assertEquals("Faltou o nome.", recusa("   ", "<p>a</p>"))
        assertEquals("Faltou o nome.", recusa("", "<p>a</p>"))
        assertEquals(
            "O nome passou do limite.",
            recusa("a".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME + 1), "<p>a</p>"),
        )
        assertEquals("Faltou o HTML.", recusa("Ok", "  \n  "))
        assertEquals("Faltou o HTML.", recusa("Ok", ""))
        assertEquals(
            "O HTML passou do limite.",
            recusa("Ok", "h".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_HTML + 1)),
        )
        assertEquals(
            "A descrição passou do limite.",
            recusa("Ok", "<p>a</p>", "d".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DA_DESCRICAO + 1)),
        )
        assertEquals(
            "As tags passaram do limite.",
            recusa("Ok", "<p>a</p>", tags = List(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DE_TAGS + 1) { "t$it" }),
        )
        assertEquals(
            "Uma tag passou do limite.",
            recusa(
                "Ok",
                "<p>a</p>",
                tags = listOf("t".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_TAMANHO_DA_TAG + 1)),
            ),
        )
        assertEquals(0, ids)
        assertTrue(biblioteca.listar().isEmpty())
        val noLimite = biblioteca.guardar(
            "t".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME),
            "h".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_HTML),
            null,
            2L,
            "d".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DA_DESCRICAO),
            List(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DE_TAGS) {
                "g".repeat(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_TAMANHO_DA_TAG)
            },
        ) { "cabe" } as ResultadoDaBibliotecaDeComponentesHtml.Ok
        assertTrue(noLimite.criou)
        assertEquals(120, noLimite.item.nome.length)
        assertEquals(500, noLimite.item.descricao.length)
        assertEquals(8, noLimite.item.tags.size)
        assertEquals(40, noLimite.item.tags.first().length)
        assertEquals(BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_HTML, noLimite.item.html.length)
        assertEquals(10 * 1024 * 1024, BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_HTML)
        assertEquals(120, BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_NOME)
    }

    @Test
    fun apagarRemoveOItemEDevolveFalsoSeNaoExistir() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("T", "<p>a</p>", null, 1L) { "a" }
        biblioteca.guardar("U", "<p>b</p>", null, 2L) { "b" }
        assertFalse(biblioteca.apagar("nao"))
        assertTrue(biblioteca.apagar("a"))
        assertNull(biblioteca.buscar("a"))
        assertEquals(listOf("b"), biblioteca.listar().map { it.id })
        assertFalse(biblioteca.apagar("a"))
    }

    @Test
    fun buscarPorNomeIgnoraEspacoECaixaEDevolveOMaisRecente() {
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Nota", "<p>1</p>", null, 1L) { "velha" }
        biblioteca.guardar("Outra", "<p>2</p>", null, 2L) { "meio" }
        biblioteca.guardar("nota", "<p>3</p>", null, 3L) { "nova" }
        assertEquals("nova", biblioteca.buscarPorNome("  NOTA ")!!.id)
        assertEquals("<p>3</p>", biblioteca.buscarPorNome("nota")!!.html)
        assertEquals("meio", biblioteca.buscarPorNome("outra")!!.id)
        assertNull(biblioteca.buscarPorNome("ausente"))
        assertNull(biblioteca.buscar("ausente"))
    }

    @Test
    fun substituirPorTrocaAListaMantendoAOrdemRecebida() {
        val inicial = ItemDaBibliotecaDeComponentesHtmlDaColecao("antigo", "A", "<p>a</p>", 1L)
        val biblioteca = BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador(listOf(inicial))
        assertEquals(listOf("antigo"), biblioteca.listar().map { it.id })
        biblioteca.substituirPor(
            listOf(
                ItemDaBibliotecaDeComponentesHtmlDaColecao("b", "B", "<p>b</p>", 3L),
                ItemDaBibliotecaDeComponentesHtmlDaColecao("a", "A", "<p>a</p>", 2L),
            ),
        )
        assertEquals(listOf("b", "a"), biblioteca.listar().map { it.id })
        assertNull(biblioteca.buscar("antigo"))
        assertEquals("B", biblioteca.buscarPorNome("b")!!.nome)
    }
}
