# Biblioteca de componentes HTML + inspeção pelo chat

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

Renomear “páginas” → **componentes**; enriquecer com `descricao` e `tags`; chat lê o código e captura screenshot do componente.

## Modelo

`ItemDaBibliotecaDeComponentesHtmlDaColecao`: `id`, `nome` (≤120), `descricao` (≤500), `tags` (lista curta), `html` (≤10MB), `atualizadoEm`.

Migração: JSON antigo com `titulo` → `nome`; descricao/tags vazios.

## Tools

- `listar_componentes_da_biblioteca` (leitura)
- `guardar_componente_da_colecao` (ação*; args opcionais descricao, tags)
- `abrir_componente_da_biblioteca` (ação*; id ou nome)
- `apagar_componente_da_biblioteca` (ação*; id)
- `ler_codigo_do_componente` (leitura; id → HTML)
- `capturar_componente` (leitura ou ação leve; id → screenshot + file:// como capturar_aba)

\*confirmação salvo site liberado.

## UI

Gaveta seção Componentes; botão Guardar na coleção.

## Fora

Manifesto/quando; skills automáticas.
