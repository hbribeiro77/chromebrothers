package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaJanelaNovaDoSiteComoModalDeLoginDoNavegador {
    @Test
    fun loginConhecidoFicaNaModal() {
        assertTrue(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://accounts.google.com/o/oauth2/v2/auth?client_id=x",
            ),
        )
        assertTrue(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://appleid.apple.com/auth/authorize",
            ),
        )
        assertTrue(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://login.microsoftonline.com/common/oauth2/v2.0/authorize",
            ),
        )
        assertTrue(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://empresa.okta.com/oauth2/v1/authorize",
            ),
        )
    }

    @Test
    fun linkNormalEBlankVaoParaNovaAba() {
        assertFalse(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://www.example.com/artigo",
            ),
        )
        assertFalse(
            JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(
                "https://x.com/someone/status/1",
            ),
        )
        assertFalse(JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin(""))
        assertFalse(JanelaNovaDoSiteComoModalDeLoginDoNavegador.deveAbrirComoModalDeLogin("about:blank"))
    }
}
