package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoCorpoJsonDoChatComFerramentasHiggsfieldEAnexo {
    @Test
    fun corpoComFerramentasEAnexoEhJsonEstruturalmenteValido() {
        val corpo = PedidoDeChatParaAOpenRouter.corpo(
            modelo = "deepseek/deepseek-v4.1-flash-1017",
            mensagens = listOf(
                MensagemDeChat(
                    papel = "user",
                    conteudo = "Gera com a máscara",
                    partes = listOf(
                        ParteDeConteudoDoChat(
                            tipo = "imagem",
                            mime = "image/jpeg",
                            nome = "batgirl.jpg",
                            dados = "QUJDREVGR0g=",
                        ),
                    ),
                ),
            ),
            comFerramentas = true,
        )
        assertJsonEstruturalmenteValido(corpo)
        assertTrue(corpo.contains("gerar_imagem_higgsfield"))
        assertTrue(corpo.contains("id_imagem"))
        assertTrue(corpo.contains("anexo"))
        // Aspas literais sem escape quebravam o pedido (OpenRouter: JSON parsing failed).
        assertFalse(corpo.contains("""id_imagem":""""))
    }

    @Test
    fun definicaoDeGerarImagemEscapaAspasNaDescricao() {
        val definicao = AcoesConfirmadasDoChatDaOpenRouter.definicoes()
            .first { it.contains("gerar_imagem_higgsfield") }
        assertJsonEstruturalmenteValido(definicao)
        assertTrue(definicao.contains("anexo"))
    }

    private fun assertJsonEstruturalmenteValido(json: String) {
        var profundidade = 0
        var emString = false
        var escape = false
        for (c in json) {
            if (emString) {
                when {
                    escape -> escape = false
                    c == '\\' -> escape = true
                    c == '"' -> emString = false
                }
            } else {
                when (c) {
                    '"' -> emString = true
                    '{', '[' -> profundidade++
                    '}', ']' -> {
                        profundidade--
                        assertTrue(profundidade >= 0, "fecha demais em JSON")
                    }
                }
            }
        }
        assertFalse(emString, "string JSON aberta")
        assertEquals(0, profundidade, "chaves/colchetes desbalanceados")
    }
}
