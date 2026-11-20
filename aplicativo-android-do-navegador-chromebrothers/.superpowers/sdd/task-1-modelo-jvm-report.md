# Task 1 — Modelo JVM: origem, campos, filtro, registrar

**Status:** DONE_WITH_CONCERNS  
**Commits:** nenhum

## O que foi implementado

Modelo JVM da biblioteca de mídia, sem UI Android e sem persistência JSON.

- `OrigemDaMidiaNaBibliotecaDoNavegador`: constantes minúsculas `higgsfield`, `sjinn`, `topview`, `pagina`, `desconhecido`.
- `parse(bruto: String?)`: trim + `lowercase(Locale.ROOT)`; valor conhecido ou `desconhecido` (null, vazio e texto fora da lista).
- `rotulo(origem)`: Higgsfield, Sjinn, Topview, Página, Desconhecido.
- `ItemDaBibliotecaDeVideos` e `ItemDaBibliotecaDeImagensCapturadas` ganharam, no fim e com default: `origem = desconhecido`, `modelo = ""`, `prompt = ""`, `refs = emptyList()`, `caminhoLocal = ""`, `exportado = false`.
- `registrar` de vídeo e de imagem aceita `origem`, `modelo`, `prompt`, `refs`, `caminhoLocal` com esses defaults. A origem gravada passa por `parse`.
- Vídeo: novo `registrar` continua preservando favorito e miniatura; também preserva `exportado`. `definirFavorito`, `definirMiniatura` e `removerDoHistorico` usam `copy` e portanto mantêm os campos novos.
- Imagem: `marcarExportado(id)` e vídeo `marcarExportado(chave)` devolvem o item ou null. `copy` de favorito/galeria/remoção preserva os metadados.
- `FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos` e `filtrarImagens`: vazio ou `todos` (ignorando maiúsculas e espaços) devolve a lista recebida; qualquer outro texto filtra pela origem já normalizada.

## Arquivos

Criados:

- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/OrigemDaMidiaNaBibliotecaDoNavegador.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/FiltroPorOrigemNaBibliotecaDeMidia.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaOrigemEFiltroNaBibliotecaDeMidia.kt`

Alterados:

- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/BibliotecaDeVideosDoNavegador.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/BibliotecaDeImagensCapturadasDoNavegador.kt`

Os testes antigos de vídeo e imagem não foram duplicados: os casos novos (parse, rótulo, filtro, registrar, marcarExportado, imagem) ficaram na classe nova. Os testes antigos foram reexecutados e continuam verdes.

## TDD

### RED

Comando (cwd do app Android):

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaOrigemEFiltroNaBibliotecaDeMidia"
```

Resultado: `BUILD FAILED` em ~17s. Tarefa `:regras-de-video-do-navegador:compileTestKotlin` FAILED.

Motivo esperado (tipos e funções ainda ausentes, não erro de assertion):

- `Unresolved reference 'OrigemDaMidiaNaBibliotecaDoNavegador'`
- `Unresolved reference 'FiltroPorOrigemNaBibliotecaDeMidia'`
- `No parameter with name 'origem' | 'modelo' | 'prompt' | 'refs' | 'caminhoLocal'`
- `Unresolved reference 'marcarExportado'`, `origem`, `prompt`, `exportado`, `refs`

### GREEN

Mesmo comando, mais as classes já existentes:

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaOrigemEFiltroNaBibliotecaDeMidia" --tests "com.chromebrothers.navegador.regras.TesteDaBibliotecaDeVideosDoNavegador" --tests "com.chromebrothers.navegador.regras.TesteDaBibliotecaDeImagensCapturadasDoNavegador"
```

Resultado: `BUILD SUCCESSFUL`. XML em `regras-de-video-do-navegador/build/test-results/test/` (timestamp 2026-11-07T04:07:31), 0 falhas:

- `TesteDaOrigemEFiltroNaBibliotecaDeMidia`: 10 testes
- `TesteDaBibliotecaDeVideosDoNavegador`: 9 testes
- `TesteDaBibliotecaDeImagensCapturadasDoNavegador`: 5 testes

O `parse` passou a usar `Locale.ROOT` depois do primeiro verde; a suíte foi rodada de novo e permaneceu verde.

## Autorevisão

- Assinaturas e os três testes do plano (`parseOrigemVaziaViraDesconhecido`, `filtroPorOrigem`, `registrarGuardaPromptEOrigem`) estão cobertos. Há testes extras de rótulo, filtro vazio, imagens, defaults e `marcarExportado`.
- Campos novos ficam no fim, com default, então os construtores e `registrar(...)` atuais continuam compilando sem argumento novo.
- Favoritar, miniatura e tirar do histórico não apagam origem/prompt/refs: o `copy` só troca o campo pedido, e há teste disso no vídeo e no favorito da imagem.
- `marcarExportado` só liga `exportado = true`. Não há como desmarcar. Re-registrar o mesmo vídeo ou a mesma imagem mantém o flag.
- Filtro `todos` / em branco devolve a mesma lista, sem cópia.

## Preocupações

1. **Re-registrar sem os argumentos novos zera os metadados.** `origem`, `modelo`, `prompt`, `refs` e `caminhoLocal` são gravados com o que foi passado. O default é `desconhecido` / vazio. O player (`registrarVideoAtual`, `abrirItemDaBiblioteca`) e `registrarVideoDaPaginaNaBiblioteca` chamam `registrar` só com chave, título, endereço e instante. Quando a Task 3 gravar origem e prompt, reabrir o item por esses caminhos apaga esses campos. Favorito, miniatura e `exportado` sobrevivem. A Task 3 precisa repassar os metadados em todo `registrar`, ou este método precisa conservar o valor anterior quando o argumento vier no default.
2. **`registrar` de imagem passou a manter favorito, `uriDaGaleria`, `nomeExibido` e `exportado` do id anterior.** Antes o favorito voltava sempre para `false` e a galeria era descartada. Nenhum teste antigo regrava o mesmo id (o quadro novo usa UUID), então a suíte continua verde. É mudança de comportamento, alinhada ao vídeo.
3. **JSON ainda não grava os campos.** Fora desta tarefa (Task 2). Itens em memória nascem com default; o disco só passa a enxergar origem/prompt quando a gravação for estendida.
4. **O módulo `:app` não foi recompilado.** A compatibilidade dos call sites é por parâmetro default no fonte Kotlin. Não houve `compileDebugKotlin`.

## Correção — re-registrar não apaga metadados

A preocupação 1 está resolvida. `BibliotecaDeVideosDoNavegador.registrar` e `BibliotecaDeImagensCapturadasDoNavegador.registrar` passaram a aceitar `origem`, `modelo`, `prompt`, `refs` e `caminhoLocal` como anuláveis, com default `null`. `null` significa manter o valor anterior; se não houver item anterior, origem vira `desconhecido` e os demais campos ficam vazios. Valor explícito (inclusive string vazia ou lista vazia) substitui o anterior. `exportado` continua vindo do item anterior.

Call sites do player e da página (`registrarVideoAtual`, `abrirItemDaBiblioteca`, `registrarVideoDaPaginaNaBiblioteca`) e o registro de quadro só passam os argumentos antigos, então o default `null` preserva o que já estava salvo. Nenhum commit.

### RED

Testes novos em `TesteDaOrigemEFiltroNaBibliotecaDeMidia` antes da mudança de assinatura:

- `registrarDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza` — falhou em `TesteDaOrigemEFiltroNaBibliotecaDeMidia.kt:103` (`AssertionFailedError`: origem virou `desconhecido`).
- `registrarImagemDeNovoSemArgumentosNovosPreservaMetadadosEOrigemExplicitaAtualiza` — falhou em `TesteDaOrigemEFiltroNaBibliotecaDeMidia.kt:128` (mesmo efeito no id da imagem).

`2 tests completed, 2 failed`. `BUILD FAILED`.

### GREEN

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaOrigemEFiltroNaBibliotecaDeMidia" --tests "com.chromebrothers.navegador.regras.TesteDaBibliotecaDeVideosDoNavegador" --tests "com.chromebrothers.navegador.regras.TesteDaBibliotecaDeImagensCapturadasDoNavegador"
```

`BUILD SUCCESSFUL` em 6s. XML em `regras-de-video-do-navegador/build/test-results/test/` (timestamp `2026-11-07T04:10:04`), 0 falhas, 26 testes:

- `TesteDaOrigemEFiltroNaBibliotecaDeMidia`: 12 testes (os 2 novos cobrem vídeo e imagem: segundo `registrar` só com argumentos antigos preserva origem/prompt/refs/caminhoLocal; `origem` explícita ainda atualiza e o resto permanece).
- `TesteDaBibliotecaDeVideosDoNavegador`: 9 testes.
- `TesteDaBibliotecaDeImagensCapturadasDoNavegador`: 5 testes.
