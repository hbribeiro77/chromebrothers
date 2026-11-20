# Task 1 — Regras de OCR (prompt, parse, gate)

**Status:** DONE_WITH_CONCERNS  
**Commits:** none  
**Módulo:** `regras-de-video-do-navegador` (JVM puro)

## O que foi entregue

`object ExtracaoDeTextoDeImagemViaIaDoNavegador` com:

- `promptDoUsuario()` — pede só o texto literal, sem comentários
- `limparResposta()` — trim e remoção de cercas markdown opcionais
- `textoParaExibir()` — vazio vira `SEM_TEXTO` (“Não encontrei texto nesta imagem.”)
- `botoesDeAcaoHabilitados()` — falso para vazio e para `SEM_TEXTO`
- `mensagemDeFalha()` — mensagem de modelo sem visão, senão o erro da API ou `FALHA_GENERICA`
- `mostrarAcao()` — verdadeiro somente com chave

## TDD

### RED

1. Teste escrito antes da classe de produção.
2. Primeira execução (`compileTestKotlin`, exit 1) falhou também por `org.junit` inexistente no módulo. O `build.gradle.kts` só declara `testImplementation(kotlin("test"))`. Imports trocados para `kotlin.test` (mesmas asserções). Corpo dos testes igual ao brief.
3. Segunda execução, ainda sem a classe de produção, exit 1:

```
Unresolved reference 'ExtracaoDeTextoDeImagemViaIaDoNavegador'
:regras-de-video-do-navegador:compileTestKotlin FAILED
BUILD FAILED in 1s
```

Falha esperada: classe ausente, não erro de asserção.

### Implementação literal e falha do próprio teste

A implementação do brief foi colada. `limparRespostaRemoveCercasMarkdown` falhou:

```
expected: <ABC-123> but was: <>
6 tests completed, 1 failed
BUILD FAILED
```

Causa: em `"```\nABC-123\n```"`, depois de tirar a cerca de abertura sobra `ABC-123\n````. A heurística de tag de idioma (linha curta, letra/dígito/`-`) trata `ABC-123` como idioma, descarta essa linha e a cerca final, e devolve string vazia.

### GREEN

Ajuste mínimo em `limparResposta`: a primeira linha só é tag de idioma se o restante, depois do trim, não for vazio nem apenas `` ``` ``. Cerca com idioma (` ```text\n... `) continua sendo limpa; o caso do teste devolve `ABC-123`.

Reexecução:

```
BUILD SUCCESSFUL in 3s
```

XML `TEST-com.chromebrothers.navegador.regras.TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.xml` (timestamp 2026-11-03T20:04:01): **tests=6, failures=0, errors=0, skipped=0**.

Casos verdes: `promptPedeSoTextoLiteral`, `limparRespostaRemoveCercasMarkdown`, `textoParaExibirQuandoVazioUsaMensagemPadrao`, `textoParaExibirQuandoHaConteudoDevolveLimpo`, `mostrarAcaoSoComChave`, `botoesDeAcaoSoComTextoUtil`.

## Arquivos

- Criado: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`
- Criado: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ExtracaoDeTextoDeImagemViaIaDoNavegador.kt`

Nenhum outro arquivo alterado. Sem commit.

## Desvios em relação ao brief

1. **Imports do teste:** `org.junit` → `kotlin.test`. Sem isso o teste não compila neste módulo. Asserções e cenários permanecem os do brief.
2. **`limparResposta`:** o código do brief não passa no teste do brief. Guarda extra para não engolir o conteúdo quando ele parece uma tag de idioma. O restante da função é o do brief.

## Auto-revisão

- Prompt contém “texto” e “sem coment” (via “sem comentários”).
- `textoParaExibir("   ")` cai em `SEM_TEXTO`; `textoParaExibir("  olá  ")` devolve `olá`.
- `mostrarAcao` é a chave booleana, sem efeito colateral.
- Botões ficam desligados na mensagem de “sem texto” e ligados para texto útil.
- `mensagemDeFalha` **não tem teste**. `null` ou branco → `FALHA_GENERICA`. Se o erro contém `vision`, `image`, `multimodal`, `does not support` ou `não suport` → `FALHA_MODELO_SEM_VISAO`. Qualquer outro erro não vazio é **devolvido cru**, enquanto a interface do brief fala em “erro genérico amigável”. Mantido como no código do brief, porque não há teste que fixe o contrato.
- `contains("image")` é largo: um erro de rede ou de tamanho que cite “image” cai na mensagem de “troque o modelo”. Também veio do brief; sem teste.
- Caminho de tag de idioma (` ```python\n... `) não está coberto por teste. A guarda nova preserva esse caminho quando há conteúdo depois da primeira linha.
- Cerca vazia (` ```\n``` `) continua virando string em branco e, em `textoParaExibir`, `SEM_TEXTO`.

## Preocupações para as tarefas seguintes

- Cobrir `mensagemDeFalha` (genérico vs. modelo sem visão vs. erro cru) antes da UI depender disso.
- Decidir se erro de API não relacionado a visão deve aparecer cru na tela.
- O filtro `image` pode classificar mal falhas que só mencionam imagem.

## Correção dos achados importantes (revisão)

**Status:** corrigido. Sem commit.

### O que mudou

- `limparResposta`: a tag de idioma é só o texto na mesma linha da cerca de abertura, entre ` ``` ` e o primeiro `\n`, sem `trim` que coma essa quebra. Tag vazia mantém a linha seguinte. `"```\nABC-123\nRua das Flores\n```"` devolve as duas linhas. `"```text\n```"` devolve branco e `textoParaExibir` cai em `SEM_TEXTO`.
- `mensagemDeFalha`: `null`, branco ou erro que não é de visão devolvem `FALHA_GENERICA` (o erro cru da API não volta). Visão/multimodal (`vision`, `multimodal`, `does not support`, `não suport`, `image_url`) devolvem `FALHA_MODELO_SEM_VISAO`. A substring solta `image` não classifica mais como falha de visão.

### Testes

Comando:

```
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador" -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

RED (antes do ajuste de produção): `10 tests completed, 3 failed` — `limparRespostaMantemTodasAsLinhasQuandoCercaNaoTemIdioma`, `cercaVaziaComIdiomaViraSemTexto`, `mensagemDeFalhaNullBrancoOuSemVisaoUsaGenerica`. `BUILD FAILED`.

GREEN:

```
BUILD SUCCESSFUL in 3s
```

XML `TEST-com.chromebrothers.navegador.regras.TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador.xml` (timestamp 2026-11-03T20:08:44): **tests=10, failures=0, errors=0, skipped=0**.
