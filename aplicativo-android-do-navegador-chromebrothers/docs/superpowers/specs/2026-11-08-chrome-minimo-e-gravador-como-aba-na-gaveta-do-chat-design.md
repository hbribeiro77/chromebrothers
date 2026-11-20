# Chrome mínimo e Gravador como aba na gaveta do chat

**Status:** aprovado (conversa 2026-11-08)

## Objetivo
Descongestionar o cabeçalho da gaveta: só **config** e **fechar** ficam acima das abas. O **Gravador** vira aba (como os componentes). Ações de conteúdo ficam **abaixo** da faixa, no contexto da aba ativa.

## Hierarquia

1. **Acima das abas:** título (Chat / Configuração) + engrenagem + fechar.
2. **Faixa de abas** (visível se Gravador aberto **ou** há componentes ancorados): `Chat` | `Gravador` (se aberto) | componentes…
3. **Barra de contexto (só aba Chat):** medidor + histórico, nova conversa, rodar roteiro, gravar roteiro (abre o gravador).
4. **Conteúdo:** corpo do chat | host do gravador (opções/chips do gravador) | host dos componentes.

## Comportamento do Gravador
- Abrir em modo gaveta: cria/ativa aba **Gravador**; mostra host com a faixa do gravador.
- Trocar para Chat ou componente: esconde o host do gravador **sem fechar** a sessão (`modoAberto` permanece); a aba Gravador continua na faixa.
- Fechar gravador (X interno, toggle do botão gravar, etc.): remove a aba Gravador; se era a ativa, volta ao Chat.
- Ancorar componente com gravador aberto: permitido (abas convivem).
- Modo faixa (overlay inferior): inalterado; não cria aba.

## Fora de escopo
- Persistência da aba Gravador entre mortes do processo.
- Longo na aba Gravador → Fechar (pode vir depois).

## Critérios de aceite
- [ ] Acima das abas só título + config + fechar.
- [ ] Histórico, nova, rodar e gravar na barra sob as abas (contexto Chat).
- [ ] Abrir gravador (gaveta) → aba Gravador; opções do gravador no painel abaixo.
- [ ] Alternar Chat ↔ Gravador ↔ componente sem perder estado do gravador até fechar.
- [ ] Fechar gravador remove a aba e restaura o painel ativo adequado.
