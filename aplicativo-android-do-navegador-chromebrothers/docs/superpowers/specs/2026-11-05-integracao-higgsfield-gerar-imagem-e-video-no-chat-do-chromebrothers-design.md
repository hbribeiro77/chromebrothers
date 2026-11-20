# Integração Higgsfield: gerar imagem e vídeo no chat do ChromeBrothers

Data: 2026-11-05  
Status: aprovado (2026-11-05)

## Problema

O chat do ChromeBrothers já automatiza o navegador (ler página, clicar, abrir URL, etc.), mas não gera mídia via conta Higgsfield. O fluxo oficial do Higgsfield para agentes (MCP em `https://mcp.higgsfield.ai/mcp`, ou CLI + skills no OpenClaw) não existe no app Android: não há Node/CLI no tablet, nem cliente MCP, nem tools de geração no catálogo OpenRouter.

## Objetivo

No chat do ChromeBrothers, com a conta Higgsfield ligada no aparelho:

1. Gerar **imagem** a partir de prompt (modelo opcional, ex. Nano Banana 2.1).
2. Gerar **vídeo** a partir de prompt (modelo opcional, ex. Wan 3).
3. Pedir **Confirmar** antes de gastar crédito; botão **Liberar** para não perguntar de novo (mesmo espírito das outras ações confirmadas / sites liberados).
4. No sucesso: **salvar na biblioteca** (imagens ou vídeos) **e** refletir no chat (abrir/mostrar o item).

## Abordagem

**Cliente MCP remoto no app**, atrás de duas tools nossas no OpenRouter. O modelo não fala MCP cru; só chama `gerar_imagem_higgsfield` / `gerar_video_higgsfield`. Por baixo o app autentica em `https://mcp.higgsfield.ai/mcp` (OAuth + refresh, sem API key de desenvolvedor) e usa as tools do server necessárias para criar o job e obter o arquivo.

Não usar: CLI npm, `npx skills`, automação frágil só da UI do site, nem a API de desenvolvedor Higgsfield (produto/billing separado) no MVP.

## Comportamento — conta

1. Na **engrenagem do chat**: entrada **Ligar Higgsfield** → fluxo OAuth (Custom Tab / device flow conforme o metadata do server) → tokens (access + refresh) no aparelho.
2. **Desligar** na engrenagem apaga a sessão.
3. Sem sessão: as tools de geração respondem pedindo para ligar na engrenagem; **não** abrem Confirmar e **não** iniciam login sozinhas.

## Comportamento — geração

1. O usuário pede no chat (ex.: “gera uma imagem de … com Nano Banana 2.1” / “vídeo com Wan 3”).
2. O modelo chama a tool correspondente com:
   - `prompt` (obrigatório)
   - `modelo` (opcional; se omitido, default sensato do conector / server)
3. Se Higgsfield **não** estiver liberado → chip **Confirmar / Liberar / Cancelar**. A confirmação mostra prompt + modelo (quando houver).
4. **Confirmar** → dispara a geração uma vez.
5. **Liberar** → marca “Higgsfield liberado” e segue a geração; próximas gerações Higgsfield não perguntam até revogar na engrenagem.
6. **Cancelar** → não gasta crédito.
7. Durante o job: feedback “Gerando…”; poll até concluir ou timeout; não inventar arquivo.
8. Sucesso → baixar mídia → gravar na **biblioteca** (imagem ou vídeo) → resposta da tool com resumo + id/caminho para o chat abrir/mostrar.
9. Erros estáveis ao usuário/modelo: sem créditos, assinatura, modelo inválido, timeout, falha de rede/auth.

## Liberação

- Flag própria **“Higgsfield liberado”** (não confundir com liberação por host de site).
- Revogável na engrenagem, no mesmo espírito da lista de sites liberados.
- Só afeta as tools de geração Higgsfield do MVP.

## Peças técnicas

| Peça | Papel |
|------|--------|
| Sessão Higgsfield | Persistir tokens; ligar/desligar; status |
| Cliente MCP | `https://mcp.higgsfield.ai/mcp`; chamar tools de gerar imagem/vídeo (+ poll/status se o server exigir) |
| Regras de tools | Nomes, JSON schema, defaults de `modelo`, mensagens de erro |
| Ações confirmadas | Confirmar / Liberar / Cancelar iguais ao padrão atual |
| Liberação Higgsfield | Flag + segueSemPerguntar |
| Pós-geração | Download → biblioteca de imagens/vídeos → payload da tool |

## Fora de escopo (MVP)

- Soul, upscale, outpaint, áudio, Motion Control, Clipper, Marketing Studio, etc.
- Tool de listar catálogo de modelos (modelo = string livre no arg; rejeição vem do server).
- Sjinn, Topview e outros provedores.
- Skills npm / CLI no tablet.
- API key do produto “Higgsfield API” de desenvolvedor.

## Ordem de entrega

1. Regras testáveis: args, confirmação/liberação, erros sem rede.
2. Sessão OAuth + engrenagem Ligar/Desligar + flag liberado.
3. Cliente MCP mínimo (imagem) → biblioteca + chat.
4. Mesmo caminho para vídeo + poll/timeout.
5. Smoke manual no aparelho (1 imagem + 1 vídeo).

## Testes

- Unitário (regras): parsing `prompt`/`modelo`; Confirmar vs Liberar vs Cancelar; flag liberado; mensagens sem sessão / crédito / modelo inválido / timeout; formato da resposta de sucesso com id/caminho de biblioteca.
- Manual: ligar conta; gerar imagem com default e com modelo nomeado; gerar vídeo; ver item na biblioteca e no chat; Liberar e gerar de novo sem chip; Desligar e ver falha clara.
