# Task 2 — Frame JPEG de vídeo para arraste (Fase 2)

**Status:** DONE  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

Objeto Android que lê um MP4 local, extrai um quadro e grava JPEG em `cacheDir/arraste-da-biblioteca/`. Devolve o `File` ou `null` se falhar. Sem UI. O XML do FileProvider não foi alterado (Task 1).

`ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo(context, videoMp4)`:

- Recusa vídeo que não é arquivo ou tem tamanho 0. Devolve `null`.
- `MediaMetadataRetriever.setDataSource` no caminho absoluto, no mesmo padrão de `quadroDoArquivoLocal`.
- `getScaledFrameAtTime(1_000_000, OPTION_CLOSEST_SYNC, 960, 540)`.
- JPEG qualidade 86 em `File(context.cacheDir, "arraste-da-biblioteca")`, com `mkdirs`.
- Nome único `quadro-<uuid>.jpg`.
- `compress` falso, arquivo vazio ou qualquer `Exception`: apaga o JPEG parcial e devolve `null`.
- `finally`: recicla o bitmap e chama `retriever.release()`.

## Arquivos

Criado:

- `app/src/main/kotlin/com/chromebrothers/navegador/ExtracaoDeQuadroJpegDeVideoLocalParaArraste.kt`

Nenhum outro arquivo foi alterado. Sem commit.

## Compilação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

```
BUILD SUCCESSFUL in 1s
16 actionable tasks: 16 up-to-date
```

Exit code 0. A classe já estava compilada (outra invocação do Gradle no mesmo instante em que o fonte foi gravado):

- fonte `01:39:39`, 1824 bytes
- `app/build/tmp/kotlin-classes/debug/com/chromebrothers/navegador/ExtracaoDeQuadroJpegDeVideoLocalParaArraste.class` `01:39:40`, 7176 bytes
- `javap`: `public final java.io.File extrairParaArquivo(android.content.Context, java.io.File)`

`:regras-de-video-do-navegador:compileKotlin` ficou `UP-TO-DATE`.

Não há teste instrumentado: o brief marca isso como opcional, e `MediaMetadataRetriever` exige runtime Android.

## Auto-revisão

- Assinatura bate com o brief: `object`, `extrairParaArquivo(context, videoMp4): File?`.
- Tempo ~1s, escala 960×540 e qualidade ~86 batem com `quadroDoArquivoLocal` e com as miniaturas da biblioteca.
- Retriever liberado e bitmap reciclado em todo caminho, inclusive falha.
- Pasta `arraste-da-biblioteca` é só escrita aqui; o `<cache-path>` do FileProvider continua com a Task 1.
