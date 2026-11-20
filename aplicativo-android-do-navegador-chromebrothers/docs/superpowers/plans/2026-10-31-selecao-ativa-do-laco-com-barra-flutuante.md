# Seleção ativa do laço com barra flutuante — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Laço fechado vira seleção ativa com máscara móvel/girável e barra flutuante Copiar / Mover / Recortar perto da seleção.

**Architecture:** Regras puras em `regras-de-video-do-navegador` para ações e posicionamento da barra; editor guarda estado da máscara (pontos + transform), desenha alças/barra e aplica efeitos reutilizando extrair/destacar/promover folha; painel mostra modal de Recortar no padrão `Dialog` do editor.

**Tech Stack:** Kotlin, Android View/Canvas, testes Kotlin no módulo regras, Gradle.

**Spec:** `docs/superpowers/specs/2026-10-31-selecao-ativa-do-laco-com-barra-flutuante-design.md`

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- Sem commit a menos que o usuário peça
- TDD nas regras; após mudar código com teste, rodar o teste
- Só laço nesta entrega (retângulo fora)
- Máscara sem preview de conteúdo antes da ação
- Windows PowerShell; assemble + adb install ao final

---

### Task 1: Regras das ações e da barra

**Files:**
- Create/Modify: `regras-de-video-do-navegador/src/main/kotlin/.../SelecaoAtivaDoLacoDaComposicao.kt` (ou em arquivo de barra/recorte existente se couber melhor)
- Test: `regras-de-video-do-navegador/src/test/kotlin/.../TesteDaSelecaoAtivaDoLacoDaComposicao.kt`

**Produces:**
- `AcaoDaSelecaoAtivaDoLaco` enum: COPIAR, MOVER, RECORTAR
- `EfeitoDaAcaoDaSelecaoAtivaDoLaco`: COPIAR_SEM_BURACO, DESTACAR_COM_BURACO, REDUZIR_MOLDURA_A_SELECAO
- `efeito(acao)` mapping
- `RecortarDaSelecaoAtiva.exigeConfirmacao(): Boolean = true`
- `PosicaoDaBarraFlutuanteDaSelecaoAtiva.decidir(bboxTopo, bboxBaixo, alturaVisivel, alturaBarra)` → ACIMA | ABAIXO

- [ ] Step 1: Testes falhando das regras acima
- [ ] Step 2: Implementar regras
- [ ] Step 3: Rodar testes e passar

---

### Task 2: Estado Seleção ativa + alças no editor

**Files:**
- Modify: `EditorDeRecorteETracoDaImagemDaBiblioteca.kt`
- Reuse: gestos de peça (mover/girar), desenho de alças/cabo

**Produces:**
- Ao `lacoFechado = true`, entrar em seleção ativa (máscara com transform: offset + ângulo)
- Toque: mover/girar máscara; toque vazio cancela seleção
- Desenhar contorno + alças + cabo
- API: `selecaoAtivaDoLaco(): Boolean`, `cancelarSelecaoAtivaDoLaco()`, callbacks se útil

- [ ] Step 1: Entrar/sair do modo ao fechar/limpar laço
- [ ] Step 2: Gestos de mover/girar só a máscara (transformar pontos ou matrix)
- [ ] Step 3: Desenho das alças
- [ ] Step 4: assembleDebug (compila)

---

### Task 3: Barra flutuante + Copiar / Mover

**Files:**
- Modify: editor (hit-test chips, desenho texto/botões)
- Modify: strings
- Reuse: `destacarLaco` / lógica de máscara; variante sem buraco para Copiar

**Produces:**
- Chips Copiar / Mover / Recortar perto da bbox
- Copiar: nova camada sem buraco, mesma posição; limpa seleção; ativa nova
- Mover: destacar com buraco; limpa seleção; ativa nova
- Hit-test dos chips tem prioridade sobre arraste

- [ ] Step 1: Desenhar barra + hit-test
- [ ] Step 2: Copiar sem buraco
- [ ] Step 3: Mover = destacar atual
- [ ] Step 4: Ocultar/ignorar botões de canto enquanto seleção ativa (painel)
- [ ] Step 5: assemble + teste manual mental checklist

---

### Task 4: Recortar moldura + modal

**Files:**
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify/Create: layout/strings do modal se precisar
- Modify: editor — API `aplicarRecorteDaSelecaoAtivaNaMoldura()` (máscara → conteúdo; moldura = bbox; remap)

**Produces:**
- Chip Recortar → callback ao painel → modal padrão → confirmar chama editor
- Cancelar modal mantém seleção ativa
- Moldura inteira vira a seleção

- [ ] Step 1: API no editor
- [ ] Step 2: Modal no painel
- [ ] Step 3: Wire chip → modal → aplicar
- [ ] Step 4: Testes regras + assembleDebug + adb install
