package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaListaDeBloqueioDeAnuncioDoNavegador {
    private val texto = """
        [Adblock Plus 2.0]
        ! comentario ||nao-bloqueia.test^
        ##.anuncio
        ||doubleclick.net^
        ||exemplo.test^${'$'}third-party
        @@||pubads.g.doubleclick.net^
        ||com^
        ||rede.test/banner^
    """.trimIndent()

    private val lista = TextoDaListaDeBloqueioDeAnuncio.ler(texto)

    @Test
    fun desligadoNaoBloqueia() {
        assertFalse(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = false,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://pagead.doubleclick.net/ads",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
    }

    @Test
    fun dominioDeAnuncioBloqueiaOSubdominio() {
        assertTrue(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://pagead.doubleclick.net/ads",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
    }

    @Test
    fun oQuadroPrincipalContinuaAbrindo() {
        assertFalse(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = true,
                urlDoPedido = "https://pagead.doubleclick.net/ads",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
    }

    @Test
    fun aExcecaoLiberaSoOHostCitado() {
        assertFalse(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://pubads.g.doubleclick.net/gampad",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
        assertTrue(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://pagead.doubleclick.net/ads",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
    }

    @Test
    fun regraDeTerceiroNaoBloqueiaOProprioSite() {
        assertFalse(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://cdn.exemplo.test/a.js",
                urlDaPagina = "https://www.exemplo.test/inicio",
                lista = lista,
            ),
        )
        assertTrue(
            DecisaoDeBloqueioDeAnuncio.deveBloquear(
                habilitado = true,
                ehQuadroPrincipal = false,
                urlDoPedido = "https://ads.exemplo.test/a.js",
                urlDaPagina = "https://jornal.test/a",
                lista = lista,
            ),
        )
    }
}
