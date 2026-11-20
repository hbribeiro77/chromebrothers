### Task 1: Regras de OCR (prompt, parse, gate)

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ExtracaoDeTextoDeImagemViaIaDoNavegador.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`

**Interfaces:**
- Produces:
  - `object ExtracaoDeTextoDeImagemViaIaDoNavegador`
  - `fun promptDoUsuario(): String`
  - `fun limparResposta(bruto: String): String` — trim; remove cercas \`\`\` opcionais
  - `fun textoParaExibir(bruto: String): String` — se limpo vazio → mensagem “Não encontrei texto nesta imagem.”
  - `fun mensagemDeFalha(erroApi: String?): String` — erro genérico amigável; se parecer modelo/visão, texto de trocar modelo
  - `fun mostrarAcao(temChave: Boolean): Boolean`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.chromebrothers.navegador.regras

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

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

- [ ] **Step 2: Run test to verify it fails**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador" -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

Expected: FAIL (classe inexistente)

- [ ] **Step 3: Write minimal implementation**

```kotlin
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
        if (t.startsWith("```")) {
            t = t.removePrefix("```").trimStart()
            val nl = t.indexOf('\n')
            if (nl in 0..20 && t.take(nl).all { it.isLetterOrDigit() || it == '-' }) {
                t = t.substring(nl + 1)
            }
            if (t.endsWith("```")) t = t.removeSuffix("```")
            t = t.trim()
        }
        return t
    }

    fun textoParaExibir(bruto: String): String {
        val limpo = limparResposta(bruto)
        return limpo.ifBlank { SEM_TEXTO }
    }

    fun botoesDeAcaoHabilitados(textoExibido: String): Boolean =
        textoExibido.isNotBlank() && textoExibido != SEM_TEXTO

    fun mensagemDeFalha(erroApi: String?): String {
        val e = erroApi.orEmpty().lowercase()
        if (e.contains("vision") || e.contains("image") || e.contains("multimodal") ||
            e.contains("does not support") || e.contains("não suport")
        ) {
            return FALHA_MODELO_SEM_VISAO
        }
        return erroApi?.takeIf { it.isNotBlank() } ?: FALHA_GENERICA
    }

    fun mostrarAcao(temChave: Boolean): Boolean = temChave
}
```

- [ ] **Step 4: Run tests — expect PASS**

Same gradle command as Step 2.

---

