# Design: suporte ao MCP Topview na geração de vídeo

Data: 2026-11-06  
Status: aprovado

## Problema

O gerador de vídeo da coleção (`geracao-mcp-video`) e o chat já falam com **Higgsfield** (MCP OAuth) e **Sjinn** (API Token / Tool API). Não há cliente Topview. O MCP oficial Topview (`https://mcp.topview.ai/mcp`, OAuth) expõe tools `topview_*` de text-to-video e image-to-video que o tablet ainda não usa.

## Objetivo

Integrar Topview **no mesmo esquema** do Higgsfield/Sjinn para vídeo:

1. Seletor MCP da coleção inclui **Topview**.
2. Conta ligada por **OAuth MCP** (engrenagem do chat: Ligar / Desligar).
3. Geração na coleção: prompt, modelo, proporção, resolução, duração, quantidade 1–4, referências 0–N.
4. Geração de **vídeo** também pelo **chat** (tool + Confirmar/Liberar), como o Higgsfield.
5. Sucesso → MP4 na biblioteca + preview/`fileUrl` no HTML / abertura no chat.

## Decisões

| Tema | Escolha |
|------|---------|
| Auth | OAuth MCP (PKCE), igual Higgsfield — **não** API Key + UID |
| Endpoint | `https://mcp.topview.ai/mcp` (+ discovery OAuth do server) |
| Abordagem de código | Cliente dedicado espelhando Higgsfield (sem refator genérica MCP neste ciclo) |
| Escopo de superfície | Coleção **e** chat |
| Refs | 0 = text-to-video; 1+ = image-to-video (upload → fileId) |
| Catálogo | **Estático** (Topview não tem list-models API); display names da doc oficial |
| Default de modelo | `Seedance 2.5` |
| Loopback OAuth | Porta **8788** (Higgsfield usa 8787) |
| Imagem Topview / marketing MCP | Fora de escopo |
| API REST Key+UID | Fora de escopo |

## Arquitetura

```
[HTML geracao-mcp-video] --bridge--> PainelDaColecao
                                         |
                                         v
                              ServicoDeGeracao…Video
                         when (higgsfield|sjinn|topview)
                                         |
                                         v
                    ClienteMcpDoTopview… + SessaoDaContaTopview…
                                         |
                                         v
                         mcp.topview.ai (tools topview_*)
                                         |
                                         v
                    Gravacao…guardarVideoComMetadados → biblioteca

[Chat OpenRouter] --tool gerar_video_topview--> mesmo Cliente + liberação própria
```

## Auth e sessão

1. Engrenagem do chat: **Ligar Topview** → OAuth Authorization Code + PKCE.
2. Preferir endpoints descobertos via metadata OAuth do MCP; fallback alinhado ao padrão Higgsfield (`register` / `authorize` / `token` sob o host Topview).
3. Redirect loopback: `http://127.0.0.1:8788/callback`.
4. Persistir `access_token`, `refresh_token`, `client_id` em EncryptedSharedPrefs (`SessaoDaContaTopviewNoAparelho`).
5. **Desligar** apaga a sessão.
6. Sem sessão: coleção e chat falham com mensagem pedindo Ligar Topview — **não** abrem OAuth sozinhos.

## Catálogo de modelos

Objeto `CatalogoDeModelosDeVideoDoTopviewNaColecao` (módulo `regras-de-video-do-navegador`):

- Itens com `id` estável (slug interno, ex. `seedance-2-5`) e `nome` = **display name** enviado à API/MCP (ex. `Seedance 2.5`) — a API é case-sensitive no display name.
- Lista inicial (subset útil da doc; expansível depois):
  - Seedance 2.5 (default), Seedance 2.0, Seedance 2.0 Fast
  - Kling V3, Kling 4.0
  - Wan 2.7, Wan 3.0, Wan 3.0 Prime
  - Gemini Omni Flash
- `listarModelosDeVideoJson` no serviço: se `mcp=topview` e há sessão → JSON do catálogo estático (mesmo formato da ponte que Sjinn/Higgsfield).
- Mapeamento de resolução UI (`480p`/`720p`/`1080p`) → inteiro `480`/`720`/`1080` no payload Topview.
- Proporção e duração da UI seguem o pedido atual; se o modelo rejeitar combinação, a mensagem de erro do server sobe ao usuário.

## Fluxo — coleção

1. HTML lista MCPs via `listarMcpsGeracaoVideo` → inclui `{id:"topview", nome:"Topview"}`.
2. Pedido JSON existente ganha `topview` em `MCPS_CONHECIDOS` / `modeloPadraoPara`.
3. `gerarVideoEAgregar` / paralelo:
   - **0 refs:** `topview_text_to_video_submit` com `waitForCompletion=false` (ou equivalente) → `taskId` → `topview_text_to_video_query` até URL final.
   - **1+ refs:** upload (`topview_upload_base64` preferencial no app; fallback se o server expuser outra tool de upload) → `fileId`(s) → `topview_image_to_video_submit` (`firstFrameFileId` e/ou `referenceImageFileIds` conforme quantidade/modelo) → query.
4. Poll com intervalo ~4s e timeout generoso (vídeo lento; alinhar ~5–10 min ao Sjinn).
5. Download bytes → `guardarVideoComMetadados` (mesmo caminho de pasta/biblioteca já usado).
6. HTML: botão Tela cheia / lift-drag de frame inalterados (contrato `fileUrl` igual).

Nomes exatos das tools MCP devem ser confirmados no `tools/list` após OAuth na implementação; a spec assume o conjunto documentado pela comunidade/skill (`topview_text_to_video_submit|query`, `topview_image_to_video_submit|query`, upload). Se o server oficial usar aliases, o cliente adapta sem mudar o contrato da UI.

## Fluxo — chat

1. Tool própria `gerar_video_topview` com `prompt` (obrigatório) + `modelo` (opcional; default Seedance 2.5).
2. Referências no chat: reutilizar o mesmo caminho de anexo já usado pelo Higgsfield no chat, se existir de forma trivial; senão, chat MVP = só prompt e refs ficam na coleção nesta entrega.
3. Detecção `ehPedidoTopview` + flag **Topview liberado** (espelho de `LiberacaoDoHiggsfieldParaOChat`) — Confirmar / Liberar / Cancelar.
4. Sem sessão → mensagem SEM_SESSAO pedindo engrenagem.
5. Sucesso → biblioteca + payload para o chat abrir o vídeo.
6. Revogar liberação na engrenagem (botão espelho do Higgsfield).

## Peças técnicas

| Peça | Papel |
|------|--------|
| `SessaoDaContaTopviewNoAparelho` | Tokens + flag liberado |
| `ClienteMcpDoTopviewParaGeracaoNoChromebrothers` | OAuth + tools MCP vídeo/upload/poll/download |
| `CatalogoDeModelosDeVideoDoTopviewNaColecao` | Lista estática + display name |
| `ExtracaoDaUrlDoResultadoDaTarefaTopview` (+ upload se preciso) | Parse JVM testável |
| `LiberacaoDoTopviewParaOChat` / regras de pedido | Confirmar/Liberar |
| `ServicoDeGeracaoDeImagemViaMcp…` | `okMcps`, listar modelos, `when` Topview |
| `PedidoDeGeracaoDeVideoViaMcpNaColecao` | `topview` conhecido |
| `PainelDoChatDaOpenRouter…` + layout engrenagem | Ligar/Desligar/Revogar + tool |
| `HtmlDoComponenteDeGeracaoDeVideo…` | Garante Topview no fluxo (rótulos/defaults se necessário) |

## Fora de escopo

- Tools de marketing / Amazon / TikTok / Shopee / YouTube KOL do Topview MCP.
- Geração ou edição de **imagem** Topview.
- Avatars, TTS, boards, Campaign Workspace UI.
- API Key + `Topview-Uid` REST.
- Refatorar Higgsfield para cliente MCP genérico compartilhado.
- Unlimited Topview (docs: não funciona com MCP automatizado).

## Ordem de entrega sugerida

1. Regras testáveis: pedido/`okMcps`, catálogo, parsers de submit/query/upload, liberação chat.
2. Sessão OAuth + UI engrenagem Ligar/Desligar/Revogar.
3. Cliente MCP: text-to-video → biblioteca (coleção).
4. Upload + image-to-video; quantidade paralela.
5. Tool + confirmação no chat.
6. Smoke no tablet + `assembleDebug` + `adb install`.

## Testes

**Unitário (JVM):**

- `MCPS_CONHECIDOS` / `okMcps` incluem `topview`.
- Catálogo: default Seedance 2.5; display names corretos.
- Extracao URL/status a partir de fixtures JSON de submit/query.
- Liberação: Confirmar vs Liberar vs sem sessão.
- HTML (se alterado): contém Topview / ponte inalterada.

**Manual:**

- Ligar Topview (OAuth) no tablet.
- Coleção: text-to-video 1×; image-to-video com 1 ref; qty>1 se crédito permitir.
- Abrir no player nativo; item na biblioteca.
- Chat: pedido de vídeo Topview → Confirmar → grava; Liberar → não pergunta de novo.
- Desligar → falha clara; Sjinn e Higgsfield ainda geram.

## Critério de pronto

Topview no seletor; OAuth persiste; 0 e 1+ refs geram e gravam MP4; chat gera com liberação; sem regressão Sjinn/Higgsfield; testes do módulo + `assembleDebug` + install ok.
