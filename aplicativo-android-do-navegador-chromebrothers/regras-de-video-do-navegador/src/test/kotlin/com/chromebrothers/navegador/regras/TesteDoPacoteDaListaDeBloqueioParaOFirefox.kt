package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoPacoteDaListaDeBloqueioParaOFirefox {
    @Test
    fun oPacoteLevaOsQuatroConjuntos() {
        val lista = TextoDaListaDeBloqueioDeAnuncio.ler(
            "||anuncio.test^\n||so-terceiro.test^\$third-party\n@@||libera.test^\n",
        )
        assertEquals(
            """{"bloqueios":["anuncio.test"],"bloqueiosDeTerceiros":["so-terceiro.test"],"excecoes":["libera.test"],"excecoesDeTerceiros":[]}""",
            PacoteDaListaDeBloqueioParaOFirefox.json(lista),
        )
    }

    @Test
    fun aBaseRegistravelUsaOsDoisUltimosRotulos() {
        assertEquals("exemplo.com", DecisaoDeBloqueioDeAnuncio.baseRegistravel("cdn.exemplo.com"))
        assertEquals("exemplo.com", DecisaoDeBloqueioDeAnuncio.baseRegistravel("exemplo.com"))
        assertEquals("1.2.3.4", DecisaoDeBloqueioDeAnuncio.baseRegistravel("1.2.3.4"))
        assertEquals("::1", DecisaoDeBloqueioDeAnuncio.baseRegistravel("::1"))
    }
}
