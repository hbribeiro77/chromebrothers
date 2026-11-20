# Seletor limpo + proporções na caixa da moldura — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Proporções da moldura na caixa do botão L×A; menu do recorte só com Retângulo/Laço/Nova camada.

**Architecture:** Regra `BarraContextual` passa a mapear RECORTE → SELECAO (não PROPORCAO). Layout da caixa de moldura ganha chips. Painel remove proporções/recortar/destacar/imagem do menu contextual.

**Tech Stack:** Kotlin, Android View, testes no módulo regras, Gradle.

**Spec:** `docs/superpowers/specs/2026-11-04-seletor-de-recorte-limpo-e-proporcoes-na-caixa-da-moldura-design.md`

## Global Constraints

- pt-BR; nomes longos e descritivos
- Sem commit a menos que o usuário peça
- Sem AlertDialog; caixa nossa
- Windows PowerShell; assemble + adb install ao final
- TDD nas regras

---

### Task 1: Regra da linha contextual

**Files:**
- Modify: `BarraContextualDoEditorDeImagemEDestinoDoToqueNaGaveta.kt`
- Test: existente ou `TesteDaBarraContextualDoEditorDeImagem.kt`

**Produces:** `LinhaSecundariaDoEditorDeImagem.SELECAO`; RECORTE → SELECAO

- [ ] Testes + implementação

---

### Task 2: Caixa da moldura com chips

**Files:**
- Modify: `layout_caixa_de_dialogo_do_tamanho_livre_da_moldura_do_editor_de_imagem.xml`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt` (`pedirTamanhoDaMoldura`)

**Produces:** chips de proporção na caixa; wire aplicar/fechar

- [ ] Layout + wire

---

### Task 3: Limpar menu do seletor

**Files:**
- Modify: layout gaveta / painel (`atualizarLinhaSecundaria`, ocultar botões)
- Optionally gone on Recortar/Destacar/Imagem/linha proporção no fluxo

**Produces:** menu só Retângulo/Laço/Nova camada (+ dedo)

- [ ] Limpeza + assemble + adb install
