# Coleção só WebView + HTML até 10 MB

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Decisões

- Coleção flutuante = **somente WebView** (sem Mídia|Página, sem miniaturas).
- Remover tools: `montar_colecao`, `organizar_colecao`, `tirar_da_colecao`, `abrir_modo_da_colecao`.
- Limite HTML: **10 * 1024 * 1024** caracteres (publicação + biblioteca).
- Biblioteca de páginas: **sem** teto de 40 itens.
- Título continua ≤ 120.
