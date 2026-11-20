# Metadados, filtro e detalhe na gaveta (Fase 1) — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax. **Não commit automático** (só se o usuário pedir).

**Goal:** Na gaveta (Vídeos e Imagens → Histórico/Favoritos), filtrar por origem MCP, ver prompt/refs/caminho no modal ⓘ (padrão Chromebrothers) e exportar cópia pública.

**Architecture:** Estender `ItemDaBibliotecaDeVideos` / `ItemDaBibliotecaDeImagensCapturadas` + JSON; save MCP grava origem/prompt/modelo/refs; chips na gaveta; modal `Dialog` dedicado; export vídeo→`Downloads/ChromeBrothers`, imagem→`Pictures/ChromeBrothers`.

**Tech Stack:** Kotlin, JSONObject, MediaStore, `Dialog` + layouts XML, módulo `regras-de-video-do-navegador` (JVM tests), app Android.

**Spec:** `docs/superpowers/specs/2026-11-07-metadados-filtro-e-detalhe-na-gaveta-de-midia-gerada-design.md` (Fase 1 only).

## Global Constraints

- Modal = `Dialog` + visual Chromebrothers — **proibido** `AlertDialog` neste fluxo.
- Export vídeo = `Downloads/ChromeBrothers`; imagem = `Pictures/ChromeBrothers`.
- Itens sem `origem` no JSON → `desconhecido`.
- Download remoto de item só-link (página) **fora** do corte mínimo (botão oculto/desabilitado).
- Fase 2 (arraste) **não** implementar neste plano.
- Build: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat"`; adb: `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.
- Respostas ao usuário em pt-BR; sem commit automático.

## File map

| Arquivo | Papel |
|---------|--------|
| `regras/…/OrigemDaMidiaNaBibliotecaDoNavegador.kt` | Enum/constantes + rótulos + parse |
| `regras/…/BibliotecaDeVideosDoNavegador.kt` | Campos novos no item + `registrar` / filtro |
| `regras/…/BibliotecaDeImagensCapturadasDoNavegador.kt` | Idem imagens |
| `regras/…/FiltroPorOrigemNaBibliotecaDeMidia.kt` | `filtrar(itens, origemSelecionada)` |
| `app/…/GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo.kt` | Ler/gravar campos novos no JSON |
| `app/…/GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo.kt` | Idem |
| `app/…/GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt` | Save com origem/modelo/prompt/refs; chave por MCP |
| `app/…/ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt` | Passar mcp/modelo/refs ao save |
| `app/…/ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.kt` | Export MP4 → MediaStore Downloads |
| `app/res/layout/layout_modal_do_detalhe_da_midia_na_biblioteca.xml` | Modal ⓘ |
| `app/…/PainelDaGavetaDaBibliotecaDeVideos.kt` | Chips, ⓘ, modal, baixar vídeo |
| Strings / ids de botões no layout da gaveta | Chip row + ícone info |

---

### Task 1: Modelo JVM — origem, campos, filtro, registrar

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/OrigemDaMidiaNaBibliotecaDoNavegador.kt`
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/FiltroPorOrigemNaBibliotecaDeMidia.kt`
- Modify: `BibliotecaDeVideosDoNavegador.kt`, `BibliotecaDeImagensCapturadasDoNavegador.kt`
- Test: `TesteDaOrigemEFiltroNaBibliotecaDeMidia.kt`; estender `TesteDaBibliotecaDeVideosDoNavegador.kt` / imagens se existir

**Interfaces:**
- Produces:
  - `object OrigemDaMidiaNaBibliotecaDoNavegador` com consts: `HIGGSFIELD`, `SJINN`, `TOPVIEW`, `PAGINA`, `DESCONHECIDO` (strings lowercase)
  - `fun parse(bruto: String?): String` → conhecida ou `DESCONHECIDO`
  - `fun rotulo(origem: String): String` → "Higgsfield" / "Sjinn" / …
  - `ItemDaBibliotecaDeVideos` + campos default: `origem`, `modelo`, `prompt`, `refs: List<String>`, `caminhoLocal`, `exportado: Boolean`
  - Idem em `ItemDaBibliotecaDeImagensCapturadas`
  - `BibliotecaDeVideos….registrar(..., origem, modelo, prompt, refs, caminhoLocal)` preservando favorito/miniatura do anterior; defaults vazios/`desconhecido`/`false`
  - `fun marcarExportado(chave/id): Item?`
  - `FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(itens, origemOuVazio): List` — vazio/`todos` = lista intacta

- [ ] **Step 1: Teste falhando**

```kotlin
@Test
fun parseOrigemVaziaViraDesconhecido() {
    assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(null))
    assertEquals("desconhecido", OrigemDaMidiaNaBibliotecaDoNavegador.parse(""))
    assertEquals("topview", OrigemDaMidiaNaBibliotecaDoNavegador.parse("Topview"))
}

@Test
fun filtroPorOrigem() {
    val a = ItemDaBibliotecaDeVideos("1", "t", "file://x", true, false, 1L, "", origem = "sjinn")
    val b = a.copy(chave = "2", origem = "topview")
    assertEquals(listOf(a), FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "sjinn"))
    assertEquals(2, FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(listOf(a, b), "todos").size)
}

@Test
fun registrarGuardaPromptEOrigem() {
    val bib = BibliotecaDeVideosDoNavegador()
    bib.registrar("sjinn-1", "curto", "file://a", true, 9L, origem = "sjinn", modelo = "wan3", prompt = "onda grande", refs = listOf("r1.jpg"), caminhoLocal = "/a.mp4")
    val item = bib.buscar("sjinn-1")!!
    assertEquals("sjinn", item.origem)
    assertEquals("onda grande", item.prompt)
    assertEquals(listOf("r1.jpg"), item.refs)
}
```

- [ ] **Step 2: Rodar testes — FAIL** (tipos/funções ausentes)

Run:  
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaOrigemEFiltroNaBibliotecaDeMidia"`

- [ ] **Step 3: Implementar mínimos** — objeto origem, filtro, campos nos data classes, `registrar`/`marcarExportado` preservando campos novos em `copy` de favorito/remover/miniatura.

- [ ] **Step 4: Rodar testes — PASS** (mesmo comando)

---

### Task 2: Persistência JSON (vídeo + imagem)

**Files:**
- Modify: `app/.../GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo.kt`
- Modify: `app/.../GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo.kt`
- Test JVM se houver parser puro; senão smoke via compile + teste de regras que monta item a partir de mapa simulado em `regras` se extrair helper:

Opcional Create: `regras/.../SerializacaoDosMetadadosDaMidiaNaBiblioteca.kt` com:

```kotlin
fun origemDeJson(opt: String?) = OrigemDaMidiaNaBibliotecaDoNavegador.parse(opt)
fun refsDeJson(arrayOuNull: JSONArray?): List<String> // se JSONArray não estiver no módulo JVM, usar List<String> já parseada no app
```

No app, ao ler:

```kotlin
origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(objeto.optString("origem")),
modelo = objeto.optString("modelo"),
prompt = objeto.optString("prompt"),
refs = (0 until objeto.optJSONArray("refs")?.length().orZero()).mapNotNull { ... },
caminhoLocal = objeto.optString("caminhoLocal"),
exportado = objeto.optBoolean("exportado", false),
```

Ao gravar: `put` dos mesmos; `refs` como `JSONArray`.

- [ ] **Step 1:** Estender leitura/gravação; JSON sem chaves novas → defaults.
- [ ] **Step 2:** `:app:compileDebugKotlin` PASS
- [ ] **Step 3:** Teste JVM: `registrar` + simular round-trip dos campos via construção manual do item (já coberto na Task 1); se possível, teste unitário do parse de refs string `"a,b"` não — usar JSONArray no app only.

---

### Task 3: Save MCP com origem, modelo, prompt e refs

**Files:**
- Modify: `GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`
- Modify: `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt` (e qualquer outro call site de `guardarVideoComMetadados` / `guardarImagemComMetadados` no chat)

**Interfaces:**
- Produces:
```kotlin
fun guardarVideoComMetadados(
    bytes: ByteArray,
    prompt: String,
    origem: String,
    modelo: String = "",
    refsBytes: List<Pair<ByteArray, String>> = emptyList(),
): VideoHiggsfieldGuardado?

fun guardarImagemComMetadados(
    bytes: ByteArray,
    prompt: String,
    origem: String,
    modelo: String = "",
    refsBytes: List<Pair<ByteArray, String>> = emptyList(),
): ImagemHiggsfieldGuardada?
```

- Chave vídeo: `"$origem-$uuid"` (ex. `topview-…`, `sjinn-…`, `higgsfield-…`) — pasta continua `biblioteca-de-videos-higgsfield` (YAGNI rename).
- Copiar cada ref para `filesDir/refs-de-geracao-da-biblioteca/<chave>-ref-<i>.<ext>` e passar lista de **nomes de arquivo** em `refs`.
- `caminhoLocal` = absolute path do MP4/JPEG.
- `prompt` completo no item; `titulo` = `prompt.take(80)`.
- Imagem: após `guardarQuadro`, atualizar item com metadados (`origem`, `prompt`, …) — pode exigir `atualizarMetadadosDeGeracao(id, …)` na biblioteca de imagens.

Call sites do serviço: em cada ramo higgsfield/sjinn/topview, passar `mcp` do pedido e `modeloLocal` já resolvido; `refsBytes` = referências atuais do serviço (mesmas usadas na geração).

Chat paths que chamam `guardarVideo` / `guardarImagem`: passar origem conhecida (`higgsfield`/`topview`/…) e modelo se disponível; refs vazias se o chat não tiver.

- [ ] **Step 1:** Assinaturas novas + implementação + atualizar call sites
- [ ] **Step 2:** Compile `:app:compileDebugKotlin` PASS
- [ ] **Step 3:** Teste JVM opcional: não obrigatório se Android File — confiar em aceite manual pós-install

---

### Task 4: Export de vídeo para Downloads/ChromeBrothers

**Files:**
- Create: `app/.../ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.kt`
- Modify: biblioteca vídeo `marcarExportado`

**Interfaces:**
```kotlin
object ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers {
    /** @return content uri string ou null */
    fun exportar(context: Context, arquivoLocal: File, nomeExibido: String): String?
}
```

Espelhar insert MediaStore de `DownloadEmPartesDaMidiaDetectadaNaPagina` / gravações existentes:

```kotlin
put(MediaStore.Downloads.DISPLAY_NAME, nome)
put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
```

Copiar bytes do `arquivoLocal` para o Uri. Imagens: reusar `baixar` / `GravadorDeQuadro…` já existente na gaveta (não reinventar).

- [ ] **Step 1:** Implementar object + chamar de `Painel…` (Task 5)
- [ ] **Step 2:** Compile PASS

---

### Task 5: UI — chips, ⓘ, modal, ações

**Files:**
- Create: `app/src/main/res/layout/layout_modal_do_detalhe_da_midia_na_biblioteca.xml`
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` (HorizontalScrollView de chips sob o subfiltro Histórico/Favoritos, visível em Vídeos e Imagens)
- Modify: layouts dos cartões OU inflate programático do ⓘ em `cartao` / `cartaoDeImagem`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Strings em `res/values/strings.xml`

**Modal XML (essência):**
- Root com `@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem`
- `TextView` título (“Detalhe”), origem+modelo, prompt (`maxLines` alto / ScrollView), `HorizontalScrollView` ou `LinearLayout` horizontal para thumbs de refs, texto do caminho, botões Fechar / Copiar prompt / Copiar caminho / Baixar

**Painel:**
- Estado `origemFiltroAtual: String = "todos"`
- Ao montar lista de recentes/favoritos: `FiltroPorOrigem….filtrar*`
- Chips: toggle → `origemFiltroAtual` → `atualizarLista()`
- Em `cartao` e `cartaoDeImagem`: botão ⓘ → `mostrarModalDoDetalheDaMidia(…)`
- `mostrarModalDoDetalhe…`: `Dialog(activity)`, inflate layout, window transparente, largura 420dp — **igual** `mostrarCaixaDoEditor`; preencher campos; Baixar só se `caminhoLocal` não vazio (ou arquivo existe); imagens com arquivo interno usam `baixar(item)` existente e `marcarExportado`/`naGaleria`; vídeos chamam `ExportacaoDeVideo…` + `marcarExportado`
- Copiar prompt/caminho → `ClipboardManager`

- [ ] **Step 1:** Layouts + wiring chips/ⓘ/modal
- [ ] **Step 2:** Garantir zero `AlertDialog` neste caminho (grep no diff)
- [ ] **Step 3:** `:app:assembleDebug` PASS
- [ ] **Step 4:** `adb install -r app/build/outputs/apk/debug/app-debug.apk`

**Aceite manual:**
1. Gerar 1 vídeo Topview + 1 Sjinn → chips filtram; ⓘ mostra prompt.
2. Baixar vídeo → aparece em Baixados.
3. Gerar imagem MCP → ⓘ + baixar → Pictures/ChromeBrothers.
4. Item antigo → Desconhecido.

---

### Task 6: Verificação final Fase 1

- [ ] **Step 1:** Rodar  
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug`
- [ ] **Step 2:** Install + checklist da spec § Critérios de aceite — Fase 1
- [ ] **Step 3:** Não implementar Fase 2

---

## Spec coverage (self-review)

| Requisito spec Fase 1 | Task |
|-----------------------|------|
| Campos origem/modelo/prompt/refs/caminho/exportado | 1–2 |
| Save MCP com origem real + refs copiadas | 3 |
| Chips filtro | 5 |
| ⓘ + modal Chromebrothers | 5 |
| Export vídeo Downloads/ChromeBrothers | 4–5 |
| Export imagem Pictures/ChromeBrothers | 5 (reuso) |
| Itens antigos desconhecido | 1–2 |
| Sem AlertDialog | 5 |
| Download remoto página fora | 5 (botão só com arquivo local) |
| Fase 2 fora | Global + Task 6 |

## Placeholder scan

Nenhum TBD/TODO restante nas steps; assinaturas e destinos MediaStore explícitos.
