# Task 1 — Regras: destino e resultado do envio

**Status:** DONE  
**Commits:** none  
**Módulo:** `regras-de-video-do-navegador` (JVM puro)

## O que foi entregue

- `enum class ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor` — `FUNDIR_NA_IMAGEM`, `PROMOVER_FOLHA_E_FUNDIR`, `RECUSAR_SEM_DESTINO`
- `enum class ResultadoDoGestoDeArrasteDaImagemDoComponente` — `FUNDIR`, `CANCELAR`
- `object EnvioDeImagemDoComponenteParaCamadaDoEditor` com:
  - `decidirDestino(editorAtivo, idCamadaAtiva, destinoEhImagem, destinoEhFolha)`
  - `resultadoDoGestoDeSoltar(soltouSobreOQuadroDoEditor)`

## Arquivos

- Criado: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.kt`
- Criado: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/EnvioDeImagemDoComponenteParaCamadaDoEditor.kt`

Nenhum outro arquivo alterado. Sem commit.

## TDD Evidence

### RED — teste primeiro, produção ausente

Comando:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor"
```

Saída relevante (exit 1):

```
e: ... Unresolved reference 'ResultadoDoEnvioDeImagemDoComponenteParaCamadaDoEditor'.
e: ... Unresolved reference 'EnvioDeImagemDoComponenteParaCamadaDoEditor'.
e: ... Unresolved reference 'ResultadoDoGestoDeArrasteDaImagemDoComponente'.
> Task :regras-de-video-do-navegador:compileTestKotlin FAILED
BUILD FAILED in 19s
```

Falha esperada: símbolos de produção inexistentes (não erro de asserção).

### GREEN — implementação mínima do brief

Mesmo comando após criar `EnvioDeImagemDoComponenteParaCamadaDoEditor.kt` (literal ao brief).

Saída (exit 0):

```
> Task :regras-de-video-do-navegador:test
BUILD SUCCESSFUL in 7s
```

XML `TEST-com.chromebrothers.navegador.regras.TesteDoEnvioDeImagemDoComponenteParaCamadaDoEditor.xml` (timestamp 2026-11-05T04:19:26): **tests=4, failures=0, errors=0, skipped=0**.

Casos verdes: `imagemAtivaAceitaFundir`, `folhaVaziaPromoveEFunde`, `semEditorOuSemCamadaRecusa`, `soltarNoQuadroFundeEForaCancela`.

## Desvios em relação ao brief

Nenhum. Teste e implementação colados do brief; o módulo já usa `kotlin.test` (como no brief).

## Auto-revisão

- Prioridade de `decidirDestino` bate com o brief: sem editor ou `idCamadaAtiva == -1` → `RECUSAR_SEM_DESTINO`; depois imagem; depois folha; senão recusa.
- `resultadoDoGestoDeSoltar` é booleano direto → `FUNDIR` / `CANCELAR`.
- Identificadores, assinaturas e enums batem com o brief.
- Sem UI Android (escopo Task 1 respeitado).
- Caso `destinoEhImagem && destinoEhFolha` ambos true: imagem ganha (ordem do brief). Sem teste dedicado — aceitável, brief não pede.
- Caminho final `RECUSAR` com editor ativo, camada válida e ambos destinos false: coberto logicamente pelo `return` final, mas o assert de `semEditorOuSemCamadaRecusa` usa `idCamadaAtiva = -1` (early return). Cobertura comportamental indireta ok para o escopo do brief.

## Preocupações

Nenhuma bloqueante. Tasks seguintes (UI/gesto) devem consumir esses enums sem reinterpretar a prioridade.
