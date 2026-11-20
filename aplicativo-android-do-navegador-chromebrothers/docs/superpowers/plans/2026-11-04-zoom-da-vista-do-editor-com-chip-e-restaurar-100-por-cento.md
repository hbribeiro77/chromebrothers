# Zoom da vista do editor com chip e restaurar 100% — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Zoom da vista do editor de 25%–800%, chip com porcentagem e toque para restaurar 100% + centro.

**Architecture:** Regra pura clamp/formato/restaurar no módulo `regras-de-video-do-navegador`; editor aplica range no pinça e expõe `restaurarZoomDaVista()`; chip `TextView` no FrameLayout do editor, mostrado/oculto pelo painel ou pelo próprio editor via callback.

**Tech Stack:** Kotlin, Android View, testes no módulo regras, Gradle.

**Spec:** `docs/superpowers/specs/2026-11-04-zoom-da-vista-do-editor-com-chip-e-restaurar-100-por-cento-design.md`

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- Sem commit a menos que o usuário peça
- TDD nas regras; após mudar arquivo com teste, rodar o teste
- Sem AlertDialog; chip no padrão visual do editor
- Windows PowerShell; assemble + adb install ao final

---

### Task 1: Regras do zoom da vista

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ZoomDaVistaDoEditorDeImagem.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoZoomDaVistaDoEditorDeImagem.kt`

**Produces:**
- `MINIMO = 0.25f`, `MAXIMO = 8f`, `ENCAIXE = 1f`
- `limitar(zoom): Float`
- `rotuloPercentual(zoom): String` → `"67%"` etc.
- `percentualInteiro(zoom): Int`

- [ ] Step 1: Testes falhando
- [ ] Step 2: Implementar
- [ ] Step 3: Rodar testes e passar

---

### Task 2: Editor — range + restaurar + callback

**Files:**
- Modify: `EditorDeRecorteETracoDaImagemDaBiblioteca.kt`

**Produces:**
- Pinça usa `ZoomDaVistaDoEditorDeImagem.limitar`
- `restaurarZoomDaVista()`
- Listener/`aoMudarZoomDaVista: ((Float) -> Unit)?` chamado em `definirZoom` e restaurar

- [ ] Step 1: Wire clamp + restaurar + callback
- [ ] Step 2: assembleDebug compila

---

### Task 3: Chip na UI + wire no painel

**Files:**
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` (TextView no FrameLayout do editor)
- Modify: `strings_do_navegador_chromebrothers.xml`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt` (mostrar/ocultar, toque, contentDescription)

**Produces:**
- Chip visível ao zoom ≠ gesto; some após ~1s
- Toque restaura 100%
- Estilo fundo arredondado do editor

- [ ] Step 1: Layout + strings
- [ ] Step 2: Wire painel
- [ ] Step 3: assembleDebug + adb install
