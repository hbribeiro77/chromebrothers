# Task 2 — Índice + medidor

**Status:** DONE  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

Regras puras em `regras-de-video-do-navegador`. Nada de Android, disco ou UI.

- `data class EntradaDoIndiceDaConversaDoChat(id, titulo, criadaEm, atualizadaEm, bytesAproximados, resumida = false)`
- `IndiceDasConversasDoChatDaOpenRouter`
  - `MAXIMO_DE_CONVERSAS = 40`
  - `TETO_DE_BYTES_TOTAIS = 200L * 1024 * 1024`
  - `TETO_DURO_POR_CONVERSA = 50L * 1024 * 1024`
  - `escrever(entradas)` / `ler(bruto)` — inválido devolve `emptyList()`
  - `criar(entradas, id, titulo, agoraMs, bytes)` — nova no início; `criadaEm` e `atualizadaEm` = `agoraMs`; `resumida` = false
  - `atualizar(entradas, id, titulo: String? = null, atualizadaEm, bytes, resumida: Boolean? = null)` — `titulo` ou `resumida` nulos conservam o valor; `criadaEm` não muda; id ausente devolve a mesma lista
  - `excluir(entradas, id)` — só remove aquele id
  - `bytesTotais(entradas)`
  - `ultrapassouQuantidade` — `size >= 40` (a lista já inclui a conversa nova)
  - `ultrapassouBytesTotais` — soma `>=` 200 MiB
  - `ultrapassouTetoDuroDaConversa(bytes)` — `bytes >` 50 MiB
- `enum class FaixaDoMedidorDoHistoricoDoChat { OK, ATENCAO, CHEIO }`
- `MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(bytesTotais, teto): Int` — 0..100; `teto <= 0` → 100; trunca
- `faixa(percentual)` — `>= 100` CHEIO, `>= 80` ATENCAO, senão OK

Formato:

```json
{"conversas":[{"id":"id1","titulo":"Oi","criadaEm":1,"atualizadaEm":2,"bytesAproximados":3,"resumida":false}]}
```

`id` e `titulo` passam por `TextoJsonDoChat.escapar`. Números saem sem aspas e voltam por `numeroDaChave` → `Long` (não `inteiroDaChave`: instante em ms estoura `Int`). `TextoJsonDoChat` não lê booleano; `resumida` é lido no próprio índice, no mesmo estilo de achar a chave e exigir `:`.

`ultrapassouTetoDuroDaConversa` usa `>` porque a Task 4 bloqueia quando os bytes da conversa são maiores que o teto. Quantidade e bytes totais usam `>=`: o brief diz `≥ MAX` depois de incluir, e o alerta é ao atingir o limite (o percentual 100 cai em CHEIO).

## Arquivos

Criados:

- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/IndiceDasConversasDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/MedidorDeUsoDoHistoricoDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoIndiceDasConversasDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter.kt`

Nenhum arquivo existente foi alterado. Sem commit.

## TDD

Gradle (JAVA_HOME = JDK 21 Adoptium, `gradle-8.11.1`):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "..."
```

### RED — índice

`TesteDoIndiceDasConversasDoChatDaOpenRouter` escrito antes da classe. `compileKotlin` ficou UP-TO-DATE. `compileTestKotlin` falhou (exit 1):

```
Unresolved reference 'EntradaDoIndiceDaConversaDoChat'.
Unresolved reference 'IndiceDasConversasDoChatDaOpenRouter'.
BUILD FAILED
Execution failed for task ':regras-de-video-do-navegador:compileTestKotlin'.
```

Falha esperada: símbolo inexistente. `Cannot infer type` é efeito colateral.

### GREEN — índice

Com `IndiceDasConversasDoChatDaOpenRouter` e sem o medidor ainda: `BUILD SUCCESSFUL in 5s`.

### RED — medidor

`TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter` escrito antes do medidor. `compileKotlin` UP-TO-DATE. Compilação de teste falhou (exit 1):

```
Unresolved reference 'FaixaDoMedidorDoHistoricoDoChat'.
Unresolved reference 'MedidorDeUsoDoHistoricoDoChatDaOpenRouter'.
```

### GREEN — suíte dos dois arquivos

Comando do passo 3. `BUILD SUCCESSFUL in 5s`.

Relatórios em `regras-de-video-do-navegador/build/test-results/test/`:

| Suíte | testes | falhas | erros |
|---|---|---|---|
| `TesteDoIndiceDasConversasDoChatDaOpenRouter` | 9 | 0 | 0 |
| `TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter` | 3 | 0 | 0 |

Os dois testes de índice e o `faixasDoMedidor` estão literais no brief.

Extras do índice, para o que o brief pede e os dois oficiais não travam: nova no início (`criadaEm` = `atualizadaEm` = `agoraMs`, `resumida` false); `atualizar` omite título/`resumida` e id ausente não altera; `excluir` tira só o id; chave `"conversas"` e título/id com aspas e quebra de linha; instante e bytes acima de `Int`; `ler("{}")` e `ler("lixo")` → lista vazia; tetos 40 / 200 MiB / 50 MiB, com `>=` na quantidade e nos bytes totais e `>` no teto duro.

Extras do medidor: 79 OK, 99 ATENCAO, 101 CHEIO; percentual 0, 50, truncamento `1/3 → 33`, 100 no teto e acima, `teto <= 0` → 100, bytes negativos → 0.

## Auto-revisão

- Assinaturas batem com o brief. O `atualizar` do teste oficial nomeia `atualizadaEm`, `bytes` e `resumida` e omite o título.
- Roundtrip de data class cobre `resumida` false e true.
- `criar` não reordena no `atualizar`. A lista da Task 6 ordena por `atualizadaEm` descendente.
- `criar` não funde id repetido: sempre insere outra entrada no início.
- Leitura de campo ausente vira `""` / `0` / `false`, no mesmo espírito da Task 1. Array `conversas` ilegível ou ausente zera a lista inteira.
- `numeroDaChave` devolve `Double`. Inteiros até `2^53` (instante em ms e 200 MiB) são exatos. Acima disso o `Long` pode arredondar.
- `percentual` trunca com divisão inteira. Se `0 < bytes < teto` e `bytes > Long.MAX_VALUE / 100`, o produto estoura. Com o teto de 200 MiB isso não ocorre, porque bytes nesse intervalo já caem no retorno 100.
- `faixa` não limita a entrada: 101 continua CHEIO. Quem chama com o `percentual()` recebe 0..100.

## Concerns

Nenhum bloqueio. `criar` com id repetido e `Long` via `Double` acima de `2^53` ficam para o repositório, se um dia importar. O teto duro é estrito (`>`): 50 MiB exatos ainda passam.

## Correção — finding importante (JSON inválido no `ler`)

**Status:** corrigido. Sem commit.

`ler` já devolvia `emptyList()` quando `objetosDaChave` era null (`{}`, `lixo`). Um array com objeto sem `id` ainda fabricava entrada com campos padrão (`id=""`, `titulo=""`, números `0`, `resumida=false`).

Agora cada objeto exige `id` string não em branco e `titulo` presente como string. Se qualquer objeto falha nisso, o documento inteiro vira `emptyList()`. Números e `resumida` ausentes continuam `0` / `false` quando `id` e `titulo` são válidos.

Não mexi nos findings menores (Double→Long acima de `2^53`, overflow do percentual).

### RED

`objetoSemIdOuComIdEmBrancoInvalidaODocumentoInteiro` falhou (exit 1), 10 testes, 1 falha:

```
expected: <[]> but was: <[EntradaDoIndiceDaConversaDoChat(id=, titulo=, criadaEm=0, atualizadaEm=0, bytesAproximados=0, resumida=false)]>
TesteDoIndiceDasConversasDoChatDaOpenRouter > objetoSemIdOuComIdEmBrancoInvalidaODocumentoInteiro() FAILED
    org.opentest4j.AssertionFailedError at TesteDoIndiceDasConversasDoChatDaOpenRouter.kt:110
BUILD FAILED in 3s
```

### GREEN

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoIndiceDasConversasDoChatDaOpenRouter"
```

```
BUILD SUCCESSFUL in 3s
4 actionable tasks: 4 executed
```

`TesteDoIndiceDasConversasDoChatDaOpenRouter`: 10 testes, 0 falhas, 0 erros. Inclui `ler("{\"conversas\":[{}]}")` → `emptyList()`, id em branco ou só espaços, `titulo` ausente, um objeto mau no meio do array (documento inteiro vazio) e `ler("{\"conversas\":[{\"id\":\"x\",\"titulo\":\"T\",\"criadaEm\":1,\"atualizadaEm\":2,\"bytesAproximados\":3}]}")` → entrada `x` / `T` / 1 / 2 / 3.

## Correção — finding importante (JSON truncado no `ler`)

**Status:** corrigido. Sem commit.

`objetosDaChave` devolve a lista ao encontrar `]`, então `{"conversas":[{"id":"x","titulo":"T"}]` (falta o `}` final) ainda virava uma entrada. Antes de aceitar o parse, `ler` exige que `bruto` inteiro tenha `{}` e `[]` balanceados fora de strings JSON. O scanner `delimitadoresBalanceados` ignora texto entre aspas e o caractere escapado depois de `\`. Desbalanceado, aspas sem fechar ou delimitador que não casa com o aberto → `emptyList()`.

### RED

`jsonTruncadoSemFecharOObjetoRaizDevolveListaVazia` falhou (exit 1), 11 testes, 1 falha:

```
expected: <[]> but was: <[EntradaDoIndiceDaConversaDoChat(id=x, titulo=T, criadaEm=0, atualizadaEm=0, bytesAproximados=0, resumida=false)]>
TesteDoIndiceDasConversasDoChatDaOpenRouter > jsonTruncadoSemFecharOObjetoRaizDevolveListaVazia() FAILED
    org.opentest4j.AssertionFailedError at TesteDoIndiceDasConversasDoChatDaOpenRouter.kt:110
BUILD FAILED in 3s
```

### GREEN

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoIndiceDasConversasDoChatDaOpenRouter"
```

```
BUILD SUCCESSFUL in 3s
4 actionable tasks: 4 executed
```

`TesteDoIndiceDasConversasDoChatDaOpenRouter`: 11 testes, 0 falhas, 0 erros. Inclui `ler("{\"conversas\":[{\"id\":\"x\",\"titulo\":\"T\"}]")` → `emptyList()`, o roundtrip válido e `ler("{\"conversas\":[{}]}")` → `emptyList()`.
