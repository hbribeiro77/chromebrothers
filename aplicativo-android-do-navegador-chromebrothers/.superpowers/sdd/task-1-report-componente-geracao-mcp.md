# Task 1 — Regras: parse do pedido e JSON de resposta

**Status:** DONE_WITH_CONCERNS  
**Commits:** nenhum (pedido explícito: não commitar; não há git nesta etapa)

## O que foi entregue

Regras puras em `regras-de-video-do-navegador`, pacote `com.chromebrothers.navegador.regras`. Sem código Android.

- `PedidoDeGeracaoDeImagemViaMcpNaColecao` (data class: `mcp`, `prompt`, `modelo`, `quantidade`, `temReferencia`)
- `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(json)` no companion object
- `ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok` / `Recusado`
- `RespostaJsonDaPonteDeGeracaoMcp.erro` / `okAndamento` / `okMcps` / `okLimpar`

`ler` reusa `TextoJsonDoChat.valorDaChave`, `inteiroDaChave` e `GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_PROMPT` (4000). `erro` reusa `TextoJsonDoChat.escapar`.

Regras de recusa, nesta ordem:

1. prompt vazio depois do trim → `"Faltou o prompt."`
2. prompt maior que o limite → `"O prompt passou do limite."`
3. `mcp` fora de `MCPS_CONHECIDOS` (`higgsfield`) → `"MCP não suportado: …"`
4. quantidade fora de 1..4 → `"A quantidade deve ser entre 1 e 4."`

`mcp` ausente ou em branco cai em `higgsfield`. `quantidade` ausente cai em 1. `modelo` ausente fica string vazia.

## Arquivos

Criados:

- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/PedidoDeGeracaoDeImagemViaMcpNaColecao.kt`

Nenhum arquivo existente foi alterado.

## TDD

Comando (PowerShell, JDK 21 Adoptium, Gradle 8.11.1):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

### RED

O teste foi escrito antes da implementação, com os dois casos do brief (`exigePromptEQuantidadeEntre1e4`, `recusaMcpDesconhecido`). A compilação de teste falhou (exit 1, `BUILD FAILED in 14s`):

```
> Task :regras-de-video-do-navegador:compileTestKotlin FAILED
e: .../TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt Unresolved reference 'ResultadoDoPedidoDeGeracaoDeImagemViaMcp'.
e: .../TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt Unresolved reference 'PedidoDeGeracaoDeImagemViaMcpNaColecao'.
Execution failed for task ':regras-de-video-do-navegador:compileTestKotlin'.
```

Falha esperada: tipos ainda não existiam. Não foi falha de asserção.

### GREEN

Depois da implementação, o mesmo comando: exit 0, `BUILD SUCCESSFUL in 8s`.

XML `regras-de-video-do-navegador/build/test-results/test/TEST-com.chromebrothers.navegador.regras.TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.xml`:

- `tests="2"` `failures="0"` `errors="0"` `skipped="0"` `time="0.045"`
- `exigePromptEQuantidadeEntre1e4()` 0,003s
- `recusaMcpDesconhecido()` 0,041s

## Adaptação em relação ao brief

O brief declara `data class PedidoDeGeracaoDeImagemViaMcpNaColecao` e `object PedidoDeGeracaoDeImagemViaMcpNaColecao` no mesmo pacote. Kotlin recusa duas declarações de topo com o mesmo nome. `ler`, `MCP_HIGGSFIELD` e `MCPS_CONHECIDOS` foram para o `companion object` da data class. A chamada `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(json)` e o tipo do pedido permanecem os do teste.

O restante do `ler` e de `RespostaJsonDaPonteDeGeracaoMcp` segue o código do brief.

## Self-review

- Prompt vazio é recusado antes da quantidade e do MCP. O teste de prompt vazio só exige `Recusado`, sem checar a mensagem.
- MCP `"outro"` com prompt preenchido e quantidade 1 cai em `Recusado`. O teste não exige o texto `"MCP não suportado: outro"`.
- Pedido válido do teste: mcp `higgsfield`, quantidade 2, `temReferencia == true`. O modelo `nano_banana_2_1` é guardado, mas o teste não o afirma.
- `temReferencia` booleano não passa por `valorDaChave` (só lê string entre aspas). O fallback é `json.contains("\"temReferencia\":true")`. `"temReferencia": true` (com espaço) ou `false` explícito com outro `true` no JSON podem ser lidos errado. O caso do brief não tem espaço e passou.
- Limite de 4000 caracteres e o default de `mcp`/`quantidade` estão no código e não têm teste neste arquivo.
- `RespostaJsonDaPonteDeGeracaoMcp` compila com o módulo e não tem asserção. `erro` escapa aspas, barra e quebras via `TextoJsonDoChat.escapar`. `okMcps` devolve o item Higgsfield. `okAndamento` e `okLimpar` são JSON fixos.
- Nenhum arquivo de produção anterior foi modificado. Sem commit.

## Concerns

1. Boolean `temReferencia` só é reconhecido no formato compacto `"temReferencia":true`, ou quando o valor é a string `"true"`.
2. Os quatro helpers de `RespostaJsonDaPonteDeGeracaoMcp` não têm teste.
3. A API pública é a do brief, mas a forma é companion object, não um `object` separado com o mesmo nome da data class.
