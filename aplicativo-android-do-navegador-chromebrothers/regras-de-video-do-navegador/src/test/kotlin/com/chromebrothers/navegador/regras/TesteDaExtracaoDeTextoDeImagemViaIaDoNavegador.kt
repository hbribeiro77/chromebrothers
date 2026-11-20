package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador {
    @Test
    fun promptPedeSoTextoLiteral() {
        val p = ExtracaoDeTextoDeImagemViaIaDoNavegador.promptDoUsuario()
        assertTrue(p.contains("texto"))
        assertTrue(p.lowercase().contains("sem coment"))
    }

    @Test
    fun limparRespostaRemoveCercasMarkdown() {
        assertEquals(
            "ABC-123",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta("```\nABC-123\n```"),
        )
    }

    @Test
    fun limparRespostaMantemTodasAsLinhasQuandoCercaNaoTemIdioma() {
        assertEquals(
            "ABC-123\nRua das Flores",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta(
                "```\nABC-123\nRua das Flores\n```",
            ),
        )
    }

    @Test
    fun cercaVaziaComIdiomaViraSemTexto() {
        assertEquals(
            "",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.limparResposta("```text\n```"),
        )
        assertEquals(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("```text\n```"),
        )
    }

    @Test
    fun mensagemDeFalhaNullBrancoOuSemVisaoUsaGenerica() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(null))
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha("   "))
        val erroComum = "timeout ao conectar no provedor"
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(erroComum))
        val erroQueSoMencionaImagem = "failed to download image: 413 payload too large"
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(erroQueSoMencionaImagem))
    }

    @Test
    fun mensagemDeFalhaVisionOuMultimodalUsaModeloSemVisao() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("model does not support vision"),
        )
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("This model is not multimodal"),
        )
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("o modelo não suporta image_url"),
        )
    }

    @Test
    fun mensagemDeFalhaSemEndpointComEntradaDeImagemUsaModeloSemVisao() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(
            regra.FALHA_MODELO_SEM_VISAO,
            regra.mensagemDeFalha("No endpoints found that support image input"),
        )
    }

    @Test
    fun mensagemDeFalhaDoesNotSupportStreamingUsaGenerica() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(
            regra.FALHA_GENERICA,
            regra.mensagemDeFalha("model does not support streaming"),
        )
    }

    @Test
    fun mensagemDeFalhaNulaUsaGenericaSemVazarTextoDaApi() {
        val regra = ExtracaoDeTextoDeImagemViaIaDoNavegador
        assertEquals(regra.FALHA_GENERICA, regra.mensagemDeFalha(null))
    }

    @Test
    fun textoParaExibirQuandoVazioUsaMensagemPadrao() {
        assertEquals(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("   "),
        )
    }

    @Test
    fun textoParaExibirQuandoHaConteudoDevolveLimpo() {
        assertEquals(
            "olá",
            ExtracaoDeTextoDeImagemViaIaDoNavegador.textoParaExibir("  olá  "),
        )
    }

    @Test
    fun mostrarAcaoSoComChave() {
        assertFalse(ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(false))
        assertTrue(ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(true))
    }

    @Test
    fun botoesDeAcaoSoComTextoUtil() {
        assertFalse(
            ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(
                ExtracaoDeTextoDeImagemViaIaDoNavegador.SEM_TEXTO,
            ),
        )
        assertTrue(ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados("placa 123"))
    }
}
