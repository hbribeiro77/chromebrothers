# Extração de texto de imagem via IA — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** No toque longo em imagem (página e visor/biblioteca), com chave OpenRouter, extrair texto via IA de forma silenciosa e mostrar modal selecionável com Copiar e Enviar ao chat (preenche o campo).

**Architecture:** Regras puras para prompt/parse/visibilidade; no app, orquestrador compacta bytes → `ClienteDeChatDaOpenRouter.enviar(..., comFerramentas = false)` sem tocar no histórico → modal no padrão das caixas; chat expõe `abrirComTextoNoCampo`.

**Tech Stack:** Kotlin, Android Dialog/layouts existentes, OpenRouter via `ClienteDeChatDaOpenRouter`, `MensagemDeChat` + `ParteDeConteudoDoChat`, `CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador`, JUnit no módulo `regras-de-video-do-navegador`.

## Global Constraints

- Sem OCR on-device na v1
- Sem gravar a chamada OCR no histórico do chat
- Sem tools na chamada OCR
- Botão só com `cofre.temChave()`
- Enviar ao chat = abrir gaveta + preencher campo (não enviar sozinho)
- Strings em pt-BR em `strings_do_navegador_chromebrothers.xml`
- Nomes de arquivo longos e descritivos
- Gradle: `$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat` com `JAVA_HOME` Adoptium jdk-21
- Commits só se o usuário pedir explicitamente (não incluir commits automáticos)

## File structure

| Arquivo | Papel |
|---------|--------|
| `regras-de-video-do-navegador/src/main/kotlin/.../ExtracaoDeTextoDeImagemViaIaDoNavegador.kt` | Prompt, limpeza da resposta, mensagens, gate de visibilidade |
| `regras-de-video-do-navegador/src/test/kotlin/.../TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt` | Testes TDD das regras |
| `app/.../res/layout/layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml` | Modal loading/resultado/erro |
| `app/.../ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt` | Dialog: estados + Copiar / Enviar / Fechar |
| `app/.../OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt` | Bytes → compactar → API → modal; ignora 2º pedido |
| `app/.../CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt` + layout página | Botão + callback extrair |
| `app/.../PainelDaGavetaDaBibliotecaDeVideos.kt` + layout visor | Botão + bytes da imagem aberta |
| `app/.../PainelDoChatDaOpenRouterNoNavegador.kt` | `abrirComTextoNoCampo(texto)` |
| `app/.../AtividadePrincipalDoNavegadorChromebrothers.kt` | Wire cofre/modelo/orquestrador |
| `app/.../res/values/strings_do_navegador_chromebrothers.xml` | Strings |

---

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

### Task 2: Modal UI

**Files:**
- Create: `app/src/main/res/layout/layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml`
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`
- Modify: `app/src/main/res/values/strings_do_navegador_chromebrothers.xml`

**Interfaces:**
- Consumes: `ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados`
- Produces:
  - `class ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(activity, aoCopiar, aoEnviarAoChat)`
  - `fun mostrarCarregando()`
  - `fun mostrarResultado(texto: String)`
  - `fun mostrarErro(mensagem: String)`
  - `fun fechar()`

- [ ] **Step 1: Add strings**

```xml
<string name="extrair_texto_da_imagem">Extrair texto</string>
<string name="titulo_da_extracao_de_texto_da_imagem">Texto da imagem</string>
<string name="extraindo_texto_da_imagem">Extraindo texto…</string>
<string name="copiar_texto_extraido_da_imagem">Copiar</string>
<string name="enviar_texto_extraido_ao_chat">Enviar ao chat</string>
<string name="texto_extraido_copiado">Texto copiado</string>
```

- [ ] **Step 2: Create layout**

Reuse visual da caixa de opções: fundo `@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem`, título, `ProgressBar` + TextView de status (loading), `TextView` com `textIsSelectable="true"` e `maxHeight` ~320dp + scroll, botões Copiar / Enviar ao chat / Fechar (mesmo estilo transparente das outras caixas).

- [ ] **Step 3: Implement modal class**

```kotlin
class ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(
    private val activity: AppCompatActivity,
    private val aoCopiar: (String) -> Unit,
    private val aoEnviarAoChat: (String) -> Unit,
) {
    // Dialog 420dp; mostrarCarregando esconde texto/botões de ação;
    // mostrarResultado/mostrarErro atualiza TextView e habilita Copiar/Enviar via
    // ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados(texto)
}
```

Verificação: `assembleDebug` compila (pode ser junto da Task 3).

---

### Task 3: Orquestrador (compactar + API + modal)

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`

**Interfaces:**
- Consumes: `ClienteDeChatDaOpenRouter`, `CompactacaoDoBitmap...paraParte` ou `preparar`, `ExtracaoDeTextoDeImagemViaIaDoNavegador`, `ModalDa...`
- Produces:
  - `class OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador(activity, cofre, lerModelo, aoAbrirChatComTexto)`
  - `fun extrairDeBytes(bytes: ByteArray)`
  - `fun extrairDeBitmap(bitmap: Bitmap)`

- [ ] **Step 1: Implement**

Fluxo:
1. Se `emAndamento` → return (ignorar 2º toque).
2. Decodificar bytes → Bitmap; `CompactacaoDoBitmapDaCapturaDaAbaParaOChatDoNavegador.paraParte(bitmap)`.
3. Montar `MensagemDeChat("user", ExtracaoDeTextoDeImagemViaIaDoNavegador.promptDoUsuario(), listOf(parte))`.
4. `modal.mostrarCarregando()`; `emAndamento = true`.
5. Cliente **separado** (não o do painel do chat, para não cancelar envio do usuário):  
   `cliente.enviar(chave, modelo, listOf(mensagem), comFerramentas = false, aoTrecho = { acumular }, aoTerminar = { fim -> ... })`.
6. No fim: se `fim.erro != null` → `modal.mostrarErro(mensagemDeFalha(...))`; senão `modal.mostrarResultado(textoParaExibir(acumulado))`.
7. `emAndamento = false`.
8. Copiar: ClipboardManager + toast `texto_extraido_copiado`.
9. Enviar: `modal.fechar()` + `aoAbrirChatComTexto(texto)`.

- [ ] **Step 2: assembleDebug**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:assembleDebug -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

Expected: BUILD SUCCESSFUL

---

### Task 4: `abrirComTextoNoCampo` no chat

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDoChatDaOpenRouterNoNavegador.kt`

**Interfaces:**
- Produces: `fun abrirComTextoNoCampo(texto: String)` — se gaveta fechada chama lógica de `abrir()`; `campoMensagem.setText(texto)`; cursor no fim; foco no campo.

- [ ] **Step 1: Implement**

Tornar `abrir()` acessível (já private — extrair ou chamar de método público novo). Não chamar `enviar()`.

- [ ] **Step 2: Compile** via assembleDebug (junto Task 5 ok).

---

### Task 5: Wire menu da imagem na página

**Files:**
- Modify: `app/src/main/res/layout/layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt`

**Interfaces:**
- Consumes: `ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao`, orquestrador
- Extends `CopiaDeImagem...` com `temChave: () -> Boolean` e `aoExtrairTexto: (ByteArray) -> Unit`

- [ ] **Step 1: Botão no XML** após Salvar (ou antes de Cancelar), id `botao_extrair_texto_da_imagem_da_pagina_no_toque_longo`, text `@string/extrair_texto_da_imagem`, default `visibility="gone"` ou GONE no código.

- [ ] **Step 2: Em `perguntar`**

```kotlin
val extrair = raiz.findViewById<View>(R.id.botao_extrair_texto_da_imagem_da_pagina_no_toque_longo)
extrair.visibility = if (temChave()) View.VISIBLE else View.GONE
extrair.setOnClickListener {
    dialogo.dismiss()
    baixar(vista, endereco, DestinoDaImagemDaPagina.EXTRAIR_TEXTO) // OU callback dedicado após baixar
}
```

Preferência: adicionar `EXTRAIR_TEXTO` ao enum `DestinoDaImagemDaPagina` e em `receberImagemDaPagina` despachar para o orquestrador — **ou** callback `aoExtrairTexto` separado no construtor de `CopiaDeImagem...` chamado com os bytes (mais limpo que misturar biblioteca).

Recomendado no código:

```kotlin
enum class DestinoDaImagemDaPagina { COPIAR, SALVAR, EDITAR, ACRESCENTAR, EXTRAIR_TEXTO }
```

Em `AtividadePrincipal.receberImagemDaPagina`: se `EXTRAIR_TEXTO` → `orquestrador.extrairDeBytes(bytes)`.

Criar orquestrador na Activity com `CofreDaChaveDaOpenRouter`, `{ preferencias.modeloDoChat }`, `{ painelDoChat.abrirComTextoNoCampo(it) }`.

- [ ] **Step 3: assembleDebug**

---

### Task 6: Wire menu do visor/biblioteca

**Files:**
- Modify: `app/src/main/res/layout/layout_caixa_de_opcoes_do_toque_longo_na_imagem_ampliada_do_visor_da_biblioteca.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (passar callback/`temChave`/orquestrador ao painel se necessário)

**Interfaces:**
- Em `mostrarOpcoesDoToqueLongoNaImagemAmpliada`: botão Extrair texto VISIBLE só se `temChave`; ao clicar, obter bytes do item aberto ou PNG do `quadroAmpliado` (mesmo padrão de `copiarImagemAberta`) e chamar orquestrador.

- [ ] **Step 1: XML + listener**

Se o painel não tem referência ao orquestrador, adicionar:

```kotlin
fun ligarExtracaoDeTexto(
    temChave: () -> Boolean,
    extrair: (ByteArray) -> Unit,
)
```

- [ ] **Step 2: Wire na Activity** após criar painel/orquestrador.

- [ ] **Step 3: assembleDebug + adb install**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "...\app\build\outputs\apk\debug\app-debug.apk"
```

- [ ] **Step 4: Smoke manual**

1. Sem chave: botão sumido nos dois menus.  
2. Com chave: extrair na página → modal → copiar.  
3. Enviar ao chat → gaveta abre com texto no campo.  
4. Visor da biblioteca: mesmo fluxo.  
5. Histórico do chat **não** ganha bolhas da OCR.

---

## Spec coverage (self-review)

| Spec | Task |
|------|------|
| Ação nos dois menus | 5, 6 |
| Só com chave | 1 (`mostrarAcao`), 5, 6 |
| Chamada avulsa sem histórico/tools | 3 |
| Modal + Copiar + Enviar (preencher campo) | 2, 3, 4 |
| Loading / erro / sem texto / 2º toque | 1, 2, 3 |
| Compactação multimodal | 3 |

Placeholders: nenhum. Tipos alinhados entre tasks.
