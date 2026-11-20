# Review package Task 1 OCR (after fix)
Base: empty
Head: after Important fixes
Commits: none

## Files
### ExtracaoDeTextoDeImagemViaIaDoNavegador.kt
```
package com.chromebrothers.navegador.regras

object ExtracaoDeTextoDeImagemViaIaDoNavegador {
    const val SEM_TEXTO = "Não encontrei texto nesta imagem."
    const val FALHA_GENERICA = "Não deu para extrair o texto. Tente de novo."
    const val FALHA_MODELO_SEM_VISAO =
        "Este modelo não lê imagem. Troque o modelo no chat e tente de novo."

    fun promptDoUsuario(): String =
        "Extraia todo o texto visível nesta imagem. Devolva somente o texto literal, " +
            "sem comentários, sem markdown e sem introdução."

    fun limparResposta(bruto: String): String {
        var t = bruto.trim()
        if (!t.startsWith("```")) return t
        t = t.removePrefix("```")
        val fimDaLinhaDaCerca = t.indexOf('\n')
        if (fimDaLinhaDaCerca >= 0) {
            t = t.substring(fimDaLinhaDaCerca + 1)
        }
        t = t.trimEnd()
        if (t.endsWith("```")) t = t.removeSuffix("```")
        return t.trim()
    }

    fun textoParaExibir(bruto: String): String {
        val limpo = limparResposta(bruto)
        return limpo.ifBlank { SEM_TEXTO }
    }

    fun botoesDeAcaoHabilitados(textoExibido: String): Boolean =
        textoExibido.isNotBlank() && textoExibido != SEM_TEXTO

    fun mensagemDeFalha(erroApi: String?): String {
        val e = erroApi.orEmpty().lowercase()
        val semVisao =
            e.contains("vision") ||
                e.contains("multimodal") ||
                e.contains("does not support") ||
                e.contains("não suport") ||
                e.contains("image_url")
        if (e.isNotBlank() && semVisao) return FALHA_MODELO_SEM_VISAO
        return FALHA_GENERICA
    }

    fun mostrarAcao(temChave: Boolean): Boolean = temChave
}

```

### TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt
```
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

```
