### Task 4: Drop na coleção MCP → adicionarReferencia

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

## JVM rule (create + test)
Create `regras-de-video-do-navegador/.../DestinoDoArrasteDaBibliotecaParaReferenciaMcp.kt`:
```kotlin
object DestinoDoArrasteDaBibliotecaParaReferenciaMcp {
    enum class Acao { USAR_ARQUIVO, USAR_FRAME, RECUSAR }
    /** tipoComponente: "imagem" | "video" (gerador MCP). mime: content mime or empty. tipoArraste: "image"|"video" from EstadoDoArraste */
    fun decidir(tipoArraste: String, mime: String, tipoComponente: String): Acao
}
```
Rules:
- RECUSAR if tipoComponente blank/unknown
- If tipoArraste image OR mime starts with image/ → USAR_ARQUIVO (both imagem and video generators accept image refs)
- If tipoArraste video OR mime starts with video/:
  - USAR_FRAME (generators today use image refs only)
- else RECUSAR

Test: `TesteDoDestinoDoArrasteDaBibliotecaParaReferenciaMcp.kt` with a few cases.
Run gradle `:regras-de-video-do-navegador:test --tests "*DestinoDoArraste*"`

## Android drop wiring
Reuse existing flow in AtividadePrincipal:
- `definirReferenciaDaGeracaoEMontarJson` / `servico.adicionarReferencia` / `painel.entregarImagemDeReferenciaDaGeracao`

Detect when a floating collection window showing geracao-mcp-imagem OR geracao-mcp-video is visible. Hook `OnDragListener` on that window's root view (or Activity overlay that hit-tests collection bounds).

On ACTION_DROP:
1. Prefer `event.localState as? EstadoDoArrasteDaBiblioteca` (check actual class name from Task 3 — may be `EstadoDoArrasteBiblioteca` / `EstadoDoArrasteDaBibliotecaDaGaveta`)
2. Else read ClipData URI
3. Open file/bytes; if decidir → USAR_FRAME, call ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo and read those bytes; Toast "Usei um frame do vídeo como referência"
4. Call same path as gallery picker: adicionarReferencia + entregarImagemDeReferenciaDaGeracao
5. Return true

On ACTION_DRAG_STARTED: return true if MCP generation collection is a valid target (so drag continues).
Do NOT break coleção→editor lift-drag fantasma.

Need a way to know "current collection is image/video MCP generator": inspect HTML title/tags/componente id — follow how PainelDaColecao identifies geracao-mcp-imagem / geracao-mcp-video.

**NO AlertDialog. NO git commits.**

Also wire Activity if PainelDaGaveta cannot see collection — Prefer AtividadePrincipal as orchestrator with access to both gaveta and colecao.

Compile app + regras tests.

Report: `.superpowers/sdd/task-f2-4-drop-mcp-report.md`
