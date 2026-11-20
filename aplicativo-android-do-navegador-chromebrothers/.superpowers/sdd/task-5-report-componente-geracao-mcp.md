# Task 5 — Gravação com id + preview (relatório)

Status: concluído (sem commit).

## Arquivo
- `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`
  - `data class ImagemHiggsfieldGuardada(val id: String, val caminhoArquivo: String)`.
  - `guardarImagemComMetadados(bytes, prompt)` usa `guardarQuadro` (naGaleria = false). Caminho = `File(filesDir/biblioteca-de-imagens-capturadas, item.nomeDoArquivo).absolutePath`. Falha do quadro devolve `null`.
  - `guardarImagem` delega. Sem metadados: `"Gerei a imagem, mas não consegui salvar na biblioteca."`. Com metadados: `sucessoImagem(guardada.id, prompt)` — o mesmo texto de antes.

## Verificação
- `:app:assembleDebug` — **BUILD SUCCESSFUL** in 7s (37 actionable tasks: 5 executed, 32 up-to-date).
- JAVA_HOME: `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`.
- Gradle: `%LOCALAPPDATA%\gradle-8.11.1\bin\gradle.bat` (o projeto não tem `gradlew`).

## Comportamento
- O id é o do item da biblioteca (`quadro-$id.jpg` em `nomeDoArquivo`).
- O caminho aponta para o JPEG que `guardarQuadro` gravou (largura máxima 1920, qualidade 90), não para os bytes originais.
- `guardarVideo` não mudou.

## Preocupações
- A pasta `biblioteca-de-imagens-capturadas` é privada em `GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo`. O caminho repete essa string. Se o construtor mudar de pasta, o preview aponta para outro lugar. `arquivo(nomeDoArquivo)` usaria a mesma instância e confirmaria que o arquivo existe.
- `prompt` entra na assinatura para a delegação e o texto de sucesso. Não é gravado em `ImagemHiggsfieldGuardada`.
- Sem teste unitário: a classe depende de `Context` e de `BitmapFactory`. A checagem desta tarefa é a compilação.
