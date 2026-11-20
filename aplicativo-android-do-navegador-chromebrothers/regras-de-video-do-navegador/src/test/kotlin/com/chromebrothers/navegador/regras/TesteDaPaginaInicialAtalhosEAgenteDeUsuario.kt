package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaPaginaInicialAtalhosEAgenteDeUsuario {
    private val agenteDoWebView = "Mozilla/5.0 (Linux; Android 14; SM-X906B; wv) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Version/4.0 Chrome/131.0.6778.200 Mobile Safari/537.36"

    @Test
    fun reconheceAPaginaInicialERecusaUmDominioParecido() {
        assertTrue(EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL))
        assertFalse(EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh("https://pagina-inicial.chromebrothers.evil/"))
    }

    @Test
    fun modoDesconhecidoAbreComoComputador() {
        assertEquals(ModoDeApresentacaoDoSite.COMPUTADOR, ModoDeApresentacaoDoSite.peloId("qualquer"))
        assertEquals(ModoDeApresentacaoDoSite.TABLET, ModoDeApresentacaoDoSite.peloId("tablet"))
    }

    @Test
    fun tabletTiraMobileEMarcaDeWebView() {
        val agente = AgenteDeUsuarioDoNavegadorChromebrothers.montar(ModoDeApresentacaoDoSite.TABLET, agenteDoWebView)
        assertTrue(agente.contains("Android"))
        assertTrue(agente.contains("Chrome/131.0.6778.200"))
        assertFalse(agente.contains("Mobile"))
        assertFalse(agente.contains("Version/4.0"))
        assertFalse(agente.contains("; wv"))
        assertFalse(AgenteDeUsuarioDoNavegadorChromebrothers.encolherParaCaberNaTela(ModoDeApresentacaoDoSite.TABLET))
    }

    @Test
    fun celularMantemMobileSemMarcaDeWebView() {
        val agente = AgenteDeUsuarioDoNavegadorChromebrothers.montar(ModoDeApresentacaoDoSite.CELULAR, agenteDoWebView)
        assertTrue(agente.contains(" Mobile"))
        assertFalse(agente.contains("Version/4.0"))
        assertFalse(agente.contains("; wv"))
    }

    @Test
    fun computadorUsaChromeDeMesaComAMesmaVersao() {
        val agente = AgenteDeUsuarioDoNavegadorChromebrothers.montar(ModoDeApresentacaoDoSite.COMPUTADOR, agenteDoWebView)
        assertTrue(agente.contains("Windows NT 10.0"))
        assertTrue(agente.contains("Chrome/131.0.6778.200"))
        assertFalse(agente.contains("Android"))
        assertFalse(agente.contains("Mobile"))
        assertTrue(AgenteDeUsuarioDoNavegadorChromebrothers.encolherParaCaberNaTela(ModoDeApresentacaoDoSite.COMPUTADOR))
    }

    @Test
    fun toqueNaVersaoParaComputadorAlternaComOPadrao() {
        assertEquals(
            ModoDeApresentacaoDoSite.COMPUTADOR,
            AlternanciaDaVersaoParaComputadorDaAba.proximo(ModoDeApresentacaoDoSite.TABLET, ModoDeApresentacaoDoSite.TABLET),
        )
        assertEquals(
            ModoDeApresentacaoDoSite.CELULAR,
            AlternanciaDaVersaoParaComputadorDaAba.proximo(ModoDeApresentacaoDoSite.COMPUTADOR, ModoDeApresentacaoDoSite.CELULAR),
        )
        assertEquals(
            ModoDeApresentacaoDoSite.TABLET,
            AlternanciaDaVersaoParaComputadorDaAba.proximo(ModoDeApresentacaoDoSite.COMPUTADOR, ModoDeApresentacaoDoSite.COMPUTADOR),
        )
    }

    @Test
    fun atalhoRepetidoOuPassadoDoLimiteNaoEntra() {
        val lista = ListaDeAtalhosDaPaginaInicialDoNavegador(limite = 1)
        assertEquals(
            ResultadoDoAtalhoDaPaginaInicial.ADICIONADO,
            lista.tentarAdicionar("Exemplo", "https://exemplo.test/"),
        )
        assertEquals(
            ResultadoDoAtalhoDaPaginaInicial.REPETIDO,
            lista.tentarAdicionar("Outro", "https://exemplo.test"),
        )
        assertEquals(
            ResultadoDoAtalhoDaPaginaInicial.LIMITE,
            lista.tentarAdicionar("Dois", "https://dois.test"),
        )
        assertEquals(
            ResultadoDoAtalhoDaPaginaInicial.INVALIDO,
            lista.tentarAdicionar("Inicio", EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL),
        )
    }
}
