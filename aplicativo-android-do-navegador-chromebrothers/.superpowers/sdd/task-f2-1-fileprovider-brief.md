### Task 1: FileProvider + URI/MIME helper (Fase 2)

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

**Files:**
- Modify: `app/src/main/res/xml/caminhos_de_arquivo_do_quadro_copiado_chromebrothers.xml`
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/PreparacaoDeUriContentParaArrasteDaBiblioteca.kt`

**XML paths to add** (keep existing; add):
- files-path: `biblioteca-de-videos-higgsfield/`
- files-path: `refs-de-geracao-da-biblioteca/`
- cache-path: `arraste-da-biblioteca/`

**Interface:**
```kotlin
object PreparacaoDeUriContentParaArrasteDaBiblioteca {
    data class Payload(val uri: android.net.Uri, val mime: String, val nome: String)
    fun deArquivoLocal(context: Context, arquivo: File, mimeForcado: String? = null): Payload?
}
```
- Authority: `"${context.packageName}.arquivos"`
- FileProvider.getUriForFile
- Return null if !arquivo.isFile || length==0 or FileProvider throws
- MIME from extension unless mimeForcado: .mp4→video/mp4, .jpg/.jpeg→image/jpeg, .png→image/png, .webp→image/webp, else application/octet-stream
- nome = arquivo.name

**Compile:** `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin`
**NO git commits. Do not implement drag UI.**
