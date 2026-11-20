package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaDecisaoDeNavegacaoParaOutroSiteDoNavegador {
    private val lista = TextoDaListaDeBloqueioDeAnuncio.ler(
        """
        ||doubleclick.net^
        ||exemplo.test^${'$'}third-party
        @@||pubads.g.doubleclick.net^
        """.trimIndent(),
    )

    @Test
    fun desligadoDeixaAPaginaIrParaOutroSite() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = false,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://outro.test/pagina",
                lista = lista,
            ),
        )
    }

    @Test
    fun semToqueNaoDeixaSairParaOutroSite() {
        assertTrue(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://outro.test/pagina",
                lista = lista,
            ),
        )
    }

    @Test
    fun umToqueEmLinkDeOutroSiteContinuaValendo() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = true,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://outro.test/pagina",
                lista = lista,
            ),
        )
    }

    @Test
    fun redirecionamentoDepoisDeUmLoginContinuaValendo() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = true,
                urlAtual = "https://jornal.test/entrar",
                urlNova = "https://contas.test/oauth",
                lista = lista,
            ),
        )
    }

    @Test
    fun oMesmoSitePodeTrocarDeEnderecoSozinho() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://www.jornal.test/b",
                lista = lista,
            ),
        )
    }

    @Test
    fun aNavegacaoPedidaPeloAplicativoAbreOutroEndereco() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://www.deviantart.com/users/login",
                urlNova = "data:text/html,pagina",
                lista = lista,
                pedidaPeloAplicativo = true,
            ),
        )
    }

    @Test
    fun dominioDeAnuncioNaoAbreNemComToque() {
        assertTrue(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = true,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://pagead.doubleclick.net/ads",
                lista = lista,
            ),
        )
    }

    @Test
    fun dominioDeAnuncioNaoAbreNemQuandoOAplicativoPede() {
        assertTrue(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://pagead.doubleclick.net/ads",
                lista = lista,
                pedidaPeloAplicativo = true,
            ),
        )
    }

    @Test
    fun aVoltaDoLoginDoGoogleParaOSiteNaoECancelada() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://accounts.google.com/gsi/transform",
                urlNova = "https://www.deviantart.com/users/login",
                lista = lista,
            ),
        )
    }

    @Test
    fun oLoginDoGoogleNaoEntregaUmDominioDeAnuncio() {
        assertTrue(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = false,
                ehRedirecionamento = false,
                urlAtual = "https://accounts.google.com/gsi/transform",
                urlNova = "https://pagead.doubleclick.net/ads",
                lista = lista,
            ),
        )
    }

    @Test
    fun aExcecaoDaListaAbreQuandoAPessoaToca() {
        assertFalse(
            DecisaoDeNavegacaoParaOutroSite.deveCancelar(
                bloqueioLigado = true,
                temGestoDoUsuario = true,
                ehRedirecionamento = false,
                urlAtual = "https://jornal.test/a",
                urlNova = "https://pubads.g.doubleclick.net/gampad",
                lista = lista,
            ),
        )
    }
}
