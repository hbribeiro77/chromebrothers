### Task 2: Persistência JSON (vídeo + imagem)

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo.kt`

**Do NOT** create SerializacaoDosMetadados helper unless needed — keep parse in app with `OrigemDaMidiaNaBibliotecaDoNavegador.parse`.

**On read** (both video and image JSON):
```kotlin
origem = OrigemDaMidiaNaBibliotecaDoNavegador.parse(objeto.optString("origem")),
modelo = objeto.optString("modelo"),
prompt = objeto.optString("prompt"),
refs = /* from optJSONArray("refs") → List<String>, blank skipped */,
caminhoLocal = objeto.optString("caminhoLocal"),
exportado = objeto.optBoolean("exportado", false),
```
Missing keys → defaults via parse/empty/false (old JSON must load without crash).

**On write:** put the same fields; `refs` as `JSONArray` of strings.

**Steps:**
1. Extend read/write for videos and images
2. Compile: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin` — PASS
3. No new JVM test required for JSONArray (Android). Optional: small private helper for parsing refs JSONArray to keep carregar clean.

**NO git commits. Do not implement Task 3/4/5.**

Workspace: `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

Depends on Task 1 (already done): Item fields `origem`, `modelo`, `prompt`, `refs`, `caminhoLocal`, `exportado` exist on both Item data classes.
