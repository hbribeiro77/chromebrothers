package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TesteDaBibliotecaDeImagensCapturadasDoNavegador {
    @Test
    fun cadaQuadroEntraComoItemNovo() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador()
        biblioteca.registrar("a", 1, "a.jpg", naGaleria = true)
        biblioteca.registrar("b", 2, "b.jpg", naGaleria = false)
        assertEquals(listOf("b", "a"), biblioteca.historico().map { it.id })
        assertTrue(biblioteca.buscar("a")!!.naGaleria)
        assertFalse(biblioteca.buscar("b")!!.naGaleria)
    }

    @Test
    fun copiadasSemBaixarSaoSoAsQueNaoForamParaAGaleria() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador()
        biblioteca.registrar("salva", 1, "salva.jpg", naGaleria = true)
        biblioteca.registrar("copia", 2, "copia.jpg", naGaleria = false)
        assertEquals(listOf("copia"), biblioteca.copiadasSemBaixar().map { it.id })
        biblioteca.marcarComoBaixada("copia")
        assertTrue(biblioteca.copiadasSemBaixar().isEmpty())
        assertTrue(biblioteca.buscar("copia")!!.naGaleria)
    }

    @Test
    fun removerDoHistoricoApagaQuemNaoEFavoritaEGuardaAFavorita() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador()
        biblioteca.registrar("solta", 1, "solta.jpg", naGaleria = false)
        biblioteca.registrar("cara", 2, "cara.jpg", naGaleria = false)
        biblioteca.definirFavorito("cara", true)
        val apagou = biblioteca.removerDoHistorico("solta")
        assertTrue(apagou!!.apagouOItem)
        assertEquals("solta.jpg", apagou.nomeDoArquivo)
        assertNull(biblioteca.buscar("solta"))
        val guardou = biblioteca.removerDoHistorico("cara")
        assertFalse(guardou!!.apagouOItem)
        assertEquals(emptyList(), biblioteca.historico())
        assertEquals(listOf("cara"), biblioteca.favoritos().map { it.id })
    }

    @Test
    fun vincularGaleriaGuardaOEnderecoEONomeParaRenomear() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador()
        biblioteca.registrar("copia", 1, "copia.jpg", naGaleria = false)
        assertNull(biblioteca.renomear("copia", "praia.jpg"))
        val ligada = biblioteca.vincularGaleria("copia", "content://galeria/1", "copia.jpg")
        assertEquals("content://galeria/1", ligada!!.uriDaGaleria)
        assertTrue(ligada.naGaleria)
        assertTrue(biblioteca.copiadasSemBaixar().isEmpty())
        val renomeada = biblioteca.renomear("copia", "praia.jpg")
        assertEquals("praia.jpg", renomeada!!.nomeExibido)
        assertEquals("content://galeria/1", renomeada.uriDaGaleria)
        assertNull(biblioteca.renomear("copia", "   "))
    }

    @Test
    fun atualizarMetadadosDeGeracaoMantemPosicaoEPreservaCamposNulos() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador()
        biblioteca.registrar("a", 1, "a.jpg", naGaleria = false)
        biblioteca.registrar("b", 2, "b.jpg", naGaleria = false)
        val atualizado = biblioteca.atualizarMetadadosDeGeracao(
            id = "a",
            origem = " Sjinn ",
            modelo = "m1",
            prompt = "um gato",
            refs = listOf("r0.jpg"),
            caminhoLocal = "/x/a.jpg",
        )
        assertEquals("sjinn", atualizado!!.origem)
        assertEquals("m1", atualizado.modelo)
        assertEquals("um gato", atualizado.prompt)
        assertEquals(listOf("r0.jpg"), atualizado.refs)
        assertEquals("/x/a.jpg", atualizado.caminhoLocal)
        assertEquals(listOf("b", "a"), biblioteca.listar().map { it.id })
        val parcial = biblioteca.atualizarMetadadosDeGeracao("a", modelo = "m2")
        assertEquals("m2", parcial!!.modelo)
        assertEquals("um gato", parcial.prompt)
        assertEquals("sjinn", parcial.origem)
        assertNull(biblioteca.atualizarMetadadosDeGeracao("inexistente", origem = "higgsfield"))
    }

    @Test
    fun imagemAntigaSemFavoritoSaiQuandoPassaDoLimite() {
        val biblioteca = BibliotecaDeImagensCapturadasDoNavegador(limiteDoHistorico = 2)
        biblioteca.registrar("a", 1, "a.jpg", false)
        biblioteca.definirFavorito("a", true)
        biblioteca.registrar("b", 2, "b.jpg", false)
        biblioteca.registrar("c", 3, "c.jpg", false)
        assertEquals(listOf("c", "b"), biblioteca.historico().map { it.id })
        assertEquals(listOf("a"), biblioteca.favoritos().map { it.id })
        assertNull(biblioteca.definirFavorito("a", false))
        assertEquals(listOf("c", "b"), biblioteca.listar().map { it.id })
    }
}
