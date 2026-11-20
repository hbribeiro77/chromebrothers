package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaLiberacaoDeNavegacaoDosSitesParaOChat {
    @Test
    fun liberarUsaOSiteSemWwwENaoLiberaOutroHospedeiro() {
        val primeira = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(
            emptyList(),
            "https://www.sjinn.ai/app",
        ) as ResultadoDaLiberacaoDeNavegacao.Liberada
        assertEquals("sjinn.ai", primeira.hospedeiro)
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.contem(primeira.sites, "sjinn.ai"))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.contem(primeira.sites, "https://app.sjinn.ai/"))
        val deNovo = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(primeira.sites, "sjinn.ai")
            as ResultadoDaLiberacaoDeNavegacao.Liberada
        assertEquals(listOf("sjinn.ai"), deNovo.sites)
    }

    @Test
    fun recusaSiteVazioPaginaInicialEPassaDoLimite() {
        val vazio = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(emptyList(), "  ")
        assertEquals("Faltou o site.", (vazio as ResultadoDaLiberacaoDeNavegacao.Recusada).mensagem)
        val inicio = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(
            emptyList(),
            "https://pagina-inicial.chromebrothers/",
        )
        assertEquals(
            "A página inicial não recebe liberação.",
            (inicio as ResultadoDaLiberacaoDeNavegacao.Recusada).mensagem,
        )
        val cheia = List(LiberacaoDeNavegacaoDosSitesParaOChat.LIMITE) { "site$it.exemplo" }
        val extra = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(cheia, "novo.exemplo")
        assertEquals("A lista chegou ao limite.", (extra as ResultadoDaLiberacaoDeNavegacao.Recusada).mensagem)
    }

    @Test
    fun abrirEClicarSeguemSemPerguntarSoNoSiteLiberado() {
        val sites = listOf("sjinn.ai")
        val abrir = PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://www.sjinn.ai/assets")
        val outro = PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://example.com")
        val clique = PedidoDeAcaoConfirmadaDoChat.Clicar("Tools")
        val abaEmBranco = PedidoDeAcaoConfirmadaDoChat.CriarAba("")
        val abaNoSite = PedidoDeAcaoConfirmadaDoChat.CriarAba("https://sjinn.ai/assets")
        val campo = PedidoDeAcaoConfirmadaDoChat.PreencherCampo("oi")
        val escrever = PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina("batman")
        val nota = PedidoDeAcaoConfirmadaDoChat.GuardarNota("", "caminho")
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(abrir, "https://example.com", sites))
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(outro, "https://sjinn.ai", sites))
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(clique, "https://sjinn.ai/app", sites),
        )
        assertFalse(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(clique, "https://example.com", sites),
        )
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(abaEmBranco, "https://sjinn.ai", sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(abaNoSite, "https://example.com", sites))
        assertTrue(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(campo, "https://sjinn.ai", sites))
        assertFalse(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(campo, "https://example.com", sites),
        )
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(escrever, "https://sjinn.ai/app", sites),
        )
        assertFalse(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                escrever,
                "https://docs.google.com/document/d/x",
                sites,
            ),
        )
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                escrever,
                "https://docs.google.com/document/d/x",
                listOf("docs.google.com"),
            ),
        )
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(nota, "https://sjinn.ai", sites))
    }

    @Test
    fun oBotaoDeLiberarApontaParaOSiteDaAcao() {
        val clique = PedidoDeAcaoConfirmadaDoChat.Clicar("Tools")
        assertEquals(
            "sjinn.ai",
            LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(clique, "https://www.sjinn.ai/app"),
        )
        val abrir = PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://example.com/a")
        assertEquals("example.com", LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(abrir, "https://sjinn.ai"))
        val campo = PedidoDeAcaoConfirmadaDoChat.PreencherCampo("oi")
        assertEquals(
            "sjinn.ai",
            LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(campo, "https://sjinn.ai"),
        )
        val escrever = PedidoDeAcaoConfirmadaDoChat.EscreverNaPagina("oi")
        assertEquals(
            "docs.google.com",
            LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(escrever, "https://docs.google.com/document/d/x"),
        )
        val inicio = PedidoDeAcaoConfirmadaDoChat.Clicar("Sjinn")
        assertEquals(
            "",
            LiberacaoDeNavegacaoDosSitesParaOChat.hospedeiroDaAcao(inicio, EnderecoDaPaginaInicialDoNavegadorChromebrothers.URL),
        )
    }

    @Test
    fun tirarOSiteVoltaAPedirConfirmacaoEOTextoGravadoVolta() {
        val liberada = LiberacaoDeNavegacaoDosSitesParaOChat.liberar(emptyList(), "sjinn.ai")
            as ResultadoDaLiberacaoDeNavegacao.Liberada
        val semEsse = LiberacaoDeNavegacaoDosSitesParaOChat.tirar(liberada.sites, "https://www.sjinn.ai/x")
        assertEquals(emptyList(), semEsse)
        val lidos = TextoDaListaDeSitesComNavegacaoLiberada.ler(
            TextoDaListaDeSitesComNavegacaoLiberada.escrever(listOf("sjinn.ai", "example.com")),
        )
        assertEquals(listOf("sjinn.ai", "example.com"), lidos)
        val pedido = PedidoDeAcaoConfirmadaDoChat.AbrirEndereco("https://sjinn.ai")
        assertFalse(LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(pedido, "", semEsse))
    }

    @Test
    fun aInstrucaoContaQueOSiteLiberadoSegueSemPerguntar() {
        val texto = FerramentasDeLeituraDoChatDaOpenRouter.instrucao("Aba: Sjinn", navegacaoLiberada = true)
        assertTrue(texto.contains("navegação está liberada"))
        assertTrue(texto.contains("sem pedir confirmação"))
    }
}
