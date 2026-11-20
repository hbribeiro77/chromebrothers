# Task 4 — Export de vídeo para Downloads/ChromeBrothers

**Status:** DONE  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

Objeto Android que copia um arquivo local de vídeo para `Downloads/ChromeBrothers` via MediaStore e devolve a content URI, ou `null` se falhar. Não chama `marcarExportado`. Não liga a gaveta. Não altera JSON de gravação nem o módulo `regras-de-video-do-navegador`.

`ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.exportar(context, arquivoLocal, nomeExibido)`:

- Recusa antes do `insert` quando `arquivoLocal` não é arquivo ou tem tamanho 0. Devolve `null`.
- `DISPLAY_NAME` = `nomeExibido`. Se não termina com `.mp4` (sem distinguir maiúsculas), acrescenta `.mp4`.
- `MIME_TYPE` = `video/mp4`.
- `RELATIVE_PATH` = `Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers"`.
- `IS_PENDING` 1, cópia dos bytes com `copyTo`, depois `IS_PENDING` 0.
- Coleção: `MediaStore.Downloads.getContentUri(VOLUME_EXTERNAL_PRIMARY)`.
- Falha de `insert`, stream nulo ou exceção na cópia/atualização: apaga a URI já criada (quando existe) e devolve `null`. Não relança para a UI.

O fluxo espelha `DownloadEmPartesDaMidiaDetectadaNaPagina.publicar`, com retorno `String?` no lugar de `FalhaNoDownloadDeMidia`.

## Arquivos

Criado:

- `app/src/main/kotlin/com/chromebrothers/navegador/ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.kt`

Nenhum outro arquivo de produção foi alterado. `PainelDaGaveta` e os arquivos de gravação JSON da Task 2 ficaram de fora. Sem commit.

## Compilação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

```
BUILD SUCCESSFUL in 25s
16 actionable tasks: 2 executed, 14 up-to-date
```

Exit code 0. O arquivo novo não gerou warning. Os seis warnings de `statusBarColor` / `navigationBarColor` já existiam em `AtividadeDoPlayerNativoComVelocidadeECapturaDeQuadro.kt` e `AtividadePrincipalDoNavegadorChromebrothers.kt`.

`:regras-de-video-do-navegador:compileKotlin` ficou `UP-TO-DATE`.

Não há teste JVM: MediaStore exige `ContentResolver` do Android, e o brief pede só o objeto mais `compileDebugKotlin`.

## Auto-revisão

- Assinatura bate com o brief: `object`, `exportar(context, arquivoLocal, nomeExibido): String?`.
- Validação de arquivo acontece antes do `insert`.
- Pasta, MIME e ciclo `IS_PENDING` batem com `publicar`.
- Exceção não escapa; URI parcial é apagada.
- `marcarExportado` continua só na biblioteca (Task 1). A Task 5 é quem deve chamar `exportar` e, se a URI voltar, marcar o item.

## Concerns

O retorno de `update` não é conferido. Se o MediaStore devolver 0 linhas, a função ainda devolve a URI e o arquivo pode permanecer `IS_PENDING` 1. O `publicar` do download em partes faz o mesmo.

Nome vazio vira `.mp4`. Um nome que já tem outra extensão (`clip.webm`) vira `clip.webm.mp4`. Caracteres ilegais no nome não são limpos; o `insert` falha e a função devolve `null`.

Não há teste no aparelho. A cópia, a pasta pública e a URI só se confirmam com um `Context` real.

A pasta deste app não é um repositório git (`git status` → `fatal: not a git repository`). Nenhum commit foi criado.

## Correção dos findings importantes

`exportar` agora envolve o corpo inteiro em `try/catch (Exception)`. `insert`, `openOutputStream`, `update` e `delete` não escapam: qualquer `Exception` devolve `null`. O `delete` da URI parcial fica em `apagarUriSemDeixarExcecaoEscapar`, com o próprio `try/catch`, inclusive quando o `catch` externo precisa apagar.

Depois de `update(IS_PENDING=0)`, se o retorno for 0 linhas, a URI é apagada e a função devolve `null`. Não trata arquivo ainda pendente como sucesso.

Assinatura mantida: `fun exportar(context: Context, arquivoLocal: File, nomeExibido: String): String?`. `RELATIVE_PATH` continua `Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers"`. Sem commit.

## Compilação da correção

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

```
BUILD SUCCESSFUL in 2s
16 actionable tasks: 1 executed, 15 up-to-date
```

Exit code 0. `:app:compileDebugKotlin` executou. `:regras-de-video-do-navegador:compileKotlin` ficou `UP-TO-DATE`. Sem warning novo na saída.
