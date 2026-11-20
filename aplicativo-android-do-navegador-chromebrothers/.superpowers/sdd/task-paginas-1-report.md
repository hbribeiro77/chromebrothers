# Task 1 — BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador (em memória)

**Status:** DONE  
**Commits:** none (não há repositório git; nenhum commit foi feito)

## O que foi entregue

Biblioteca pura em Kotlin, no mesmo estilo de lista mutável da `BibliotecaDeImagensCapturadasDoNavegador` (mais recente no índice 0; `listar()` devolve cópia).

- `ItemDaBibliotecaDePaginasHtmlDaColecao` — `id`, `titulo`, `html`, `atualizadoEm`.
- `ResultadoDaBibliotecaDePaginasHtml.Ok(item, criou)` e `Recusada(mensagem)`.
- `guardar` apara título e HTML, recusa vazio ou acima do limite, atualiza e move para o topo quando `idExistente` existe (`criou=false`), senão cria com `novoId()` (`criou=true`).
- No limite (padrão 40, configurável), criação nova devolve `"A biblioteca de páginas chegou ao limite."`; atualização continua.
- `buscar`, `buscarPorTitulo` (trim + ignoreCase, o primeiro da lista), `apagar`, `substituirPor`.

Constantes: título 120, HTML 300_000, itens 40. Mensagens de título/HTML iguais às da publicação: `"Faltou o título."`, `"O título passou do limite."`, `"Faltou o HTML."`, `"O HTML passou do limite."`.

Sanitização de HTML não entra aqui.

## TDD

### RED — teste escrito antes da implementação

Arquivo de teste criado com a classe de produção ainda ausente.

Comando (raiz do app, PowerShell):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador"
```

Resultado: **BUILD FAILED in 1s** (`compileTestKotlin`). Motivo esperado — API ausente:

```
e: .../TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt:12:26 Unresolved reference 'BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador'.
e: ... Unresolved reference 'ResultadoDaBibliotecaDePaginasHtml'.
e: ... Unresolved reference 'ItemDaBibliotecaDePaginasHtmlDaColecao'.
```

`compileKotlin` do main ficou UP-TO-DATE (produção ainda não existia).

### GREEN — implementação mínima e reexecução

O mesmo comando, depois da biblioteca.

Resultado: **BUILD SUCCESSFUL in 5s**. XML em `regras-de-video-do-navegador/build/test-results/test/TEST-com.chromebrothers.navegador.regras.TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.xml`:

- `tests="10"` `failures="0"` `errors="0"` `skipped="0"` `time="0.066"`
- `guardarCriaNoTopoComTituloEHtmlAparados`
- `paginaMaisNovaFicaNaFrente`
- `atualizarPorIdSubstituiConteudoEMoveParaOTopo`
- `idInexistenteCriaItemNovo`
- `aQuarentaEUmaCriacaoERecusadaEAtualizarAindaCabe`
- `limiteInformadoRecusaCriacaoEPermiteAtualizar`
- `tituloEHtmlInvalidosSaoRecusadosEOsLimitesExatosPassam`
- `apagarRemoveOItemEDevolveFalsoSeNaoExistir`
- `buscarPorTituloIgnoraEspacoECaixaEDevolveOMaisRecente`
- `substituirPorTrocaAListaMantendoAOrdemRecebida`

Sem aviso no `system-err`.

## Arquivos

- Criado: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt`
- Criado: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt`

## Auto-revisão

- `idExistente` ausente ou desconhecido cria outro id via `novoId()`; não adota o id informado.
- Títulos repetidos são permitidos. `buscarPorTitulo` devolve o mais recente (primeiro da lista).
- `substituirPor` e o construtor não cortam em 40 nem removem id duplicado. O teto vale na criação por `guardar`. Quem carrega do disco precisa entregar a ordem (mais recente primeiro).
- O tamanho do HTML é medido depois do trim. 120 e 300_000 passam; 121 e 300_001 recusam.
- `novoId` não é chamado em recusa de validação nem em atualização.
- Nenhum commit.
