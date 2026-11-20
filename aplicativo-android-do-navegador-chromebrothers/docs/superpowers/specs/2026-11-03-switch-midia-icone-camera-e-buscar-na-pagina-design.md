# Switch de mídia no overflow, ícone de câmera e buscar na página

## Escopo

1. **Switch capturar mídia** — some da barra; no menu ⋮ fica sempre, checkable liga/desliga (igual hoje). **Não** alterar o ícone Baixar nem a detecção de mídia.
2. **Captura de tela** — trocar o drawable de crop por ícone de câmera (barra e qualquer uso do mesmo recurso).
3. **Buscar na página** — atalho lupa na barra (modo amplo); no compacto vai ao ⋮. Barra estilo Chrome: campo, anterior/próximo, “N de M”, fechar. Motor: `GeckoSession.SessionFinder`.

## Fora de escopo

- Mover ou mudar `botao_baixar_midia_da_pagina`
- Match case / whole word avançados na v1
- Atalho de teclado físico (pode vir depois)
