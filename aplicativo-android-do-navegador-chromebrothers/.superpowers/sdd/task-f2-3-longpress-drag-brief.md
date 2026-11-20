### Task 3: Long-press na gaveta → startDragAndDrop

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

**Modify:** `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`

**Also create** (small data class, same package or in Painel file companion):
```kotlin
class EstadoDoArrasteDaBiblioteca(
    val tipo: String, // "image" | "video"
    val caminhoLocal: String,
    val id: String,
)
```

**Gestures:** On `cartao(item)` and `cartaoDeImagem(item)` — NOT on ⓘ button:
```kotlin
cartao.setOnLongClickListener {
    iniciarArrasteDoVideoDaBiblioteca(item) // or imagem
    true
}
```
Keep existing setOnClickListener for open. ⓘ stays separate.

**iniciarArraste…:**
1. Resolve local File from `caminhoLocal` or image library file or file:// of enderecoParaAbrir
2. If no local file: Toast "Só link — baixe ou exporte antes" (or similar pt-BR), return
3. `PreparacaoDeUriContentParaArrasteDaBiblioteca.deArquivoLocal(activity, arquivo)` → Payload or null
4. ClipData.newUri(contentResolver, payload.nome, payload.uri)
5. Optional: set ClipDescription mime
6. DragShadowBuilder — prefer ImageView/Bitmap from miniatura with alpha; else View.DragShadowBuilder(cartao)
7. localState = EstadoDoArrasteDaBiblioteca(...)
8. Flags: `View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ` (API 24+)
9. `view.startDragAndDrop(clip, shadow, localState, flags)`

For video: primary payload is the MP4 file (video/mp4). Frame conversion happens on drop (Task 4).

**Do NOT** implement OnDragListener for MCP yet (Task 4).
**Do NOT** break Fase 1 ⓘ / chips.
**NO AlertDialog. NO git commits.**

Compile `:app:compileDebugKotlin`

Report: `.superpowers/sdd/task-f2-3-longpress-drag-report.md`
