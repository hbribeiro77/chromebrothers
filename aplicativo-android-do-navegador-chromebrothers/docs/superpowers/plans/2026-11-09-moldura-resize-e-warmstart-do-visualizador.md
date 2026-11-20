# Moldura, resize e warm-start do Visualizador — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Moldura dourada na thumb da modal, resize sem engasgar a WebView, e revelar warm-start sem tela preta.

**Architecture:** Harden no stack atual (WebView + inject + carcaça). Destaque via JS na grade; outline nativo no resize; lifecycle `INVISIBLE` + `onResume`/`resumeTimers` no warm-start.

**Tech Stack:** Kotlin, Android WebView, layout XML da carcaça flutuante.

## Global Constraints

- pt-BR; identidade visual existente (`#f5c518`, caixas próprias).
- Sem galeria nativa neste plano.
- Sem `AlertDialog` bagaceiro.

---

### Task 1: Moldura na grade

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (`mostrarMenuDeContextoDaMidiaDoComponente`)

- [ ] Expor `destacarMidiaNaGrade` / `limparDestaqueNaGrade` (evaluateJavascript + CSS `.carta.cb-destaque-modal`)
- [ ] Gestor propaga
- [ ] Activity destaca ao abrir Dialog e limpa no `setOnDismissListener`

### Task 2: Resize com outline

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt` (`prepararTamanho`)

- [ ] Criar View outline (borda dourada) no parent do painel
- [ ] MOVE só move/redimensiona o outline
- [ ] UP aplica `layoutParams` no painel + persiste + esconde outline

### Task 3: Warm-start sem preto

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`

- [ ] Warm-start: `INVISIBLE` em vez de `GONE` no painel
- [ ] Helper `retomarWebViewDaColecao()` com onResume/resumeTimers/invalidate
- [ ] Chamar ao revelar (`mostrar`/`crescer`)

### Task 4: Build + install

- [ ] `assembleDebug` + `adb install -r`
