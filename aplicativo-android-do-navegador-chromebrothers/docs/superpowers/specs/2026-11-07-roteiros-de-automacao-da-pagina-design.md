# Roteiros de automação da página — Design

**Status:** aprovado (conversa 2026-11-07)

## Objetivo

IA monta um roteiro (JSON) uma vez; o usuário roda com ▶ sem gastar tokens.

## v1

- Passos: `abrir`, `aguardar` (seletor|texto + timeout), `clicar` (seletor|texto), `preencher` (seletor|texto + valor), `esperar_ms`, `rolar` (seletor|pixels), `clicar_se` (seletor + indice + contem + se_nao).
- Persistência em `files/roteiros-de-automacao/*.json`.
- Tools: `guardar_roteiro_de_automacao`, `listar_roteiros_de_automacao`, `rodar_roteiro_de_automacao`.
- Botão ▶ na gaveta do chat; na coleção: `chromebrothers.rodarRoteiro(id)`.

## Fora de escopo

- Accessibility / apps nativos; gravação automática de tool_calls; Playwright externo.
