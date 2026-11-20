package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TesteDoBancoDeNotasDoCampoDoNavegador {
    @Test
    fun guardarColocaANotaNaFrenteENaoDuplicaOTexto() {
        val primeira = BancoDeNotasDoCampoDoNavegador.guardar(emptyList(), "  rascunho  ", 1L)
        val segunda = BancoDeNotasDoCampoDoNavegador.guardar(primeira, "outro", 2L)
        val repetida = BancoDeNotasDoCampoDoNavegador.guardar(segunda, "rascunho", 3L)
        assertEquals(listOf(2L, 1L), segunda.map { it.id })
        assertEquals(listOf(3L, 2L), repetida.map { it.id })
        assertEquals(segunda, BancoDeNotasDoCampoDoNavegador.guardar(segunda, "   ", 4L))
    }

    @Test
    fun gravarELerConservamQuebraDeLinha() {
        val notas = listOf(NotaDoCampoDoNavegador(7L, "linha 1\nlinha\ttwo"))
        val lidas = BancoDeNotasDoCampoDoNavegador.ler(BancoDeNotasDoCampoDoNavegador.gravar(notas))
        assertEquals(notas, lidas)
        assertEquals("linha 1\nlinha\ttwo", BancoDeNotasDoCampoDoNavegador.texto(lidas, 7L))
        assertNull(BancoDeNotasDoCampoDoNavegador.texto(lidas, 8L))
        assertEquals(emptyList(), BancoDeNotasDoCampoDoNavegador.remover(lidas, 7L))
    }
}
