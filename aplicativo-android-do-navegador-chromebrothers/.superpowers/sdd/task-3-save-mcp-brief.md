### Task 3: Save MCP com origem, modelo, prompt e refs

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`
- Also update any other call sites of `guardarVideoComMetadados` / `guardarImagemComMetadados` / `guardarVideo` / `guardarImagem` (chat paths)

**New signatures:**
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

**Behavior:**
- Video chave: `"$origemNormalizada-$uuid"` where origemNormalizada = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origem) — folder stays `biblioteca-de-videos-higgsfield`
- Copy each ref to `filesDir/refs-de-geracao-da-biblioteca/<chave>-ref-<i>.<ext>` (ext from second of pair / mime / .jpg default); store **file names** in `refs` list on item
- `caminhoLocal` = absolute path of mp4/jpeg
- Full `prompt` on item; `titulo` = prompt.take(80)
- Pass origem, modelo, prompt, refs, caminhoLocal into `biblioteca.registrar(...)`
- Image: after guardarQuadro, set metadata via new `atualizarMetadadosDeGeracao` on BibliotecaDeImagensCapturadas OR re-register / copy fields — add method on regras if needed:
  `fun atualizarMetadadosDeGeracao(id, origem, modelo, prompt, refs, caminhoLocal): Item?`
- `guardarVideo`/`guardarImagem` wrappers: require origem param or default higgsfield for chat Higgsfield paths

**Servico call sites:** each higgsfield/sjinn/topview branch passes mcp id as origem, resolved modelo, and current reference bytes list used for generation.

**Chat paths:** pass known origem (higgsfield/topview/sjinn); modelo if available; refs empty OK.

**Compile:** `:app:compileDebugKotlin` PASS

**NO git commits. Do not implement UI (Task 5) or Export class (Task 4).**

Workspace: `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

Task 1+2 done: Item fields + JSON persist exist. registrar preserves metadata when null args.
