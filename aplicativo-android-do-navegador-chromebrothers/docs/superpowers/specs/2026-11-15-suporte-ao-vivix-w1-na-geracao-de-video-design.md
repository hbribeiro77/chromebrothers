# Design: suporte ao Vivix W1 na geração de vídeo

Data: 2026-11-15
Status: implementado (coleção + chat)

## Objetivo

Adicionar o **Vivix W1** (API REST `https://api.vivix.ai`, token Bearer) como 5º provedor do gerador de vídeo, no mesmo esquema do Sjinn (token guardado no aparelho), com vídeo como referência e escolha de resolução.

Base: integração do `higgsfieldclone` (`lib/cliente-da-api-de-video-vivix-w1.ts`), atualizada para a API atual.

## O que a API aceita (doc + teste com a chave em 2026-11-15)

| Item | Valor |
|------|-------|
| Criar | `POST /v1/videos/jobs` → `{model, pe_mode:"standard", jobs:[{…}]}` (exatamente 1 job) → `data.results[0].job_id` |
| Consultar | `GET /v1/videos/jobs/{job_id}` → `status` queued/processing/succeeded/failed, `video_url`, `error.code` |
| Modelos na conta | `vivix-w1`, `vivix-w1-fast`, `vivix-w1-ultrafast` (`vivix-w1-auto` é documentado, mas **não está liberado** na conta) |
| Duração | 5–15 s |
| Proporção | 16:9, 9:16, 1:1, 4:3, 3:4 |
| **Resolução** | `jobs[0].resolution` = **`720p` ou `1088p`**. Não está na doc; descoberto pela resposta 20004 `"must be 720p or 1088p"`. **Não há 480p na API** (o playground mostra 480/720). |
| Referências | upload em `/v1/files/images` (≤ 10 MB, até 8) e `/v1/files/videos` (mp4/mov/webm, ≤ 20 MB, ≤ 15 s cada, ≤ 20 s somados, até 4) → usar `data.url` em `ref_images[]` / `ref_videos[]` |
| Campos desconhecidos | recusados (20004) — só mandamos campos conhecidos |
| first/last frame | não combina com referências → fora de escopo |

## Decisões

| Tema | Escolha |
|------|---------|
| Id do provedor | `vivix` (nome na UI: "Vivix W1") |
| Auth | Token em EncryptedSharedPrefs (`SessaoDaContaVivixNoAparelho`), engrenagem → Vivix W1: Guardar / Apagar / Revogar liberação |
| Modelo padrão | `vivix-w1` (mais barato, ≈1,7 créd/s) |
| Resolução da UI | 480p/720p → `720p`; 1080p → `1088p` |
| Duração da UI | presa em 5–15 s |
| `adaptive` | vira 16:9 |
| Catálogo | estático, com custo por segundo no rótulo |
| Vídeo de referência | aceito em todos os W1; ao misturar com outros MCPs vale o limite mais restrito |
| Reenvio | o `POST` do job nunca é repetido sozinho; erro de rede no poll vira "ainda gerando" (card mostra Verificar) |
| Chat | ferramenta `gerar_video_vivix` (prompt, modelo, duração, resolução, proporção), Confirmar / Liberar Vivix, pergunta com estimativa de créditos. Sem referências no chat. |

## Peças

- Regras (JVM, testadas): `PedidoDeVideoDoVivixW1` (ajuste, corpo do job, limites, créditos, mensagens), `CatalogoDeModelosDeVideoDoVivixNaColecao`, `GeracaoDeVideoPeloVivixNoChat`, `LiberacaoDoVivixParaOChat`; `MCP_VIVIX` no pedido, origem `vivix` na biblioteca, limites em `ReferenciaDeVideoNaGeracaoMcp`.
- App: `SessaoDaContaVivixNoAparelho`, `ClienteApiDoVivixParaGeracaoDeVideoNoChromebrothers`, ramo `vivix` no serviço da coleção (gerar + verificar), seção na engrenagem, ramo no chat, chip de origem na gaveta.

## Testes

- `TesteDoVivixW1NaGeracaoDeVideo` (11 casos) + suíte completa do módulo (208 classes, sem falhas).
- `assembleDebug` + `adb install` ok.
- Pendente (manual, gasta crédito): colar o token no tablet, gerar 1× na coleção (com e sem vídeo de referência) e 1× pelo chat.
