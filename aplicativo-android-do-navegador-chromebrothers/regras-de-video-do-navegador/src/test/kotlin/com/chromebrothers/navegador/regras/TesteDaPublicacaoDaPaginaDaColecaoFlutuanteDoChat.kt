package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaPublicacaoDaPaginaDaColecaoFlutuanteDoChat {
    @Test
    fun lerPublicacaoSanitizaELimita() {
        val args = """{"titulo":"Resumo","html":"<h1>Oi</h1><script>x()</script><script src=\"https://evil.test/a.js\"></script>"}"""
        val pronta = PublicacaoDaPaginaDaColecaoFlutuanteDoChat.ler(args)
            as ResultadoDaPublicacaoDaPaginaDaColecao.Pronta
        assertEquals("Resumo", pronta.titulo)
        assertTrue(pronta.html.contains("<h1>Oi</h1>"))
        assertTrue(pronta.html.contains("<script>x()</script>"))
        assertFalse(pronta.html.contains("evil.test"))
        assertTrue(
            PublicacaoDaPaginaDaColecaoFlutuanteDoChat.pergunta(pronta.titulo)
                .startsWith("Publicar página"),
        )
    }

    @Test
    fun htmlVazioOuGiganteSaoRecusados() {
        val vazio = PublicacaoDaPaginaDaColecaoFlutuanteDoChat.ler("""{"titulo":"x","html":"   "}""")
        assertTrue(vazio is ResultadoDaPublicacaoDaPaginaDaColecao.Recusada)
        assertEquals(10 * 1024 * 1024, PublicacaoDaPaginaDaColecaoFlutuanteDoChat.LIMITE_DO_HTML)
        val gigante = "a".repeat(PublicacaoDaPaginaDaColecaoFlutuanteDoChat.LIMITE_DO_HTML + 1)
        val estouro = PublicacaoDaPaginaDaColecaoFlutuanteDoChat.ler(
            """{"titulo":"x","html":"$gigante"}""",
        )
        assertEquals(
            "O HTML passou do limite.",
            (estouro as ResultadoDaPublicacaoDaPaginaDaColecao.Recusada).mensagem,
        )
    }

    @Test
    fun siteLiberadoSegueSemPerguntarAoPublicarPagina() {
        val pedido = PedidoDeAcaoConfirmadaDoChat.PublicarPaginaDaColecao("t", "<p>a</p>")
        assertTrue(
            LiberacaoDeNavegacaoDosSitesParaOChat.segueSemPerguntar(
                pedido,
                "https://sjinn.ai/aula",
                listOf("sjinn.ai"),
            ),
        )
    }
}
