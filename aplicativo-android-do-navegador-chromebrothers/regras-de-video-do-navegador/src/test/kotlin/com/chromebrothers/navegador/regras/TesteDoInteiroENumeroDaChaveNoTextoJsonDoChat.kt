package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoInteiroENumeroDaChaveNoTextoJsonDoChat {
    @Test
    fun leInteiroComESemAspas() {
        assertEquals(3, TextoJsonDoChat.inteiroDaChave("""{"offset":3}""", "offset"))
        assertEquals(3, TextoJsonDoChat.inteiroDaChave("""{"offset":"3"}""", "offset"))
        assertEquals(null, TextoJsonDoChat.inteiroDaChave("""{}""", "offset"))
    }

    @Test
    fun leNumeroComDecimal() {
        assertEquals(1.5, TextoJsonDoChat.numeroDaChave("""{"paginas":1.5}""", "paginas"))
    }

    @Test
    fun leNumeroComDecimalEntreAspas() {
        assertEquals(1.5, TextoJsonDoChat.numeroDaChave("""{"paginas":"1.5"}""", "paginas"))
    }
}
