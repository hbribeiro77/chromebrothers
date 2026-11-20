package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaCompactacaoDaConversaDoChatDaOpenRouter {
    @Test
    fun compactaMantemUltimasDuasTrocasSemPartes() {
        val hist = listOf(
            MensagemDeChat("user", "u1", partes = listOf(ParteDeConteudoDoChat("imagem", mime = "image/png", dados = "xx"))),
            MensagemDeChat("assistant", "a1"),
            MensagemDeChat("user", "u2"),
            MensagemDeChat("assistant", "a2"),
            MensagemDeChat("user", "u3"),
            MensagemDeChat("assistant", "a3"),
        )
        val out = CompactacaoDaConversaDoChatDaOpenRouter.aplicar("RESUMO", hist)
        assertEquals("RESUMO", out.first().conteudo)
        assertEquals("assistant", out.first().papel)
        assertTrue(out.drop(1).none { it.partes.isNotEmpty() })
        assertEquals(listOf("u2", "a2", "u3", "a3"), out.drop(1).map { it.conteudo })
    }

    @Test
    fun aplicarCopiaSoOTextoDasUltimasTrocasEIgnoraToolESystem() {
        assertEquals(2, CompactacaoDaConversaDoChatDaOpenRouter.ULTIMAS_TROCAS_USER_ASSISTANT)
        val hist = listOf(
            MensagemDeChat("system", "sys-antiga"),
            MensagemDeChat("user", "u1"),
            MensagemDeChat("assistant", "a1"),
            MensagemDeChat("tool", "resultado", idDaChamada = "t1"),
            MensagemDeChat(
                "user",
                "u2",
                partes = listOf(ParteDeConteudoDoChat("imagem", mime = "image/png", dados = "yy")),
            ),
            MensagemDeChat("system", "sys-meio"),
            MensagemDeChat(
                "assistant",
                "a2",
                chamadas = listOf(ChamadaDeFerramenta("c1", "ler", "{}")),
            ),
            MensagemDeChat("user", "u3"),
            MensagemDeChat("tool", "ok", idDaChamada = "t2"),
            MensagemDeChat("assistant", "a3"),
        )
        val out = CompactacaoDaConversaDoChatDaOpenRouter.aplicar("  RESUMO  ", hist)
        assertEquals(
            listOf(
                MensagemDeChat("assistant", "RESUMO"),
                MensagemDeChat("user", "u2"),
                MensagemDeChat("assistant", "a2"),
                MensagemDeChat("user", "u3"),
                MensagemDeChat("assistant", "a3"),
            ),
            out,
        )
    }

    @Test
    fun aplicarMantemUsuarioSemRespostaEHistoricoCurtoOuVazio() {
        val incompleta = listOf(
            MensagemDeChat("user", "u1"),
            MensagemDeChat("assistant", "a1"),
            MensagemDeChat("user", "u2"),
            MensagemDeChat("assistant", "a2"),
            MensagemDeChat("user", "u3"),
        )
        assertEquals(
            listOf("R", "u2", "a2", "u3"),
            CompactacaoDaConversaDoChatDaOpenRouter.aplicar("R", incompleta).map { it.conteudo },
        )
        val uma = listOf(MensagemDeChat("user", "so"), MensagemDeChat("assistant", "resposta"))
        assertEquals(
            listOf(
                MensagemDeChat("assistant", "R"),
                MensagemDeChat("user", "so"),
                MensagemDeChat("assistant", "resposta"),
            ),
            CompactacaoDaConversaDoChatDaOpenRouter.aplicar("R", uma),
        )
        assertEquals(
            listOf(MensagemDeChat("assistant", "R")),
            CompactacaoDaConversaDoChatDaOpenRouter.aplicar(" R ", emptyList()),
        )
    }

    @Test
    fun promptPedeResumoFielEmPortuguesSemBase64DosAnexos() {
        val hist = listOf(
            MensagemDeChat(
                "user",
                "u1",
                partes = listOf(ParteDeConteudoDoChat("imagem", mime = "image/png", dados = "xx")),
            ),
            MensagemDeChat("assistant", "a1"),
            MensagemDeChat("tool", "segredo-tool", idDaChamada = "c1"),
            MensagemDeChat("system", "instrucao-interna"),
            MensagemDeChat(
                "user",
                "planilha",
                partes = listOf(
                    ParteDeConteudoDoChat("arquivo", nome = "a.pdf", mime = "application/pdf", dados = "xx"),
                    ParteDeConteudoDoChat("texto", texto = "trecho-lido"),
                ),
            ),
        )
        val out = CompactacaoDaConversaDoChatDaOpenRouter.promptParaResumir(hist)
        assertTrue(out.size >= 1)
        assertTrue(out.any { it.papel == "system" })
        assertTrue(out.any { it.papel == "user" })
        val texto = out.joinToString("\n") { it.conteudo }
        assertTrue(texto.contains("resumir"))
        assertTrue(texto.contains("português"))
        assertFalse(texto.contains("xx"))
        assertTrue(texto.contains("[imagem]"))
        assertTrue(texto.contains("[arquivo]"))
        assertTrue(texto.contains("u1"))
        assertTrue(texto.contains("a1"))
        assertTrue(texto.contains("planilha"))
        assertTrue(texto.contains("trecho-lido"))
        assertFalse(texto.contains("segredo-tool"))
        assertFalse(texto.contains("instrucao-interna"))
        assertTrue(out.all { it.partes.isEmpty() })
    }
}
