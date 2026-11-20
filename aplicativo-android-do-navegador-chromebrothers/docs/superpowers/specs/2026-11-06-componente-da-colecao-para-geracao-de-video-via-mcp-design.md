# Design: componente coleção — geração de vídeo via MCP/API

Data: 2026-11-06  
Status: aprovado (componente **separado** do de imagem)

## Objetivo

Seed `geracao-mcp-video` para gerar vídeo (Sjinn Tool API; default **Wan 3** / `wan3-video-api`), sem alterar o componente de imagem nem a área de prompt no espírito do existente.

## Decisões

| Tema | Escolha |
|------|---------|
| Onde | Componente HTML separado (não misturar com imagem) |
| MCPs | Higgsfield (MCP OAuth) + Sjinn (API Token) |
| Auth Sjinn | Mesmo API Token já guardado |
| Default Sjinn | `wan3` → `wan3-video-api` |
| Default Higgsfield | Wan 3.0 → `wan3_0` via `generate_video` |
| MVP refs | Até 4 imagens no Sjinn; Higgsfield só prompt no MVP |
| Quantidade | 1 por vez (vídeo é lento/caro) |
| Resultado | MP4 em `filesDir` + intercept WebView também em `filesDir` (não só cache) |

## Params UI

- prompt, MCP (sjinn), modelo, duração 2–30 (default 5), resolução 480P/720P/1080P (default 480P), proporção (16:9 default + adaptive/9:16/1:1/4:3/3:4)

## Nativo

Reusa sessão/upload/poll Sjinn; `create_tool_task` + `query_tool_task_status`; `guardarVideo` com caminho absoluto.
