package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertTrue

class TesteDoTextoDaPaginaInicialParaOChat {
    @Test
    fun aPaginaInicialListaCadaAtalhoComNomeEEndereco() {
        val texto = TextoDaPaginaInicialParaOChat.descrever(
            listOf(
                AtalhoDaPaginaInicial("Sjinn", "https://sjinn.example/"),
                AtalhoDaPaginaInicial("", "https://outro.example/"),
            ),
        )
        assertTrue(texto.contains("página inicial do ChromeBrothers"))
        assertTrue(texto.contains("não um site"))
        assertTrue(texto.contains("Sjinn — https://sjinn.example/"))
        assertTrue(texto.contains("https://outro.example/ — https://outro.example/"))
    }

    @Test
    fun semAtalhoAPaginaInicialDizQueNadaEstaFixado() {
        val texto = TextoDaPaginaInicialParaOChat.descrever(emptyList())
        assertTrue(texto.contains("Nenhum atalho fixado."))
    }
}
