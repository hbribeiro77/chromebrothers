# Aguardar qualquer + ir_para — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Ramificação A/B no DSL de roteiros (`aguardar_qualquer`, `marcar`, `ir_para`) com executor, gravador e testes.

**Architecture:** Novos sealed steps em `RoteiroDeAutomacaoDaPaginaDoNavegador`; script JS de polling multi-opção; executor resolve salto 1-based/`destino` + condição `se`; gravador com chips A|B / Marcar / Ir para.

**Tech Stack:** Kotlin, unit tests em `regras-de-video-do-navegador`, Gecko `evaluateJavascript`.

## Task 1: Parse/serialize + testes

Files: `RoteiroDeAutomacaoDaPaginaDoNavegador.kt`, `TesteDoRoteiroDeAutomacaoDaPaginaDoNavegador.kt`, `Montagem…`, `Variaveis…`

## Task 2: Script + executor

Files: `ScriptsDosPassos…`, `ExecutorDosRoteiros…`

## Task 3: Gravador + playbooks/strings

Files: `PainelDoGravador…`, `Playbooks…`, `FerramentasDeLeitura…`, strings

## Task 4: assembleDebug + adb install
