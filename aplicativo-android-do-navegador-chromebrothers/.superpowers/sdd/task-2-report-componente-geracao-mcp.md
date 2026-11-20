# Task 2 — Catálogo: itens JSON para a ponte

**Status:** DONE_WITH_CONCERNS  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

API pública em `ListagemDeModelosDoHiggsfieldNoChat`, reusando `extrairItens` (continua privado) e `TextoJsonDoChat.escapar`.

- `itensParaSelect(bruto): List<Pair<String, String>>` — cada par é `(id, name)` na ordem do array `items`. Item sem `id` continua fora, como em `extrairItens`.
- `jsonDosItensParaPonte(bruto): String` — `{"ok":true,"itens":[{"id":"…","nome":"…"},…]}`. `id` e `nome` passam por `TextoJsonDoChat.escapar`.

O caso do brief (`soul_2` / `Soul`, `output_type` image) produz JSON com `"ok":true`, `soul_2` e `Soul`.

## Arquivos

Alterados:

- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaListagemDeModelosDoHiggsfieldNoChat.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ListagemDeModelosDoHiggsfieldNoChat.kt`

## TDD

Comando (PowerShell, JDK 21 Adoptium, Gradle 8.11.1):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests TesteDaListagemDeModelosDoHiggsfieldNoChat -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

### RED

O teste `jsonDosItensParaPonteExtraiIdENome` foi escrito antes da implementação. Compilação de teste falhou (exit 1, `BUILD FAILED in 1s`):

```
> Task :regras-de-video-do-navegador:compileTestKotlin FAILED
e: .../TesteDaListagemDeModelosDoHiggsfieldNoChat.kt:63:56 Unresolved reference 'jsonDosItensParaPonte'.
```

Falha esperada: o método ainda não existia.

### GREEN

Depois da implementação, o mesmo comando: exit 0, `BUILD SUCCESSFUL in 6s`.

XML `regras-de-video-do-navegador/build/test-results/test/TEST-com.chromebrothers.navegador.regras.TesteDaListagemDeModelosDoHiggsfieldNoChat.xml`:

- `tests="5"` `failures="0"` `errors="0"` `skipped="0"` `time="0.056"`
- `jsonDosItensParaPonteExtraiIdENome()` 0,004s
- `filtroOpcionalEhTrimadoETemLimite()` 0,003s
- `formatarFiltraPorTrechoSemCase()` 0,002s
- `formatarLêItemsJsonDoModelsExplore()` 0,002s
- `semFiltroDevolveCatalogoTruncadoSePreciso()` 0,043s

## Adaptação em relação ao brief

O brief escreve `List<Pair<id,nome>>`. Em Kotlin o tipo é `List<Pair<String, String>>`, com `id` no `first` e o campo JSON `name` no `second` (serializado como `nome`).

`extrairItens` permanece privado. As duas funções públicas só o consomem.

## Self-review

- O spec do componente (`listarModelosGeracao`) pede catálogo `models_explore` **type image**. `formatarCatalogo` já descarta `output_type` diferente de vazio/`image` no filtro padrão. `itensParaSelect` e `jsonDosItensParaPonte` devolvem **todos** os itens com id, inclusive vídeo. O teste do brief só tem um item de imagem e usa `contains`, então um modelo de vídeo também passaria.
- `itensParaSelect` não tem asserção própria. Só entra no teste pelo JSON da ponte.
- O teste não exige o JSON exato, nem escape de aspas/`\` no nome, nem `{"ok":true,"itens":[]}` quando não há `items`.
- Nome em branco vira `"nome":""`. Não há fallback para o id.
