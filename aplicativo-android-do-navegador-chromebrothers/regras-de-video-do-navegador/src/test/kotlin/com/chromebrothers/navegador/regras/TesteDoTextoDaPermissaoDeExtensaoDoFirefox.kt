package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoTextoDaPermissaoDeExtensaoDoFirefox {
    @Test
    fun permissaoConhecidaViraFrase() {
        assertEquals(
            "Ler e alterar dados em todos os sites",
            TextoDaPermissaoDeExtensaoDoFirefox.frase("<all_urls>"),
        )
    }

    @Test
    fun origemDesconhecidaContinuaVisivel() {
        assertEquals(
            "Acessar https://exemplo.test/*",
            TextoDaPermissaoDeExtensaoDoFirefox.frase("https://exemplo.test/*"),
        )
    }
}
