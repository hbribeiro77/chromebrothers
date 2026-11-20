# Design: multi-MCP (checkboxes) no gerador de vídeo

Data: 2026-11-06  
Status: aprovado

## Objetivo

No componente `geracao-mcp-video`, trocar o select de MCP por checkboxes: o usuário marca um ou mais serviços e gera em paralelo em todos. A quantidade da UI é **por MCP**. O select de modelo mostra só o que é **comum** aos MCPs marcados (interseção por família canônica, ex. Wan 3).

## Decisões

| Tema | Escolha |
|------|---------|
| Escopo | Só vídeo (não imagem) |
| UI MCP | Checkboxes (≥1) |
| Quantidade | N por MCP |
| Modelo | Interseção; 1 MCP → catálogo completo |
| Falha parcial | Entrega sucessos + avisa falhas |
| Pedido | `mcps: [...]`; compat com `mcp` singular |

## Peças

- `EquivalenciaDeModelosDeVideoEntreMcps` — família canônica + id por MCP
- Pedido/`okMcps`/HTML checkboxes
- Ponte `listarModelosGeracaoVideoComuns` (ou args multi)
- Serviço: paralelo MCP × qty

## Fora

Imagem; modelo por MCP separado; OAuth.
