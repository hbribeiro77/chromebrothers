# Leitura paginada, consultar elementos e rolar página no chat

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

Em páginas grandes, o agente deixa de depender só dos primeiros 6000 caracteres / 40 mídias / viewport do screenshot. Passa a **paginar texto**, **extrair nós por CSS** e **rolar a aba** (sem confirmação), para montar coleção/página HTML com dados reais.

## Abordagem

Três ferramentas novas de **leitura** (sem confirmação do usuário), na aba ativa. `ler_texto_visivel` permanece como atalho do início da página.

## Ferramentas

### `ler_texto_da_pagina`

- Args: `offset` (int ≥ 0, default 0), `limite` (int, default 6000, máx 6000).
- Fonte: `document.body.innerText` normalizado (espaços colapsados), igual ao espírito de `ler_texto_visivel`.
- Resposta textual para o modelo, em português, incluindo:
  - cabeçalho com título/endereço da aba (quando disponível no wiring, ou só o bloco se o helper for puro)
  - corpo do trecho
  - metadados explícitos: `total`, `offset`, `proximo_offset`, `tem_mais`
- Se `offset` ≥ total: corpo vazio, `tem_mais=false`, `total` informado.
- `ler_texto_visivel` continua existindo (equivalente prático a offset 0 / limite 6000 sem metadados de paginação obrigatórios).

### `consultar_elementos`

- Args: `seletor` (CSS, obrigatório, máx 300 caracteres), `limite` (default 40, máx 40), `offset` (default 0).
- Execução: `document.querySelectorAll(seletor)` — **sem** `eval`, Function, nem JS arbitrário.
- Por elemento (após aplicar offset/limite): tag; texto curto (máx ~200); `href`/`src` se existirem e forem `http`/`https`; attrs `alt`, `title`, `aria-label` quando úteis; filhos diretos relevantes `img` / `video` / `a` com URL http(s).
- Resposta: lista formatada + indicação de quantos matches totais e se há mais além do limite/offset.
- Erros: seletor vazio; seletor inválido (exceção do engine); nenhum match — mensagens curtas em pt-BR.

### `rolar_pagina`

- Sem confirmação (exploração, não muda URL).
- Args — **um** modo por chamada:
  - `pixels`: scroll relativo (delta Y)
  - `y`: scroll absoluto
  - `paginas`: múltiplos da altura do viewport (pode ser fracionário positivo/negativo via número)
  - `seletor`: `scrollIntoView` no primeiro match (mesmo limite de tamanho de seletor)
- Se nenhum modo válido ou vários conflitantes de forma ambígua: recusar com mensagem clara (preferência de interpretação: se mais de um presente, prioridade fixa `seletor` > `y` > `paginas` > `pixels`, documentada no schema/descrição).
- Resposta: `y` atual, `altura_pagina`, `altura_viewport`, `no_fim` (bool).

## Arquitetura

| Camada | Responsabilidade |
|--------|------------------|
| `regras-de-video-do-navegador` | Constantes, parse de args (`TextoJsonDoChat` + inteiros), scripts JS, formatação da resposta, validação de limites |
| `FerramentasDeLeituraDoChatDaOpenRouter` | Nomes, schema JSON com `properties`, `permitidas`, rótulos, trecho de `instrucao` |
| `PainelDoChatDaOpenRouterNoNavegador` | Dispatch das três tools no `when` de leitura (não passam por `AcoesConfirmadas`) |
| `AtividadePrincipalDoNavegadorChromebrothers` | Callbacks: `evaluateJavascript` na WebView da aba ativa; para scroll, ler posição após `scrollTo` / `scrollIntoView` |

Helpers sugeridos (nomes longos e descritivos):

- `LeituraPaginadaDoTextoDaPaginaParaOChat`
- `ConsultaDeElementosDaPaginaPorSeletorCssParaOChat`
- `RolagemDaPaginaDaAbaParaOChat`

## Instrução do sistema

Acrescentar orientação: se a página for grande ou o pedido for estruturado (listas, cards, midia+texto), usar `consultar_elementos`, `ler_texto_da_pagina` com offset e `rolar_pagina` quando o conteúdo for lazy-load; não afirmar que “não cabe / não vê tudo” sem esgotar paginação/rolagem/consulta.

## Segurança

- Apenas CSS selector + APIs DOM listadas.
- URLs só `http`/`https` nas extrações.
- Limites enforced no app.
- Scroll não dispara navegação nem clique.

## Testes (JVM)

- Paginação: fatias, `tem_mais`, offset além do fim.
- Elementos: parse/format, seletor vazio/inválido simulado via resultado do script, teto de limite.
- Scroll: escolha de modo pela prioridade, formatação, args ausentes.
- Schema: as três tools em `json()`; nomes em `permitidas`; rótulos.

## Fora de escopo

- Offset/filtro em `listar_midia_da_pagina` / `ler_enderecos_da_pagina`
- Captura full-page multimodal no chat
- Esperar rede / MutationObserver / JS livre
- Confirmação ou liberação para essas tools (sempre livres)

## Critério de sucesso

Pedido do tipo “liste os itens desta página longa com imagem/vídeo e publique na webview” deixa o agente capaz de: rolar se preciso → consultar cards → paginar texto se faltar contexto → montar/publicar, sem depender do usuário dizer “prossiga” só porque o primeiro bloco cortou.
