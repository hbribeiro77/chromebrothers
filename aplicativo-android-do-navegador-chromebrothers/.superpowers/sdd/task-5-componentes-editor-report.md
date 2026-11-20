# Task 5 — Fechar janelas ao sair do editor

**Status:** DONE  
**Commits:** none  
**Módulo:** `app` (UI Android)

## O que foi entregue

- `PainelDaGavetaDaBibliotecaDeVideos`: `var aoEncerrarEdicaoDeImagem: (() -> Unit)? = null`.
- Invocação **só** em `abandonarEdicaoNaMemoria()` — funil único de saída da edição, evitando double-call:
  - `cancelarEdicao()` → `abandonarEdicaoNaMemoria()`
  - `esconder()` com `editandoImagem` → `abandonarEdicaoNaMemoria()`
  - `tratarVolta()` em modo edição → `abandonarEdicaoNaMemoria()`
  - `concluirEdicao()` / `fecharVisor()` / `estacionarEdicaoAtual()` também passam por aí
- `AtividadePrincipalDoNavegadorChromebrothers`: `painelDaBibliotecaDeVideos.aoEncerrarEdicaoDeImagem = { painelDaColecao.encerrarTodas() }`
- Confirmado: `painelDaColecao` é `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador` (tem `encerrarTodas()`).

## Arquivos

- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt`

Sem commit.

## Compile check

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Saída: **BUILD SUCCESSFUL in 5s** (exit 0). Warnings pré-existentes de `statusBarColor`/`navigationBarColor` deprecated — não relacionados a esta task.

## Desvios em relação ao brief

- Nenhum desvio funcional: callback único no funil `abandonarEdicaoNaMemoria` em vez de chamar nos três métodos listados (equivalente, sem risco de invoke duplo).

## Preocupações

- `estacionarEdicaoAtual` (trocar de rascunho / abrir outra imagem) também dispara `encerrarTodas` — desejável para não deixar órfãs do contexto anterior.
- `AtividadeDoPlayerNativo…` não liga o callback (só Activity principal, como no brief).
