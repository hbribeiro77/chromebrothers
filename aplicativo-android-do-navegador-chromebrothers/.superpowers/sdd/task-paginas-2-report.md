# Task 2 — GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo

**Status:** DONE  
**Commits:** none

## O que foi entregue

Persistência no `filesDir`, no mesmo estilo das outras gravações de biblioteca.

- Pasta: `filesDir/biblioteca-de-paginas-da-colecao`.
- Índice: `itens.json` com `id`, `titulo`, `atualizadoEm` (sem HTML no JSON).
- HTML: arquivo separado `$id.html`.
- `carregar()` monta `BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador` na ordem do índice e preenche `html` a partir do arquivo.
- `gravar()` reescreve o índice e chama `escreverHtml` de cada item.
- `lerHtml`, `escreverHtml`, `apagarArquivos`.
- Nome de arquivo só aceita id com letra, dígito, `-` ou `_`, até 80 caracteres. UUID passa. Barra, `..` e outros caracteres não viram caminho.

Na `AtividadePrincipalDoNavegadorChromebrothers`, campos `gravacaoDaBibliotecaDePaginas` e `bibliotecaDePaginasDaColecao` (privados, como as outras bibliotecas). No create, depois das bibliotecas de vídeo e de imagem: instancia a gravação e chama `carregar()`. Ainda não há leitor na UI.

## Compilação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

**BUILD SUCCESSFUL in 6s.** Sem teste instrumentado (o brief pediu smoke por compilação). Avisos de `statusBarColor` / `navigationBarColor` já existiam e não vêm desta tarefa.

## Arquivos

- Criado: `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo.kt`
- Alterado: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt`

## Auto-revisão

- `gravar` não apaga HTML órfão. Quem remove um item precisa chamar `apagarArquivos(id)` e depois `gravar`.
- `carregar` pula item sem título, sem HTML ou com id que não serve de nome de arquivo. JSON ilegível devolve biblioteca vazia. Um HTML ilegível é pulado; o resto do índice permanece.
- `sincronizarBibliotecaDeVideos` não recarrega páginas. Fora do pedido desta tarefa.
- Nenhum commit.
