### Task 2: Frame JPEG de vídeo para arraste (Fase 2)

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

**Create:** `app/src/main/kotlin/com/chromebrothers/navegador/ExtracaoDeQuadroJpegDeVideoLocalParaArraste.kt`

```kotlin
object ExtracaoDeQuadroJpegDeVideoLocalParaArraste {
    /** Grava JPEG em cacheDir/arraste-da-biblioteca/; null se falhar */
    fun extrairParaArquivo(context: Context, videoMp4: File): File?
}
```

- Use MediaMetadataRetriever like GravacaoDaMidiaGerada…quadroDoArquivoLocal (getScaledFrameAtTime ~1s, 960x540)
- Write JPEG quality ~86 to `File(context.cacheDir, "arraste-da-biblioteca")` mkdirs, unique name `quadro-<uuid>.jpg`
- Release retriever; recycle bitmap
- Return null on any failure / empty video

**Do NOT modify FileProvider XML** (Task 1 owns that) — cache path `arraste-da-biblioteca` will be added by Task 1; your code still writes there.

**Compile:** `:app:compileDebugKotlin`
**NO git commits. No UI.**
