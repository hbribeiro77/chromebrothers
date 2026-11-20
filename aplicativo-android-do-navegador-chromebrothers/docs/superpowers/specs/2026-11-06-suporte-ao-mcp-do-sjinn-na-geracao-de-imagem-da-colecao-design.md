# Design: suporte ao Sjinn na geração de imagem da coleção

Data: 2026-11-06  
Status: aprovado com ajuste técnico

## Objetivo

Oferecer **Sjinn** no seletor de MCP do componente `geracao-mcp-imagem` com a mesma UX (prompt intacto, refs, proporção, resolução, quantidade).

## Bloqueio MCP OAuth (descoberta)

O AS Sjinn (`client_id_metadata_document_supported`) **só aceita CIMD em domínios allowlisted** (ex.: `claude.ai`, `openai.com`, `chatgpt.com`). Domínios genéricos / tunnel / GitHub raw retornam:

`invalid_client: client_id is not an allowed metadata document URL.`

Sem allowlist da ChromeBrothers (ou DCR), OAuth MCP nativo não completa login.

## Abordagem MVP adotada

Usar a **Tool API** documentada (`POST /api/un-api/create_tool_task` + `query_tool_task_status`) com **API key** (Bearer), no mesmo slot `mcp=sjinn` da coleção.

| UI / pedido | Implementação |
|-------------|-----------------|
| `listarMcpsGeracao` | inclui `{ id: sjinn, nome: Sjinn }` |
| `listarModelosGeracao(sjinn)` | catálogo estático alinhado aos modelos MCP/API de imagem |
| `gerarImagensMcp` | `create_tool_task` + poll `output_urls` |
| Login | colar API Token (painel do chat), espelhando OpenRouter |

Quando Sjinn allowlistar um CIMD nosso, o cliente OAuth MCP pode substituir a auth sem mudar o contrato da ponte.

## Defaults

- Modelo: `gpt-image-2` → tool `gpt-image-2-api`
- Resolução UI `1k|2k|4k` → API `1K|2K|4K`
- Refs locais: `POST /api/un-api/upload_file` (`bucket_name=comfy-online`, `content_type`, `size_bytes`) → PUT `signed_url` → usar `cdn_url` em `image_list` / `image_urls`

## Fora de escopo

Vídeo/áudio/compose; OAuth MCP até allowlist.
