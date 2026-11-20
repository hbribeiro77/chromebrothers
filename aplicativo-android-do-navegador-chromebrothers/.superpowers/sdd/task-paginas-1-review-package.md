# Task 1 pages

## FILE: regras-de-video-do-navegador\src\main\kotlin\com\chromebrothers\navegador\regras\BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt
```kotlin
package com.chromebrothers.navegador.regras

data class ItemDaBibliotecaDePaginasHtmlDaColecao(
    val id: String,
    val titulo: String,
    val html: String,
    val atualizadoEm: Long,
)

sealed class ResultadoDaBibliotecaDePaginasHtml {
    data class Ok(
        val item: ItemDaBibliotecaDePaginasHtmlDaColecao,
        val criou: Boolean,
    ) : ResultadoDaBibliotecaDePaginasHtml()

    data class Recusada(val mensagem: String) : ResultadoDaBibliotecaDePaginasHtml()
}

class BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador(
    inicial: List<ItemDaBibliotecaDePaginasHtmlDaColecao> = emptyList(),
    private val limite: Int = LIMITE_DE_ITENS,
) {
    private val itens = inicial.toMutableList()

    fun listar(): List<ItemDaBibliotecaDePaginasHtmlDaColecao> = itens.toList()

    fun buscar(id: String): ItemDaBibliotecaDePaginasHtmlDaColecao? = itens.find { it.id == id }

    fun buscarPorTitulo(titulo: String): ItemDaBibliotecaDePaginasHtmlDaColecao? {
        val alvo = titulo.trim()
        return itens.find { it.titulo.equals(alvo, ignoreCase = true) }
    }

    fun guardar(
        titulo: String,
        html: String,
        idExistente: String?,
        agora: Long,
        novoId: () -> String,
    ): ResultadoDaBibliotecaDePaginasHtml {
        val tituloLimpo = titulo.trim()
        val htmlLimpo = html.trim()
        val recusa = when {
            tituloLimpo.isEmpty() -> "Faltou o título."
            tituloLimpo.length > LIMITE_DO_TITULO -> "O título passou do limite."
            htmlLimpo.isEmpty() -> "Faltou o HTML."
            htmlLimpo.length > LIMITE_DO_HTML -> "O HTML passou do limite."
            else -> ""
        }
        if (recusa.isNotEmpty()) return ResultadoDaBibliotecaDePaginasHtml.Recusada(recusa)

        val indice = if (idExistente == null) -1 else itens.indexOfFirst { it.id == idExistente }
        if (indice >= 0) {
            val atualizado = itens[indice].copy(
                titulo = tituloLimpo,
                html = htmlLimpo,
                atualizadoEm = agora,
            )
            itens.removeAt(indice)
            itens.add(0, atualizado)
            return ResultadoDaBibliotecaDePaginasHtml.Ok(atualizado, criou = false)
        }
        if (itens.size >= limite) {
            return ResultadoDaBibliotecaDePaginasHtml.Recusada("A biblioteca de páginas chegou ao limite.")
        }
        val criado = ItemDaBibliotecaDePaginasHtmlDaColecao(
            id = novoId(),
            titulo = tituloLimpo,
            html = htmlLimpo,
            atualizadoEm = agora,
        )
        itens.add(0, criado)
        return ResultadoDaBibliotecaDePaginasHtml.Ok(criado, criou = true)
    }

    fun apagar(id: String): Boolean = itens.removeAll { it.id == id }

    fun substituirPor(novos: List<ItemDaBibliotecaDePaginasHtmlDaColecao>) {
        itens.clear()
        itens.addAll(novos)
    }

    companion object {
        const val LIMITE_DO_TITULO = 120
        const val LIMITE_DO_HTML = 300_000
        const val LIMITE_DE_ITENS = 40
    }
}

```

## FILE: regras-de-video-do-navegador\src\test\kotlin\com\chromebrothers\navegador\regras\TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt
```kotlin
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador {
    @Test
    fun guardarCriaNoTopoComTituloEHtmlAparados() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        val resultado = biblioteca.guardar(
            titulo = "  Nota  ",
            html = "  <p>Oi</p>  ",
            idExistente = null,
            agora = 10L,
            novoId = { "id-1" },
        ) as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertTrue(resultado.criou)
        assertEquals("id-1", resultado.item.id)
        assertEquals("Nota", resultado.item.titulo)
        assertEquals("<p>Oi</p>", resultado.item.html)
        assertEquals(10L, resultado.item.atualizadoEm)
        assertEquals(listOf("id-1"), biblioteca.listar().map { it.id })
        assertEquals(resultado.item, biblioteca.buscar("id-1"))
    }

    @Test
    fun paginaMaisNovaFicaNaFrente() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Antiga", "<p>a</p>", null, 1L) { "antiga" }
        biblioteca.guardar("Nova", "<p>b</p>", null, 2L) { "nova" }
        assertEquals(listOf("nova", "antiga"), biblioteca.listar().map { it.id })
    }

    @Test
    fun atualizarPorIdSubstituiConteudoEMoveParaOTopo() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Velha", "<p>1</p>", null, 1L) { "pag-1" }
        biblioteca.guardar("Outra", "<p>2</p>", null, 2L) { "pag-2" }
        var chamouNovoId = false
        val resultado = biblioteca.guardar(
            titulo = " Nova ",
            html = " <p>3</p> ",
            idExistente = "pag-1",
            agora = 9L,
            novoId = {
                chamouNovoId = true
                "nao-usar"
            },
        ) as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertFalse(resultado.criou)
        assertFalse(chamouNovoId)
        assertEquals("pag-1", resultado.item.id)
        assertEquals("Nova", resultado.item.titulo)
        assertEquals("<p>3</p>", resultado.item.html)
        assertEquals(9L, resultado.item.atualizadoEm)
        assertEquals(listOf("pag-1", "pag-2"), biblioteca.listar().map { it.id })
    }

    @Test
    fun idInexistenteCriaItemNovo() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        val resultado = biblioteca.guardar("T", "<p>x</p>", "sumiu", 3L) { "gerado" }
            as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertTrue(resultado.criou)
        assertEquals("gerado", resultado.item.id)
        assertNull(biblioteca.buscar("sumiu"))
    }

    @Test
    fun aQuarentaEUmaCriacaoERecusadaEAtualizarAindaCabe() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        repeat(40) { indice ->
            val ok = biblioteca.guardar("T$indice", "<p>$indice</p>", null, indice.toLong()) { "id-$indice" }
            assertTrue(ok is ResultadoDaBibliotecaDePaginasHtml.Ok, "criação $indice")
        }
        assertEquals(40, biblioteca.listar().size)
        val extra = biblioteca.guardar("Mais", "<p>x</p>", null, 100L) { "id-extra" }
        assertEquals(
            "A biblioteca de páginas chegou ao limite.",
            (extra as ResultadoDaBibliotecaDePaginasHtml.Recusada).mensagem,
        )
        assertEquals(40, biblioteca.listar().size)
        assertNull(biblioteca.buscar("id-extra"))
        val atualizada = biblioteca.guardar("T0 novo", "<p>novo</p>", "id-0", 200L) { "nao" }
            as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertFalse(atualizada.criou)
        assertEquals("id-0", biblioteca.listar().first().id)
        assertEquals("T0 novo", atualizada.item.titulo)
        assertEquals(40, biblioteca.listar().size)
    }

    @Test
    fun limiteInformadoRecusaCriacaoEPermiteAtualizar() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador(limite = 2)
        biblioteca.guardar("A", "<p>a</p>", null, 1L) { "a" }
        biblioteca.guardar("B", "<p>b</p>", null, 2L) { "b" }
        val extra = biblioteca.guardar("C", "<p>c</p>", null, 3L) { "c" }
        assertEquals(
            "A biblioteca de páginas chegou ao limite.",
            (extra as ResultadoDaBibliotecaDePaginasHtml.Recusada).mensagem,
        )
        val atualizada = biblioteca.guardar("B2", "<p>b2</p>", "b", 4L) { "nao" }
            as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertFalse(atualizada.criou)
        assertEquals(listOf("b", "a"), biblioteca.listar().map { it.id })
    }

    @Test
    fun tituloEHtmlInvalidosSaoRecusadosEOsLimitesExatosPassam() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        var ids = 0
        fun recusa(titulo: String, html: String): String {
            val resultado = biblioteca.guardar(titulo, html, null, 1L) {
                ids += 1
                "id-$ids"
            }
            return (resultado as ResultadoDaBibliotecaDePaginasHtml.Recusada).mensagem
        }
        assertEquals("Faltou o título.", recusa("   ", "<p>a</p>"))
        assertEquals("Faltou o título.", recusa("", "<p>a</p>"))
        assertEquals("O título passou do limite.", recusa("a".repeat(121), "<p>a</p>"))
        assertEquals("Faltou o HTML.", recusa("Ok", "  \n  "))
        assertEquals("Faltou o HTML.", recusa("Ok", ""))
        assertEquals("O HTML passou do limite.", recusa("Ok", "h".repeat(300_001)))
        assertEquals(0, ids)
        assertTrue(biblioteca.listar().isEmpty())
        val noLimite = biblioteca.guardar("t".repeat(120), "h".repeat(300_000), null, 2L) { "cabe" }
            as ResultadoDaBibliotecaDePaginasHtml.Ok
        assertTrue(noLimite.criou)
        assertEquals(120, noLimite.item.titulo.length)
        assertEquals(300_000, noLimite.item.html.length)
    }

    @Test
    fun apagarRemoveOItemEDevolveFalsoSeNaoExistir() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("T", "<p>a</p>", null, 1L) { "a" }
        biblioteca.guardar("U", "<p>b</p>", null, 2L) { "b" }
        assertFalse(biblioteca.apagar("nao"))
        assertTrue(biblioteca.apagar("a"))
        assertNull(biblioteca.buscar("a"))
        assertEquals(listOf("b"), biblioteca.listar().map { it.id })
        assertFalse(biblioteca.apagar("a"))
    }

    @Test
    fun buscarPorTituloIgnoraEspacoECaixaEDevolveOMaisRecente() {
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador()
        biblioteca.guardar("Nota", "<p>1</p>", null, 1L) { "velha" }
        biblioteca.guardar("Outra", "<p>2</p>", null, 2L) { "meio" }
        biblioteca.guardar("nota", "<p>3</p>", null, 3L) { "nova" }
        assertEquals("nova", biblioteca.buscarPorTitulo("  NOTA ")!!.id)
        assertEquals("<p>3</p>", biblioteca.buscarPorTitulo("nota")!!.html)
        assertEquals("meio", biblioteca.buscarPorTitulo("outra")!!.id)
        assertNull(biblioteca.buscarPorTitulo("ausente"))
        assertNull(biblioteca.buscar("ausente"))
    }

    @Test
    fun substituirPorTrocaAListaMantendoAOrdemRecebida() {
        val inicial = ItemDaBibliotecaDePaginasHtmlDaColecao("antigo", "A", "<p>a</p>", 1L)
        val biblioteca = BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador(listOf(inicial))
        assertEquals(listOf("antigo"), biblioteca.listar().map { it.id })
        biblioteca.substituirPor(
            listOf(
                ItemDaBibliotecaDePaginasHtmlDaColecao("b", "B", "<p>b</p>", 3L),
                ItemDaBibliotecaDePaginasHtmlDaColecao("a", "A", "<p>a</p>", 2L),
            ),
        )
        assertEquals(listOf("b", "a"), biblioteca.listar().map { it.id })
        assertNull(biblioteca.buscar("antigo"))
        assertEquals("B", biblioteca.buscarPorTitulo("b")!!.titulo)
    }
}

```

