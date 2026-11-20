# Task 1 — Persistência das mensagens + título

**Status:** DONE  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

Regras puras em `regras-de-video-do-navegador`:

- `PersistenciaDasMensagensDoChatDaOpenRouter.escrever(mensagens: List<MensagemDeChat>): String`
- `PersistenciaDasMensagensDoChatDaOpenRouter.ler(bruto: String): List<MensagemDeChat>` — JSON inválido devolve `emptyList()`
- `TituloDaConversaDoChatDaOpenRouter.gerar(mensagens: List<MensagemDeChat>, maximo: Int = 60): String`

Formato gravado:

```json
{"mensagens":[{"papel":"user","conteudo":"...","idDaChamada":"","partes":[],"chamadas":[]}]}
```

Cada parte: `tipo`, `texto`, `mime`, `nome`, `dados`.  
Cada chamada: `id`, `nome`, `argumentos`.  
Escrita com `TextoJsonDoChat.escapar`. Leitura com `objetosDaChave` (array na chave `mensagens`, e de novo em `partes` / `chamadas`) e `valorDaChave`.

Título: primeira mensagem com `papel == "user"`, `conteudo` com trim, espaços em branco colapsados num único espaço, corte em `maximo` caracteres. Sem user, ou user que fica vazio depois disso → `"Conversa"`.

## Arquivos

Criados:

- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/PersistenciaDasMensagensDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/TituloDaConversaDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaPersistenciaDasMensagensDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoTituloDaConversaDoChatDaOpenRouter.kt`

Nenhum arquivo existente foi alterado. Sem commit.

## TDD

Gradle (JAVA_HOME = JDK 21 Adoptium, `gradle-8.11.1`):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "..."
```

### RED — roundtrip de texto

Teste `roundtripDeMensagemUserComTexto` escrito antes da classe. Compilação de teste falhou (exit 1):

```
e: .../TesteDaPersistenciaDasMensagensDoChatDaOpenRouter.kt:10:20 Unresolved reference 'PersistenciaDasMensagensDoChatDaOpenRouter'.
e: .../TesteDaPersistenciaDasMensagensDoChatDaOpenRouter.kt:11:20 Unresolved reference 'PersistenciaDasMensagensDoChatDaOpenRouter'.
BUILD FAILED
Execution failed for task ':regras-de-video-do-navegador:compileTestKotlin'.
```

Falha esperada: símbolo inexistente, não erro de asserção.

### GREEN — roundtrip de texto

Depois de `escrever`/`ler`, o mesmo filtro:

```
BUILD SUCCESSFUL in 5s
```

### Testes extras de persistência

Acrescentados no mesmo arquivo, na ordem do plano (depois do formato completo):

- `roundtripComPartesImagemChamadasEMensagemTool` — parte `imagem` com `mime` + `dados`, chamada de ferramenta com argumentos JSON (`{"seletor":"h1"}`) e mensagem `tool` com `idDaChamada`.
- `jsonInvalidoDevolveListaVazia` — `ler("{}")` e `ler("lixo")` → lista vazia.

Primeira execução da classe inteira: `BUILD SUCCESSFUL`. O XML confirma 3 testes, 0 falhas. Esses dois extras não tiveram um RED próprio: o passo 3 do plano pede o formato com partes e chamadas antes dos testes extras, então eles já nasceram verdes. O RED comprovado da persistência é a compilação sem a classe.

### RED — título

`TesteDoTituloDaConversaDoChatDaOpenRouter` escrito antes de `TituloDaConversaDoChatDaOpenRouter`. Compilação falhou (exit 1):

```
Unresolved reference 'TituloDaConversaDoChatDaOpenRouter'.
```

(O aviso `Cannot infer type` em `emptyList()` é efeito colateral do símbolo ausente.)

### GREEN — suíte dos dois arquivos

Comando do passo 6. `BUILD SUCCESSFUL in 5s`.

Relatórios em `regras-de-video-do-navegador/build/test-results/test/`:

| Suíte | testes | falhas | erros |
|---|---|---|---|
| `TesteDaPersistenciaDasMensagensDoChatDaOpenRouter` | 3 | 0 | 0 |
| `TesteDoTituloDaConversaDoChatDaOpenRouter` | 3 | 0 | 0 |

Casos: `roundtripDeMensagemUserComTexto`, `roundtripComPartesImagemChamadasEMensagemTool`, `jsonInvalidoDevolveListaVazia`, `tituloPegaPrimeiraMensagemUserCortada`, `semUserUsaConversa`, `tituloColapsaEspacosEUsaConversaQuandoUserFicaVazio`.

Os dois testes de título do plano estão literais. O terceiro trava o que o plano pede e os dois oficiais não cobrem: colapsar espaços (incluindo quebra de linha) e user só com espaço em branco → `"Conversa"`.

## Auto-revisão

- Assinaturas batem com o plano. JSON raiz usa a chave `mensagens`. Campos de parte e de chamada batem com `ParteDeConteudoDoChat` e `ChamadaDeFerramenta`.
- Roundtrip de igualdade de data class cobre defaults vazios (`partes`, `chamadas`, `idDaChamada`, `texto`, `nome`).
- Aspas no texto do user e aspas dentro de `argumentos` passam por `escapar` / `valorDaChave`. O teste da chamada usa `{"seletor":"h1"}`.
- `ler` inválido não lança: ausência da chave `mensagens` (ou array que o parser não fecha) vira lista vazia, como `objetosDaChave` já devolve `null`.
- Título ignora `system` anterior, corta em 60 e, no caso `"a".repeat(100)`, o resultado tem tamanho 60 e começa com `"aaa"`.
- Colapso manual (`isWhitespace`), sem regex, no mesmo estilo do restante do módulo. Trim antes; espaço pendente no fim não é gravado, então não sobra espaço à direita.
- A primeira mensagem `user` é a usada, mesmo se o texto dela ficar vazio. Uma `user` posterior com texto não substitui essa. É o que a frase do plano descreve.
- `maximo` negativo não está especificado. `String.take` lança `IllegalArgumentException` nesse caso. O padrão é 60 e os testes usam 60.
- O corte é por `Char` (UTF-16). Um emoji no limite pode partir um par substituto. O teste oficial é ASCII; não alterei isso.
- `TextoJsonDoChat.objetosDaChave` / `valorDaChave` acham a primeira ocorrência da chave no texto. A escrita deste módulo escapa aspas, então o roundtrip que nós produzimos não injeta uma chave falsa. JSON escrito à mão com a sequência da chave dentro de um valor continua limitado pelo parser compartilhado, que o plano mandou usar.
- Nada de Android, disco ou UI nesta tarefa. Só as duas regras.

## Concerns

Nenhum bloqueio. A limitação do parser compartilhado e o corte por `Char` ficam para quem for persistir ou exibir o título, se um dia importar.
