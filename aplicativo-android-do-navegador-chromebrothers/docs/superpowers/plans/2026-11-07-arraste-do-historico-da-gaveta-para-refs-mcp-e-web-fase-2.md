# Arraste do histórico da gaveta → refs MCP e web (Fase 2) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax. **Não commit automático** (só se o usuário pedir).

**Goal:** Toque longo no cartão do histórico (imagem e vídeo) inicia arraste com thumbnail translúcida; soltar no gerador MCP adiciona referência; soltar em página web (ex. ChatGPT) entrega arquivo via DnD de sistema.

**Architecture:** Dois caminhos a partir do mesmo long-press: (A) `startDragAndDrop` + `ClipData` + `FileProvider` `content://` para WebView/página; (B) hit-test nativo sobre janelas da coleção MCP → `Servico.adicionarReferencia` + callback HTML `__chromebrothersImagemGeracao`. Não reutilizar o lift-drag coleção→editor (cancela sobre a coleção).

**Tech Stack:** Kotlin, `View.startDragAndDrop`, `ClipData`, `FileProvider`, MediaStore/File, WebView Firefox, HTML MCP na coleção.

**Spec:** `docs/superpowers/specs/2026-11-07-metadados-filtro-e-detalhe-na-gaveta-de-midia-gerada-design.md` § Fase 2.

## Global Constraints

- Gestos: toque curto = abrir mídia; ⓘ = detalhe (Fase 1); **longo = arraste** (não abre modal).
- Vídeo: preferir arquivo MP4 (`video/*`); se alvo MCP só aceita imagem → frame JPEG com Toast curto.
- Sem `AlertDialog` novo neste fluxo.
- Não substituir o lift-drag coleção→editor.
- Build: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat"`; adb: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.
- Respostas pt-BR; sem commit automático.
- Workspace pode não ter git.

## File map

| Arquivo | Papel |
|---------|--------|
| `res/xml/caminhos_de_arquivo_do_quadro_copiado_chromebrothers.xml` | Paths FileProvider: vídeos + refs + cache de staging |
| `PreparacaoDeUriContentParaArrasteDaBiblioteca.kt` | Resolve File → content:// + MIME |
| `ExtracaoDeQuadroJpegDeVideoLocalParaArraste.kt` (app ou regras) | Frame JPEG de MP4 local |
| `PainelDaGavetaDaBibliotecaDeVideos.kt` | Long-press nos cartões + inicia drag |
| `AtividadePrincipalDoNavegadorChromebrothers.kt` | Drop listener / orquestração soltar na coleção MCP |
| `PainelDaColecaoFlutuante…` + HTML MCP | Aceitar drop → `adicionarReferencia` + JSON refs |
| `ServicoDeGeracao…` | Já tem `adicionarReferencia` — reusar |

---

### Task 1: FileProvider + URI/MIME helper

**Files:**
- Modify: `app/src/main/res/xml/caminhos_de_arquivo_do_quadro_copiado_chromebrothers.xml`
- Create: `app/.../PreparacaoDeUriContentParaArrasteDaBiblioteca.kt`

**Paths a acrescentar** (files-path ou cache-path):
- `biblioteca-de-videos-higgsfield/`
- `refs-de-geracao-da-biblioteca/`
- `cache/arraste-da-biblioteca/` (staging se precisar copiar)

**Interface:**
```kotlin
object PreparacaoDeUriContentParaArrasteDaBiblioteca {
    data class Payload(val uri: android.net.Uri, val mime: String, val nome: String)
    fun deArquivoLocal(context: Context, arquivo: File, mimeForcado: String? = null): Payload?
}
```
- Authority: `${context.packageName}.arquivos` (já no Manifest)
- `FLAG_GRANT_READ_URI_PERMISSION` documentado no call site do drag
- MIME: image/jpeg|png|webp por extensão; video/mp4 para .mp4; fallback `application/octet-stream`

- [ ] **Step 1:** Estender XML paths
- [ ] **Step 2:** Implementar helper
- [ ] **Step 3:** `:app:compileDebugKotlin` PASS

---

### Task 2: Frame JPEG de vídeo (para alvo só-imagem)

**Files:**
- Create: `app/.../ExtracaoDeQuadroJpegDeVideoLocalParaArraste.kt`

```kotlin
object ExtracaoDeQuadroJpegDeVideoLocalParaArraste {
    /** Grava JPEG em cache/arraste-da-biblioteca/; null se falhar */
    fun extrairParaArquivo(context: Context, videoMp4: File): File?
}
```
Espelhar `MediaMetadataRetriever` de `GravacaoDaMidiaGerada…quadroDoArquivoLocal` / `gravarQuadroDoVideoParaArraste`.

- [ ] **Step 1:** Implementar + compile PASS
- [ ] **Step 2:** Sem teste instrumentado obrigatório

---

### Task 3: Long-press na gaveta → startDragAndDrop

**Files:**
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`

**Gestos no `cartao` / `cartaoDeImagem` (não no ⓘ):**
```kotlin
cartao.setOnLongClickListener {
    iniciarArrasteDoItemDaBiblioteca(item) // ou overload imagem
    true
}
```

`iniciarArraste…`:
1. Resolver `File` local (`caminhoLocal` ou path da biblioteca de imagens/`file://`)
2. Se vídeo e vamos oferecer também shadow de imagem: payload primário = vídeo; ClipData com URI
3. `ClipData.newUri(resolver, label, contentUri)` + mime
4. `View.DragShadowBuilder` com bitmap da miniatura (alpha ~0.82) se fácil; senão default shadow
5. `cartao.startDragAndDrop(clip, shadow, localState, View.DRAG_FLAG_GLOBAL or DRAG_FLAG_GLOBAL_URI_READ)`  
   - API 24+: `DRAG_FLAG_GLOBAL` para atravessar janelas; conceder read na URI

**localState** (ou tag): enum/objeto `ArrasteDaBiblioteca(tipo=IMAGE|VIDEO, caminhoLocal, chaveId)` para o drop nativo na coleção reconhecer origem sem depender só do ClipData.

Se não há arquivo local: Toast “Só link — baixe antes” e return true (consome long-press sem abrir).

- [ ] **Step 1:** Wiring long-press vídeo + imagem
- [ ] **Step 2:** Compile PASS; ⓘ e click curto intactos

---

### Task 4: Drop na coleção MCP → adicionarReferencia

**Files:**
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` e/ou `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Possibly HTML: opcional highlight `#refs` on dragover (nice-to-have)

**Comportamento:**
1. Registrar `OnDragListener` na raiz da janela flutuante da coleção (quando o HTML for geracao-mcp-imagem ou geracao-mcp-video) **ou** na Activity cobrindo hit-test das bounds da coleção.
2. `ACTION_DROP`:
   - Se `localState` é arraste da biblioteca **ou** ClipData tem content URI:
   - Ler bytes (resolver.openInputStream)
   - Se MIME video/* e componente é gerador de **imagem** ou ref de vídeo exige imagem: extrair frame JPEG (Task 2), Toast “Usei um frame do vídeo como referência”
   - Se gerador de vídeo aceitar só imagem como ref hoje (confirmado no HTML/serviço): sempre imagem/frame para refs
   - `servico.adicionarReferencia(bytes, nome, mime)`
   - `entregarImagemDeReferenciaDaGeracao` / mesmo JSON que o picker usa → WebView atualiza `#refs`
3. `ACTION_DRAG_STARTED` / `LOCATION`: return true se a coleção MCP de geração estiver visível e for alvo válido
4. Não interferir no fantasma coleção→editor (listeners distintos)

**Regra JVM opcional** em `regras`: `DestinoDoArrasteDaBiblioteca.decidir(mime, tipoComponente) → USAR_ARQUIVO | USAR_FRAME | RECUSAR` + teste unitário.

- [ ] **Step 1:** Listener + wiring adicionarReferencia
- [ ] **Step 2:** Teste JVM do decidir (se criado)
- [ ] **Step 3:** Compile PASS

---

### Task 5: Drop na WebView da página (ChatGPT etc.)

**Files:**
- Modify: superfície WebView da página em `AtividadePrincipal…` / `SuperficieDaPaginaNoMotorDoFirefox…` conforme o projeto recebe drag

Com `DRAG_FLAG_GLOBAL` + `content://` FileProvider, muitos WebViews já aceitam drop em `<input type=file>`. Verificar:
1. WebView/Gecko permite drop externo
2. Se não: documentar limitação; fallback Toast “Solte no gerador MCP ou use baixar”

Se o motor **não** propaga drop: Task 5 entrega o drag system-wide mesmo assim (funciona em apps externos) + nota no report; não bloquear Fase 2.

- [ ] **Step 1:** Confirmar flags + testar no aparelho com página de file input / ChatGPT
- [ ] **Step 2:** Ajuste mínimo se permissão URI faltar (`context.grantUriPermission` / clip flags)
- [ ] **Step 3:** Reportar resultado do teste manual

---

### Task 6: Assemble, install, aceite

- [ ] `:app:assembleDebug` + `adb install -r`
- [ ] Checklist:
  1. Long-press imagem histórico → fantasma; soltar no gerador imagem → ref aparece
  2. Long-press vídeo → soltar no gerador vídeo → ref (frame se preciso) + Toast
  3. Long-press → soltar em ChatGPT/file input (melhor esforço)
  4. ⓘ e toque curto intactos
  5. Sem Fase 2 quebrando lift-drag coleção→editor

---

## Spec coverage

| Requisito Fase 2 | Task |
|------------------|------|
| Long-press thumbnail | 3 |
| Soltar MCP → ref | 4 |
| Soltar web / content:// | 1, 3, 5 |
| Vídeo arquivo vs frame | 2, 4 |
| ⓘ / curto intactos | 3, 6 |
| Não substituir lift editor | Global + 4 |

## Placeholder scan

Assinaturas e authority FileProvider explícitas; sem TBD.
