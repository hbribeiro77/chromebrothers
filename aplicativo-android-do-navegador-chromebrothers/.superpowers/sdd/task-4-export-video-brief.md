### Task 4: Export de vídeo para Downloads/ChromeBrothers

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.kt`
- `marcarExportado` already exists on BibliotecaDeVideos from Task 1 — do NOT change regras module unless compile forces it. UI wiring of export is Task 5; this task only creates the export object.

**Interface:**
```kotlin
object ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers {
    /** @return content uri string ou null on failure */
    fun exportar(context: Context, arquivoLocal: File, nomeExibido: String): String?
}
```

Mirror MediaStore pattern from `DownloadEmPartesDaMidiaDetectadaNaPagina.kt` `publicar`:
- DISPLAY_NAME = nomeExibido (ensure .mp4 if missing)
- MIME = video/mp4
- RELATIVE_PATH = Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers"
- IS_PENDING 1 → copy bytes from arquivoLocal → IS_PENDING 0
- On failure delete uri / return null (don't throw to UI)

Validate arquivoLocal.isFile && length > 0 before insert.

**Steps:**
1. Create the object
2. Compile: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin` — PASS
3. Do NOT wire PainelDaGaveta yet (Task 5)

**NO git commits. Do not touch JSON Gravacao files (Task 2 owns those).**

Workspace: `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`
