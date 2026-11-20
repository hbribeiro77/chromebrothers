# Modo Colar da seleção copiada — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Copiar abre modo Colar com prévia transformável; Colar funde na camada imagem ativa; cancelar descarta.

**Architecture:** Regra pura decide destino válido / cancelar vs confirmar; editor arma prévia efêmera (sem histórico até Colar), filtra lista, chip Colar na moldura; fundir reutiliza `fundirPecaTransformadaNaOrigem`.

**Tech Stack:** Kotlin, módulo `regras-de-video-do-navegador`, editor Android View.

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- TDD nas regras; assembleDebug + adb install ao final
- Sem commits não pedidos

---

### Task 1: Regras do modo Colar

- [ ] Testes + `ModoColarDaSelecaoCopiadaNaComposicao` (destino válido, cancelar fora/ferramenta, confirmar fundir)
- [ ] Estender `AcaoDaSelecaoAtivaDoLaco.COLAR` + efeito + testes

### Task 2: Editor — armar / colar / cancelar

- [ ] Copiar laço e retângulo → armar prévia (sem Destaque no histórico)
- [ ] Chip Colar na moldura; toque confirma fundir
- [ ] Fora / troca ferramenta → cancelar
- [ ] Lista oculta prévia; `definirCamadaAtiva` só muda destino
- [ ] Sem `Transformacao` empilhada na prévia

### Task 3: Verificar

- [ ] Testes regras + assembleDebug + adb install
