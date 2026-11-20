# Gravador sempre aba + barrinha split visual

**Status:** aprovado (2026-11-08)

## Gravador sempre na faixa
- Faixa sempre visível: `Chat | Gravador` (+ componentes ancorados).
- Remover ícone de gravar da barra de contexto do Chat.
- Toque em Gravador: se sessão fechada, abre em modo **gaveta**; se aberta, só foca a aba.
- Longo na aba Gravador: menu de modo (gaveta/faixa/padrões).
- Fechar (X do painel): encerra sessão, aba permanece; ativa Chat.

## Barrinha split (só visual)
- Na abertura da gaveta: linha + pílula na junção página↔chat.
- Cor: `#E8ECF2` (~60% linha, ~80% pílula); halo preto discreto opcional.
- Acompanha `geometriaDosBlocos` no mesmo interpolador.
- No fim da animação de split: fade out ~180 ms e remove.
- Sem toque / sem resize. Fechamento: sem barrinha (só abertura).
