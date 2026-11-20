# Componente da coleção: geração de imagem via MCP

Data: 2026-11-05  
Status: aprovado (design)

## Objetivo

Oferecer um **componente HTML minimalista** na coleção flutuante para gerar imagens direto pelo MCP escolhido (v1: só Higgsfield), sem passar pelo chat/LLM: prompt, imagem de referência opcional, modelo da lista ao vivo, número de gerações e envio.

## Decisões

| Tema | Escolha |
|------|---------|
| Onde mora | Coleção flutuante (modo página / biblioteca de componentes) |
| Resultado | Preview no componente **e** gravação na biblioteca de imagens |
| Referência | Galeria do aparelho **ou** biblioteca do ChromeBrothers (opcional) |
| Confirmação de crédito | Nenhuma — Gerar dispara na hora |
| Abordagem | HTML + ponte `chromebrothers` + cliente MCP nativo |

## Arquitetura

```
[Componente HTML na WebView da coleção]
        │  chromebrothers.*
        ▼
[PonteJavascriptDaPaginaDaColecao]
        │
        ▼
[Serviço/facade de geração MCP]
   ├─ SessaoDaContaHiggsfieldNoAparelho
   ├─ ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers
   └─ GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers
        │
        ▼
   JSON → preview file:// / id na biblioteca
```

- MCP e créditos ficam no nativo (sessão OAuth já existente).
- Novos MCPs no futuro: mesmo contrato da ponte; só entram em `listarMcpsGeracao`.
- Sem referência → text-to-image; com referência → `media_upload` + PUT (`If-None-Match: *`) + `media_confirm` + `params.medias` (fluxo já validado no tablet).

## Contrato da ponte (v1)

Respostas em JSON string. Erros: `{ "ok": false, "erro": "…" }`.

| Método | Entrada | Saída (sucesso) |
|--------|---------|-----------------|
| `listarMcpsGeracao()` | — | `{ ok, itens:[{ id, nome }] }` — v1 só `higgsfield` |
| `listarModelosGeracao(mcpId)` | id do MCP | `{ ok, itens:[{ id, nome, … }] }` — catálogo ao vivo (Higgsfield `models_explore` type image) |
| `escolherImagemGeracao(origem)` | `"galeria"` \| `"biblioteca"` | `{ ok, origem, id?, uri?, mime?, nome? }` (bytes ficam no nativo até o gerar; HTML só precisa de preview via uri/`file://` se disponível) |
| `limparImagemGeracao()` | — | `{ ok: true }` |
| `gerarImagensMcp(pedidoJson)` | ver abaixo | `{ ok, itens:[{ idBiblioteca, fileUrl?, nome? }], erro? }` |

### `pedidoJson`

```json
{
  "mcp": "higgsfield",
  "prompt": "…",
  "modelo": "nano_banana_2_1",
  "quantidade": 1,
  "temReferencia": true
}
```

- `prompt`: obrigatório, mesmo limite do chat (4000).
- `modelo`: id do catálogo; se vazio, default Higgsfield imagem (`gpt_image_2_6` ou o default já usado no cliente).
- `quantidade`: inteiro **1–4** (alinhado ao `count` do `generate_image`).
- `temReferencia`: usa a imagem escolhida na sessão da ponte; se false/ausente, ignora ref.
- Sem diálogo de confirmação (diferente do chat).

### Comportamento de `quantidade`

- Estender `ClienteMcpDoHiggsfield…` para enviar `params.count` (1–4) em `generate_image`.
- Se a resposta trouxer vários resultados/URLs, gravar todos; se trouxer só um job apesar do `count`, repetir a geração até atingir a quantidade pedida (fallback).
- Itens parciais em falha no meio: devolver os que gravaram + `ok: false` e mensagem.

## UI do componente

Uma coluna, tipografia sóbria, sem cards no hero:

1. Select **MCP** (só Higgsfield no v1)
2. Textarea **Prompt**
3. **Imagem**: preview + Galeria / Biblioteca / Limpar
4. Select **Modelo** (preenche ao abrir e ao trocar MCP; loading/erro inline)
5. **Gerações** 1–4
6. Botão **Gerar** (desabilita durante o job; status “enviando…”, “1/N…”)
7. **Grade de resultados** com preview; toque pode usar `abrirImagemNoVisor` / ponte já existente

Visual: sem tema “AI purple”; contraste alto sobre o fundo da página da coleção.

## Entrega do HTML

- Componente na biblioteca com **id estável** `geracao-mcp-imagem` (nome amigável: “Geração MCP — imagem”).
- Seed idempotente na abertura da coleção (ou boot do app): se o id `geracao-mcp-imagem` **não** existir na biblioteca, criar com o HTML empacotado; se já existir, **não** sobrescrever (usuário pode ter editado).
- Tags: `mcp`, `higgsfield`, `imagem`.

## Erros

| Situação | Comportamento |
|----------|----------------|
| Sem sessão Higgsfield | `{ ok:false, erro: "Ligue o Higgsfield na engrenagem do chat." }` + texto no UI |
| Lista de modelos falhou | erro inline no select; Gerar bloqueado até haver modelo ou default explícito |
| Upload/confirm/geração falhou | mensagem no form; habilita Gerar de novo; parciais na grade se houver |
| Timeout / rede | mensagem amigável; não trava a WebView |

## Fora de escopo (v1)

- Vídeo / áudio MCP
- Outros provedores além de Higgsfield (só o gancho na lista)
- Confirmação de crédito / “Higgsfield liberado” do chat
- Edição avançada de parâmetros por modelo (resolution, seed, etc.)
- Drag da imagem gerada para o editor (pode reutilizar ponte existente depois, não obrigatório no v1)

## Testes

- Regras/unidade: parse do `pedidoJson` (limites, quantidade 1–4, mcp desconhecido).
- Ponte: respostas JSON estáveis (`ok`/`erro`).
- Manual no tablet: seed do componente → listar modelos → gerar sem ref → gerar com ref (galeria e biblioteca) → itens na grade e na biblioteca.
- Regressão: `If-None-Match: *` no PUT de referência permanece.

## Sucesso

Usuário abre o componente na coleção, preenche prompt (e opcionalmente imagem), escolhe modelo e N, toca Gerar, vê previews e acha as imagens na biblioteca — sem usar o chat.
