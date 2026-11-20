# Arraste do Visualizador → MCP + colar na página — Implementation Plan

> **For agentic workers:** Executar task a task. Sem commit automático.

**Goal:** Long-press no Visualizador (já existente) também solta referência no gerador MCP e cola em inputs da página (mesmo verde “+” / Ctrl+V da gaveta), sem perder o fundir no editor.

**Architecture:** Estende o fantasma do componente (JS bridge → Activity). Nova regra JVM decide destino: MCP > editor > colar página > cancelar. Reusa `soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo` e o fluxo Ctrl+V da gaveta; no start copia a imagem para a área de transferência.

**Tech Stack:** Kotlin, regras JVM + testes, fantasma nativo existente, `evaluateJavascript` na página.

**Spec:** `docs/superpowers/specs/2026-11-07-warm-start-e-arraste-do-visualizador-e-mcp-design.md`

## Global Constraints

- Não quebrar arraste → editor (moldura/camada).
- Soltar no próprio Visualizador continua cancelando.
- Vídeo no Visualizador já vira quadro `file://` antes do arraste.
- Sem commit automático; pt-BR; Gradle local `gradle-8.11.1`.

## File map

| Arquivo | Papel |
|---------|--------|
| `DestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina.kt` | Decisão + verde |
| `TesteDoDestinoDoArrasteDoComponenteDaColecaoParaMcpEditorOuPagina.kt` | TDD |
| `AtividadePrincipalDoNavegadorChromebrothers.kt` | Clipboard, sonda, drop MCP/colar |

---

### Task 1: Regra de destino (TDD)

- [x] Teste: MCP vence; editor só fora da coleção; colar se editável; Visualizador cancela
- [x] Implementar enum + `decidir` + `corIndicaPodeSoltar`
- [x] `:regras-de-video-do-navegador:test` PASS

### Task 2: Wiring na Activity

- [x] No start do fantasma: copiar bytes da URI para clipboard
- [x] No move: sonda de editável (mesma regra da gaveta) + verde via nova regra (MCP/editor/página)
- [x] No soltar: MCP → nucleo; editor → fundir; página → foco+Ctrl+V; senão cancelar
- [x] Build/install

### Task 3: Verificação manual

- Visualizador → MCP imagem: entra refs
- Visualizador → ChatGPT/Higgsfield: “+” verde e cola
- Visualizador → editor: ainda funde
