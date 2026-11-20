# Task 2 — Persistência JSON (vídeo + imagem)

**Status:** DONE_WITH_CONCERNS  
**Commits:** nenhum

## O que foi implementado

Leitura e gravação dos metadados da Task 1 nos dois JSON da biblioteca, no módulo `app`. Não há helper `SerializacaoDosMetadados`. O `parse` continua em `OrigemDaMidiaNaBibliotecaDoNavegador`.

Em `carregar` de vídeo (`itens-da-biblioteca.json`) e de imagem (`itens-da-biblioteca-de-imagens.json`):

- `origem` = `OrigemDaMidiaNaBibliotecaDoNavegador.parse(objeto.optString("origem"))`
- `modelo` = `objeto.optString("modelo")`
- `prompt` = `objeto.optString("prompt")`
- `refs` = `referenciasDeTextoNoArrayJson(objeto.optJSONArray("refs"))` — `null` vira lista vazia; texto em branco é ignorado
- `caminhoLocal` = `objeto.optString("caminhoLocal")`
- `exportado` = `objeto.optBoolean("exportado", false)`

Chave ausente cai no default do `opt*` / do `parse` (`desconhecido`, string vazia, lista vazia, `false`). O `try/catch` que já existia continua devolvendo biblioteca vazia se o arquivo inteiro for inválido.

Em `gravar`, os mesmos campos entram no objeto. `refs` é um `JSONArray` de strings (`JSONArray(item.refs)`).

Helper privado `referenciasDeTextoNoArrayJson` foi duplicado nas duas classes para o `carregar` não percorrer o array no meio da montagem do item. Não foi criado arquivo novo.

Fora desta task: UI de exportação, save MCP, chips, modal e qualquer alteração em `regras-de-video-do-navegador`.

## Arquivos

Alterados:

- `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo.kt`
- `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo.kt`

## Verificação

O brief dispensa teste JVM novo porque `JSONArray` é Android. Não havia teste dessas classes `Gravacao*` para reexecutar.

Comando (projeto Android):

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Resultado: `BUILD SUCCESSFUL` in 16s. `:app:compileDebugKotlin` executou (não estava UP-TO-DATE). Aviso pré-existente de SDK XML version 4; não é erro de compilação.

## Autorevisão

- Item antigo sem as chaves novas monta o data class com os defaults da Task 1. `optString` ausente é `""`; `parse("")` é `desconhecido`; `optJSONArray` ausente é `null` e vira `emptyList()`; `optBoolean(..., false)` ausente é `false`.
- A próxima `gravar` reescreve o arquivo já com as chaves novas. Leitor antigo ignora chaves que não lê.
- `refs` em branco (`""` ou só espaço) não entram na lista. Número ou outro valor não-string passa por `optString` e vira texto se não for branco.
- Origem conhecida é normalizada (`Topview` → `topview`) na leitura, igual ao `registrar` da Task 1.

## Preocupações

1. **Não há teste de ida e volta do JSON.** O compile garante que os campos existem e que o código Kotlin fecha. Não prova, em runtime, que um arquivo antigo abre nem que `refs` em branco somem. O brief pediu só `:app:compileDebugKotlin`.
2. **`refs` que não seja array é descartado.** String, objeto ou número na chave `refs` faz `optJSONArray` voltar `null` e a lista fica vazia. O contrato do brief é array de strings.
3. **Origem fora da lista vira `desconhecido` na leitura.** Um valor cru no JSON não sobrevive ao próximo `gravar`. É o `parse` da Task 1, não um campo livre.
4. **O helper de refs está copiado nas duas classes.** Evita o arquivo `SerializacaoDosMetadados` que o brief pediu para não criar. Se o critério de “em branco” mudar, os dois métodos precisam mudar juntos.
