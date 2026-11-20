package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDaContagemDaBuscaNaPaginaDoNavegador {
    @Test
    fun formataOcorrenciasNoEstiloChrome() {
        assertEquals("1 de 12", ContagemDaBuscaNaPaginaDoNavegador.texto(atual = 1, total = 12, encontrado = true))
        assertEquals("3 de 3", ContagemDaBuscaNaPaginaDoNavegador.texto(atual = 3, total = 3, encontrado = true))
    }

    @Test
    fun semResultadoMostraZero() {
        assertEquals("0 de 0", ContagemDaBuscaNaPaginaDoNavegador.texto(atual = 0, total = 0, encontrado = false))
        assertEquals("0 de 0", ContagemDaBuscaNaPaginaDoNavegador.texto(atual = 1, total = 5, encontrado = false))
    }
}
