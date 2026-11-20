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
  - Also `filtrarImagens` for image items

- [ ] **Step 1: Teste falhando** — use the three tests from the plan (parseOrigemVaziaViraDesconhecido, filtroPorOrigem, registrarGuardaPromptEOrigem). Also cover `marcarExportado` and image `registrar` with origem/prompt if cheap.

- [ ] **Step 2: Rodar testes — FAIL**

Run:
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaOrigemEFiltroNaBibliotecaDeMidia"`

- [ ] **Step 3: Implementar mínimos** — objeto origem, filtro, campos nos data classes, `registrar`/`marcarExportado` preservando campos novos em `copy` de favorito/remover/miniatura. Defaults: origem=`desconhecido`, modelo="", prompt="", refs=emptyList(), caminhoLocal="", exportado=false. Existing `registrar(...)` call sites must keep compiling via default params.

- [ ] **Step 4: Rodar testes — PASS** (mesmo comando + ensure existing BibliotecaDeVideos/Imagens tests still pass)

Workspace root:
`D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

**NO git commits** (workspace has no git / user forbids auto-commit).
