package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaMigracaoDoIndiceJsonDaBibliotecaDeComponentesHtml {
    @Test
    fun tituloAntigoViraNomeEDescricaoETagsFicamVazios() {
        val lidos = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(
            """[{"id":"abc","titulo":"Cartão velho","atualizadoEm":42}]""",
        )
        assertEquals(1, lidos.size)
        assertEquals("abc", lidos[0].id)
        assertEquals("Cartão velho", lidos[0].nome)
        assertEquals("", lidos[0].descricao)
        assertEquals(emptyList(), lidos[0].tags)
        assertEquals(42L, lidos[0].atualizadoEm)
    }

    @Test
    fun nomeGanhaDeTituloETagsEDescricaoVoltamNaEscrita() {
        val lidos = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(
            """[{"id":"n1","titulo":"Velho","nome":"Novo","descricao":"um bloco","tags":["aula"," pdf "],"atualizadoEm":7}]""",
        )
        assertEquals("Novo", lidos.single().nome)
        assertEquals("um bloco", lidos.single().descricao)
        assertEquals(listOf("aula", "pdf"), lidos.single().tags)
        val escrito = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.escrever(lidos)
        assertTrue(escrito.contains("\"nome\":\"Novo\""))
        assertTrue(!escrito.contains("titulo"))
        val deNovo = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(escrito)
        assertEquals(lidos, deNovo)
    }

    @Test
    fun nomeEmBrancoCaiNoTituloEItemSemNomeSome() {
        val lidos = LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml.ler(
            """[{"id":"a","nome":"  ","titulo":"Pelo titulo"},{"id":"","titulo":"sem id"},{"titulo":"sem id de verdade"}]""",
        )
        assertEquals(listOf("Pelo titulo"), lidos.map { it.nome })
    }
}
