# Warm-start (Visualizador + MCP) e arraste do visualizador — Design

**Status:** aprovado (conversa 2026-11-07)

## Warm-start

- Pré-carregar **invisível** após o 1º paint: componentes com propriedade `warmStart` (Visualizador + MCP imagem/vídeo).
- **Abrir** = revelar/focar (sem reload). **Fechar** = esconder sem bolha; pausar mídia.
- **Propriedade no item** da biblioteca (`warmStart: true` no JSON / tags/`warm-start`).
- **Toggle global** nos ⋯ do navegador: “Pré-carregar painéis” (default ligado). Desliga todos.
- Não pré-carrega o resto da biblioteca.

## Arraste (fase seguinte)

- Itens do Visualizador: além do arraste → editor, mesmo fluxo da gaveta (referência MCP + colar em inputs de imagem).

## Fora de escopo agora

- Warm-start de todos os HTML; bolha ao fechar; commit automático.
