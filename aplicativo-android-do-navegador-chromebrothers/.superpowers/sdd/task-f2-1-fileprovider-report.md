# Task 1 — FileProvider + URI/MIME (Fase 2)

## Status: DONE

`:app:compileDebugKotlin` → **BUILD SUCCESSFUL** (5s). Sem commit. `ExtracaoDeQuadroJpeg` não foi alterado. Sem UI de arraste.

## Arquivos
- **Alterado:** `app/src/main/res/xml/caminhos_de_arquivo_do_quadro_copiado_chromebrothers.xml`
- **Criado:** `app/src/main/kotlin/com/chromebrothers/navegador/PreparacaoDeUriContentParaArrasteDaBiblioteca.kt`

## Implementação
1. Paths novos, sem remover os existentes:
   - `files-path` `biblioteca-de-videos-higgsfield/`
   - `files-path` `refs-de-geracao-da-biblioteca/`
   - `cache-path` `arraste-da-biblioteca/`
2. `PreparacaoDeUriContentParaArrasteDaBiblioteca.deArquivoLocal`:
   - Authority `${context.packageName}.arquivos` + `FileProvider.getUriForFile`.
   - `null` se o arquivo não é arquivo regular, tem tamanho 0, ou o FileProvider lança exceção.
   - MIME: `mimeForcado` se informado; senão `.mp4` → `video/mp4`, `.jpg`/`.jpeg` → `image/jpeg`, `.png` → `image/png`, `.webp` → `image/webp`, demais → `application/octet-stream` (extensão sem diferenciar maiúsculas).
   - `nome` = `arquivo.name`.
   - KDoc lembra que o call site do drag concede `FLAG_GRANT_READ_URI_PERMISSION`.

## Compile
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin` → BUILD SUCCESSFUL.

## Concerns
- Sem teste instrumentado (o módulo `app` não tem source set de teste; FileProvider exige contexto Android). O plano da Task 1 pede só compilação.
- Arquivos fora desses três paths (por exemplo `biblioteca-de-videos-com-miniatura/` ou `biblioteca-de-imagens-capturadas/` já existente só para imagens) continuam rejeitados pelo FileProvider se não estiverem no XML.
