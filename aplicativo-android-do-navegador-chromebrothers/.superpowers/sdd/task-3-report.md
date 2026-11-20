# Task 3 — Compactação + prompt de resumo

**Status:** DONE  
**Commits:** nenhum (pedido explícito: não commitar)

## O que foi entregue

Regras puras em `regras-de-video-do-navegador`. Nada de Android, disco, rede ou UI.

- `CompactacaoDaConversaDoChatDaOpenRouter`
  - `ULTIMAS_TROCAS_USER_ASSISTANT = 2`
  - `promptParaResumir(mensagens)` — `system` + `user` pedindo resumo fiel em português do Brasil. Entram só mensagens `user`/`assistant`. Anexo `imagem` vira `[imagem]`, `arquivo` vira `[arquivo]`; `dados` (base64), mime e nome não entram. Parte de outro tipo contribui com `texto`.
  - `aplicar(resumo, mensagensOriginais)` — `[MensagemDeChat("assistant", resumo.trim())]` + últimas trocas. Cada troca guardada é só `papel` + `conteudo` (`partes`, `chamadas` e `idDaChamada` ficam de fora).

Regra da cauda: filtra `user`/`assistant` (ignora `tool` e `system`), percorre de trás e conta no máximo 2 trocas. Assistant imediatamente depois de user forma um par (2 mensagens, 1 troca). Mensagem que não fecha par (user solto no fim, ou assistant sem user antes) conta como 1 troca. A ordem cronológica é preservada, então a cauda tem no máximo 4 mensagens.

## Arquivos

Criados:

- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/CompactacaoDaConversaDoChatDaOpenRouter.kt`
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaCompactacaoDaConversaDoChatDaOpenRouter.kt`

Nenhum arquivo de produção existente foi alterado. Sem commit.

O `task-3-report.md` anterior descrevia a consulta CSS por seletor (outro ciclo). Este arquivo passou a ser o relatório desta Task 3.

## TDD

Gradle (JAVA_HOME = JDK 21 Adoptium, `gradle-8.11.1`):

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaCompactacaoDaConversaDoChatDaOpenRouter"
```

### RED

Testes escritos antes da classe. `compileKotlin` UP-TO-DATE. `compileTestKotlin` falhou (exit 1):

```
Unresolved reference 'CompactacaoDaConversaDoChatDaOpenRouter'.
BUILD FAILED
Execution failed for task ':regras-de-video-do-navegador:compileTestKotlin'.
```

Falha esperada: símbolo inexistente. `Cannot infer type` é efeito colateral.

### GREEN

```
BUILD SUCCESSFUL in 5s
4 actionable tasks: 4 executed
```

Relatório em `regras-de-video-do-navegador/build/test-results/test/TEST-com.chromebrothers.navegador.regras.TesteDaCompactacaoDaConversaDoChatDaOpenRouter.xml`:

| Suíte | testes | falhas | erros |
|---|---|---|---|
| `TesteDaCompactacaoDaConversaDoChatDaOpenRouter` | 4 | 0 | 0 |

`compactaMantemUltimasDuasTrocasSemPartes` está literal no brief. O prompt cobre `system` e `user`, as substrings `resumir` e `português`, `[imagem]`/`[arquivo]`, o texto `u1`/`a1`/`planilha`/`trecho-lido`, e a ausência de `xx` e do texto de `tool`/`system`.

Extras: constante `= 2`; `aplicar` faz trim do resumo; tool/system no meio não entram na cauda; partes e chamadas das trocas mantidas somem; user final sem resposta conta como troca incompleta (`u2`, `a2`, `u3`); uma troca só é preservada; histórico vazio devolve só o resumo.

## Auto-revisão

- Assinaturas batem com o brief.
- O resumo vai sempre na frente, papel `assistant`, mesmo se o trim ficar vazio.
- `promptParaResumir` não reenvia `partes`. O base64 não aparece no texto.
- A Task 6 deve chamar `promptParaResumir` sem ferramentas e, se a resposta vier sem erro, `aplicar` e gravar.

## Concerns

A cauda copia só papel e texto. Se uma das últimas trocas for um assistant só com `chamadas` e conteúdo vazio, a chamada se perde. O brief pede “copiar texto só” e “sem partes”; `chamadas` não é `partes`, mas também não é texto.

O nome do arquivo não vai para o prompt, só `[arquivo]`. Quem for resumir não vê o filename.

Um user no fim, sem assistant, gasta uma das duas trocas. A cauda fica com o par anterior mais esse user (3 mensagens), não com quatro.
