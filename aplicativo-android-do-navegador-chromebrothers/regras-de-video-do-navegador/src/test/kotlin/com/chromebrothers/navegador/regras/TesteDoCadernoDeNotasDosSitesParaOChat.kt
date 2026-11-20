package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoCadernoDeNotasDosSitesParaOChat {
    @Test
    fun guardarUsaOSiteSemWwwESubstituiANotaAntiga() {
        val primeira = CadernoDeNotasDosSitesParaOChat.guardar(
            emptyList(),
            "https://www.sjinn.ai/app",
            "Os assets ficam em Tools e depois My Assets.",
        ) as ResultadoDaNotaDoSite.Guardada
        assertEquals("sjinn.ai", primeira.nota.hospedeiro)
        val segunda = CadernoDeNotasDosSitesParaOChat.guardar(
            primeira.notas,
            "sjinn.ai",
            "O caminho novo é Tools, My Assets.",
        ) as ResultadoDaNotaDoSite.Guardada
        assertEquals(1, segunda.notas.size)
        assertEquals("O caminho novo é Tools, My Assets.", segunda.notas.single().texto)
        assertTrue(CadernoDeNotasDosSitesParaOChat.paraOModelo(segunda.notas.single()).contains("My Assets"))
    }

    @Test
    fun recusaNotaVaziaSegredoEPaginaInicial() {
        val vazia = CadernoDeNotasDosSitesParaOChat.guardar(emptyList(), "sjinn.ai", "  ")
        assertEquals("Faltou o texto.", (vazia as ResultadoDaNotaDoSite.Recusada).mensagem)
        val segredo = CadernoDeNotasDosSitesParaOChat.guardar(emptyList(), "sjinn.ai", "token sk-abc")
        assertEquals("Não guardo chave nem segredo.", (segredo as ResultadoDaNotaDoSite.Recusada).mensagem)
        val inicio = CadernoDeNotasDosSitesParaOChat.guardar(
            emptyList(),
            "https://pagina-inicial.chromebrothers/",
            "atalhos",
        )
        assertEquals("A página inicial não recebe nota.", (inicio as ResultadoDaNotaDoSite.Recusada).mensagem)
    }

    @Test
    fun oTextoGravadoVoltaComQuebraDeLinha() {
        val guardada = CadernoDeNotasDosSitesParaOChat.guardar(
            emptyList(),
            "sjinn.ai",
            "Tools\nMy Assets",
        ) as ResultadoDaNotaDoSite.Guardada
        val lidas = TextoDoCadernoDeNotasDosSites.ler(TextoDoCadernoDeNotasDosSites.escrever(guardada.notas))
        assertEquals(guardada.notas, lidas)
        val semEssa = CadernoDeNotasDosSitesParaOChat.apagar(lidas, "https://sjinn.ai/assets")
        assertTrue(semEssa.isEmpty())
    }

    @Test
    fun aFerramentaPedeConfirmacaoSemAbrirSozinha() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            "deepseek/deepseek-v4.1-flash-1017",
            listOf(MensagemDeChat("user", "lembra isso")),
            comFerramentas = true,
        )
        assertTrue(corpo.contains(AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_NOTA))
        assertFalse(corpo.contains("javascript"))
        val resultado = AcoesConfirmadasDoChatDaOpenRouter.interpretar(
            AcoesConfirmadasDoChatDaOpenRouter.GUARDAR_NOTA,
            """{"texto":"Tools e depois My Assets"}""",
        )
        val pedido = (resultado as ResultadoDaInterpretacaoDaAcaoDoChat.Pronta).pedido
        assertEquals(
            "Guardar esta nota no site desta aba?\nTools e depois My Assets",
            AcoesConfirmadasDoChatDaOpenRouter.pergunta(pedido),
        )
    }
}
