# Sugestões da barra: ranking Chrome-like + favicon

**Status:** aprovado (2026-11-08 — “isso”)

## Ranking
- Matches do histórico (título/URL) vêm **antes** de “Buscar …”.
- Ordenação: prefixo do host > contém no host > contém no título/URL; empate por ordem recente.
- “Buscar digitado” (ou ENDERECO se parecer URL) fica **sempre por último**, não some.

## Favicon
- Ao carregar página http(s), guardar bitmap do ícone por host em cache local.
- Item de sugestão: ImageView com favicon; se ausente, letra colorida atual.
