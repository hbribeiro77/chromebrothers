# Melhorias — Geração MCP imagem (coleção)

## Escopo

1. Fechar o editor de imagem **não** fecha/esconde janelas da coleção.
2. Modelo padrão: `nano_banana_2_1` (Nano Banana 2.1) quando listado; fallback interno o mesmo id.
3. Até **4** imagens de referência (galeria/biblioteca), todas em `params.medias`.
4. Select de proporção: `1:1`, `16:9`, `9:16`, `4:3`, `3:4` (default `1:1`) → `aspect_ratio`.
5. Select de resolução: `1k` / `2k` / `4k` (default `1k`) → `params.resolution`.
6. Layout compacto (grade Modelo|Proporção|Resolução|Gerações); **prompt e MCP intactos**.

## Fora de escopo

- Confirmação de crédito; parâmetros avançados por modelo (seed, thinking_level).
