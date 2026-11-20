# Componentes flutuantes no editor + envio de `<img>` — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Abrir a coleção flutuante de componentes a partir do editor e fundir uma `<img>` do componente na camada imagem ativa (toque longo na fatia 1; arrastar na fatia 2).

**Architecture:** Reusar `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador` + biblioteca HTML. Regras puras decidem se o destino aceita fundir. Editor expõe `fundirBitmapNaCamadaAtiva`. Bridge `chromebrothers` + script injetado na WebView capturam toque longo (e depois drag) na `<img>`. Fechar o editor chama `encerrarTodas()` nas janelas desse contexto.

**Tech Stack:** Kotlin, Android Views/WebView, módulo `regras-de-video-do-navegador`, TDD nas regras.

**Spec:** `docs/superpowers/specs/2026-11-05-componentes-flutuantes-no-editor-com-envio-de-imagem-para-camada-design.md`

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- TDD nas regras; após mudanças no editor/app: testes relevantes + `assembleDebug` + `adb install`
- Sem commits não pedidos pelo usuário
- Windows PowerShell; Gradle: `$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat` com `JAVA_HOME` Adoptium 21
- YAGNI: fatia 1 completa e instalável antes da fatia 2
- Não criar WebView paralela “só do editor”

## File map

| Arquivo | Papel |
|---------|--------|
| `regras-.../EnvioDeImagemDoComponenteParaCamadaDoEditor.kt` | Destino válido + resultado do gesto |
| `regras-.../TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt` | Testes |
| `EditorDeRecorteETracoDaImagemDaBiblioteca.kt` | `fundirBitmapNaCamadaAtiva` |
| `PainelDaGavetaDaBibliotecaDeVideos.kt` | Botão Componentes; API painel; fechar janelas ao sair |
| `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` | Botão no chrome |
| `strings_do_navegador_chromebrothers.xml` | Labels |
| `PainelDaColecaoFlutuanteDoChatDoNavegador.kt` | Bridge + inject long-press / drag |
| `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt` | Encaminhar callback de envio |
| `AtividadePrincipalDoNavegadorChromebrothers.kt` | Ligar callback → painel/editor |

---

### Task 1: Regras — destino e resultado do envio

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/EnvioDeImagemDoComponenteParaCamadaDoEditor.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt`

**Interfaces:**
- Produces: `ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor`, `EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(...)`, `EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(...)`

- [ ] **Step 1: Write the failing test**

```kotlin
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals

class TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    @Test
    fun imagemAtivaAceitaFundir() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 3,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun folhaVaziaPromoveEFunde() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = 1,
                destinoEhImagem = false,
                destinoEhFolha = true,
            ),
        )
    }

    @Test
    fun semEditorOuSemCamadaRecusa() {
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = false,
                idCamadaAtiva = 1,
                destinoEhImagem = true,
                destinoEhFolha = false,
            ),
        )
        assertEquals(
            ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
                editorAtivo = true,
                idCamadaAtiva = -1,
                destinoEhImagem = false,
                destinoEhFolha = false,
            ),
        )
    }

    @Test
    fun soltarNoQuadroFundeEForaCancela() {
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreOQuadroDoEditor = true,
            ),
        )
        assertEquals(
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR,
            EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar(
                soltouSobreOQuadroDoEditor = false,
            ),
        )
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor"
```

Expected: FAIL (classe/símbolos não existem).

- [ ] **Step 3: Write minimal implementation**

```kotlin
package com.chromebrothers.navegador.regras

enum class ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    FUNDIR_NA_IMAGEM,
    PROMOVER_FOLHA_E_FUNDIR,
    RECUSAR_SEM_DESTINO,
}

enum class ResultadoDoGestoDeArrasteDaImagemDoComponente {
    FUNDIR,
    CANCELAR,
}

object EnvioDeImagemDoComponenteParaCamadaDoEditor {
    fun decidirDestino(
        editorAtivo: Boolean,
        idCamadaAtiva: Int,
        destinoEhImagem: Boolean,
        destinoEhFolha: Boolean,
    ): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
        if (!editorAtivo || idCamadaAtiva == -1) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
        }
        if (destinoEhImagem) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
        }
        if (destinoEhFolha) {
            return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR
        }
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    }

    fun resultadoDoGestoDeSoltar(
        soltouSobreOQuadroDoEditor: Boolean,
    ): ResultadoDoGestoDeArrasteDaImagemDoComponente =
        if (soltouSobreOQuadroDoEditor) {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.FUNDIR
        } else {
            ResultadoDoGestoDeArrasteDaImagemDoComponente.CANCELAR
        }
}
```

- [ ] **Step 4: Run tests — expect PASS** (mesmo comando do Step 2)

---

### Task 2: Editor — `fundirBitmapNaCamadaAtiva`

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/EditorDeRecorteETracoDaImagemDaBiblioteca.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt` (wrapper público)

**Interfaces:**
- Consumes: `EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino`
- Produces: `Editor…fundirBitmapNaCamadaAtiva(bitmap: Bitmap): ResultadoDoEnvio…`
- Produces: `Painel…enviarBitmapDoComponenteParaCamadaAtiva(bitmap: Bitmap): ResultadoDoEnvio…` (só se `editandoImagem`)

- [ ] **Step 1: API no editor**

Adicionar método público que:
1. Lê `idDaCamadaAtiva` e a peça.
2. Chama `decidirDestino(editorAtivo = true, …)`.
3. Se `PROMOVER_FOLHA_E_FUNDIR`: `promoverFolhaParaImagem` (já existente, privado) e atualiza `idDaCamadaAtiva`.
4. Se `FUNDIR_NA_IMAGEM` (ou após promover): cria `Peca.Imagem` temporária com o bitmap, posicionada **centrada** no retângulo da destino, com largura/altura em coords da composição preservando aspect (caber dentro da destino se for maior; senão tamanho natural em px → coords via escala da destino).
5. Insere a peça na lista, chama `fundirPecaTransformadaNaOrigem(idTemp, idDestino)` (já empilha undo).
6. Retorna o enum da regra; em falha de promote → `RECUSAR_SEM_DESTINO`.

Esqueleto:

```kotlin
fun fundirBitmapNaCamadaAtiva(origem: Bitmap): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    var destino = objetos.find { it.id == idDaCamadaAtiva }
    val decisao = EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino(
        editorAtivo = true,
        idCamadaAtiva = idDaCamadaAtiva,
        destinoEhImagem = destino is Peca.Imagem,
        destinoEhFolha = destino is Peca.Folha,
    )
    when (decisao) {
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO -> return decisao
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.PROMOVER_FOLHA_E_FUNDIR -> {
            val folha = destino as? Peca.Folha ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
            destino = promoverFolhaParaImagem(folha)
                ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
            idDaCamadaAtiva = destino.id
        }
        ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM -> Unit
    }
    val imagemDestino = destino as? Peca.Imagem
        ?: return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    val copia = origem.copy(Bitmap.Config.ARGB_8888, false) ?: origem
    val temp = Peca.Imagem(novoId(), copia)
    // dimensionar/centrar em cima de imagemDestino (aspect)
    objetos.add(temp)
    fundirPecaTransformadaNaOrigem(temp.id, imagemDestino.id)
    aoMudarSelecao?.invoke()
    invalidate()
    return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.FUNDIR_NA_IMAGEM
}
```

- [ ] **Step 2: Wrapper no Painel**

```kotlin
fun enviarBitmapDoComponenteParaCamadaAtiva(
    origem: Bitmap,
): ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor {
    if (!editandoImagem) {
        return ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor.RECUSAR_SEM_DESTINO
    }
    return editor.fundirBitmapNaCamadaAtiva(origem)
}
```

Toast/mensagem curta no caller da Activity quando `RECUSAR_SEM_DESTINO` (string nova).

- [ ] **Step 3: Strings**

Em `strings_do_navegador_chromebrothers.xml`:

```xml
<string name="enviar_imagem_do_componente_para_a_camada_do_editor">Enviar à camada</string>
<string name="recusa_envio_de_imagem_do_componente_sem_camada_no_editor">Selecione uma camada de imagem no editor</string>
<string name="componentes_no_chrome_do_editor_de_imagem">Componentes</string>
<string name="falha_ao_ler_imagem_do_componente_para_o_editor">Não foi possível ler a imagem</string>
```

- [ ] **Step 4: Compile check**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Expected: SUCCESS.

---

### Task 3: Bridge + toque longo na `<img>`

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt`

**Interfaces:**
- Consumes: decode URI → Bitmap (reusar caminho de `abrirImagemDoComponenteNoVisorOuEditor` / `chamarAberturaDeMidia`)
- Produces: `@JavascriptInterface fun enviarImagemParaCamadaDoEditor(uri: String?): String`
- Produces: script injetado após load da página do componente

- [ ] **Step 1: Callback nativo**

No Painel da coleção (mesmo padrão dos `ligarAbrirImagemNoEditor`):

```kotlin
private var aoEnviarImagemParaCamadaDoEditor: ((String) -> String)? = null

fun ligarEnviarImagemParaCamadaDoEditor(enviar: (String) -> String) {
    aoEnviarImagemParaCamadaDoEditor = enviar
}
```

Na `PonteJavascriptDaPaginaDaColecao`:

```kotlin
@JavascriptInterface
fun enviarImagemParaCamadaDoEditor(uri: String?): String =
    chamarAberturaDeMidia(uri, aoEnviarImagemParaCamadaDoEditor)
```

No Gestor: espelhar `ligar…` para todas as janelas (como os outros `ligar*`).

Na Activity: ligar callback que:
1. Decodifica URI → Bitmap (mesmo helper usado em `abrirImagemDoComponenteNoVisorOuEditor`).
2. Se decode falhar → toast `falha_ao_ler_imagem…` e retorna mensagem de erro curta.
3. Chama `painelDaBibliotecaDeVideos.enviarBitmapDoComponenteParaCamadaAtiva(bitmap)`.
4. Se `RECUSAR_SEM_DESTINO` → toast `recusa_envio…`.
5. Retorna `"ok"` / erro para o JS.

- [ ] **Step 2: Injetar listener de toque longo**

Após `carregarComponenteDaBiblioteca` / load HTML da página, `evaluateJavascript` com script que:
- Em cada `img`, `contextmenu` (e/ou touch long ~450ms) previne default e chama  
  `chromebrothers.enviarImagemParaCamadaDoEditor(img.currentSrc || img.src)`.
- Não interfere em inputs/links.

Manter o script idempotente (`window.__chromebrothersEnvioImgArmado`).

- [ ] **Step 3: Smoke compile** (`:app:compileDebugKotlin`)

---

### Task 4: Botão Componentes no chrome do editor

**Files:**
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` (`barra_de_acoes_da_imagem_ampliada`)
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (se o abrir componente ficar na Activity)

- [ ] **Step 1: Layout**

`ImageButton` ou `Button` `@+id/botao_componentes_no_chrome_do_editor_de_imagem` na barra de ações da imagem ampliada, `contentDescription` / texto `componentes_no_chrome_do_editor_de_imagem`, visível no chrome de edição (junto de `mostrarChromeDeEdicao` / `GONE` no chrome de visita).

- [ ] **Step 2: Click → abrir componente**

Callback `aoAbrirComponentesDoEditor: (() -> Unit)?` no Painel, wired na Activity para:
1. Listar biblioteca (mesmo store da gaveta/overflow).
2. Mostrar seletor (preferir dialog/caixa **custom** do app, não `AlertDialog` do sistema — padrão já usado no overflow de componentes).
3. Ao escolher item: `painelDaColecao.carregarComponenteDaBiblioteca(item)` **sem** `painelDaBibliotecaDeVideos.esconder()` (diferente do `aoAbrirPagina` da gaveta, que esconde a biblioteca).

- [ ] **Step 3: Visibilidade**

Só mostrar o botão quando `editandoImagem` (chrome de edição).

---

### Task 5: Fechar janelas ao sair do editor

**Files:**
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (passar `encerrarTodas` ou callback)

- [ ] **Step 1:** Callback `aoEncerrarEdicaoDeImagem: (() -> Unit)?` ou ligar direto `painelDaColecao.encerrarTodas()`.

- [ ] **Step 2:** Invocar ao abandonar/cancelar/concluir edição e em `esconder()` quando estava editando — alinhado a `abandonarEdicaoNaMemoria` / `cancelarEdicao` / ramo de edição em `tratarVolta`.

Spec: **fechar** janelas abertas nesse contexto (não deixar órfãs).

---

### Task 6: Verificar fatia 1

- [ ] Rodar testes de regras Task 1
- [ ] `assembleDebug` + `adb install -r` do APK debug
- [ ] Manual: editar imagem → Componentes → abrir → toque longo na `<img>` → funde na camada → desfazer → sair do editor fecha janelas

---

### Task 7: Fatia 2 — arrastar `<img>` até o quadro

**Files:**
- Modify: script JS no `PainelDaColecaoFlutuante…`
- Modify: `EditorDeRecorteETraco…` ou overlay no Painel (hit-test “soltou no quadro”)
- Reuse: `resultadoDoGestoDeSoltar` + `enviarBitmapDoComponenteParaCamadaAtiva`

- [ ] **Step 1:** JS `dragstart` / touch-move longa em `<img>` notifica nativo com URI + coords de tela (bridge `iniciarArrasteDeImagemParaOEditor` / `atualizarArraste` / `soltarArraste`).

- [ ] **Step 2:** Nativo: shadow/feedback leve; no UP, converter coords → se intersecta bounds do `editor` na tela → `FUNDIR`, senão `CANCELAR`.

- [ ] **Step 3:** Mesmo decode + `enviarBitmap…` da fatia 1.

- [ ] **Step 4:** Testes de regra do gesto já cobertos na Task 1; assemble + install; smoke drag no quadro / fora.

---

## Self-review vs spec

| Spec | Task |
|------|------|
| Abrir componentes no editor (mesma coleção) | 4 |
| Entrada no chrome | 4 |
| Máx. 3 / mesmo gestor | 4 (reuso) |
| Fechar janelas ao fechar editor | 5 |
| Toque longo `<img>` → fundir | 1–3 |
| Folha promove | 1–2 |
| Undo | 2 (`fundirPecaTransformadaNaOrigem`) |
| Feedback destino/decode inválido | 2–3 |
| Arrastar → fundir / fora cancela | 7 (+ regras Task 1) |
| Fora de escopo (captura janela, substituir, camada nova) | não planejado |
