# Design: metadados, filtro e detalhe na gaveta de mídia gerada

Data: 2026-11-07  
Status: aprovado

## Problema

Na gaveta (Vídeos / Imagens → Histórico e Favoritos) o usuário não consegue:

1. Distinguir ou filtrar o que veio de **Higgsfield**, **Sjinn**, **Topview** ou só da **página**.
2. Ver o **prompt completo** e as **imagens de referência** usadas na geração MCP.
3. Saber se o arquivo está **no aparelho** e **onde**; exportar cópia pública quando ainda só existe no storage privado do app.

Hoje o índice guarda pouco: chave, título truncado (≤80 do prompt), URL/`file://`, favorito, miniatura. A origem MCP **não** é persistida (vídeos gerados usam prefixo `higgsfield-` mesmo para Sjinn/Topview). Refs existem só em memória durante a geração.

Além disso, há desejo de **arrastar** do histórico para slots de referência MCP e para inputs web (ex. ChatGPT) — fluxo relacionado, mas engenharia distinta do lift-drag interno da coleção.

## Objetivo

Uma evolução em **duas fases** na mesma visão de produto:

| Fase | Entrega |
|------|---------|
| **1 (implementar agora)** | Metadados no índice; chips de origem; modal ⓘ com prompt/refs/local; baixar/exportar; vale para **vídeos e imagens** geradas |
| **2 (spec agora, plano depois)** | Toque longo no cartão → thumbnail translúcida → soltar em MCP (ref) e em página web (DnD de sistema) |

## Decisões

| Tema | Escolha |
|------|---------|
| Abordagem | Estender índices existentes (vídeo + imagem); não criar “biblioteca rica” paralela |
| Tags livres / categorias editáveis | **Fora** desta spec (MVP = origem como filtro) |
| Abrir detalhe | Ícone **ⓘ** no cartão; toque normal continua abrindo a mídia |
| Modal | Padrão Chromebrothers: `Dialog` + família `layout_caixa_de_dialogo_do_editor_de_imagem` — **sem** `AlertDialog` |
| Escopo de mídia | Vídeos **e** imagens (geradas MCP + itens de página com origem `pagina`) |
| Export vídeo | `Downloads/ChromeBrothers` (aparece na aba **Baixados**) |
| Export imagem | `Pictures/ChromeBrothers` (padrão atual da galeria) |
| Downloads raiz / SAF | **Fora** — manter subpasta ChromeBrothers |
| Itens antigos | `origem = desconhecido`; sem backfill de prompt/refs |
| Fase 2 no mesmo ciclo de código | **Não** — só documentada; plano/implementação após Fase 1 estável |

## Fase 1 — Metadados, filtro, ⓘ, baixar

### Modelo de dados

Estender `ItemDaBibliotecaDeVideos` e `ItemDaBibliotecaDeImagensCapturadas` (+ JSON em disco) com campos **opcionais** (compatível com JSON antigo):

| Campo | Valores / uso |
|-------|----------------|
| `origem` | `higgsfield` \| `sjinn` \| `topview` \| `pagina` \| `desconhecido` |
| `modelo` | id/nome usado na geração (string) |
| `prompt` | texto completo; título do cartão continua truncado/amigável |
| `refs` | lista de identificadores locais (paths ou nomes de arquivo) das cópias das imagens de referência |
| `caminhoLocal` | path absoluto ou `file://` quando há arquivo no app; vazio se só link remoto |
| `exportado` | boolean — já houve export para pasta pública (imagens podem reutilizar/alinhar com `naGaleria`) |

**Save MCP** (`guardarVideoComMetadados` / equivalente de imagem):

- Gravar `origem` real do MCP usado (corrigir chave/prefixo para não rotular tudo como Higgsfield).
- Gravar `prompt`, `modelo`.
- Persistência de refs: copiar bytes das referências usadas para pasta dedicada sob `filesDir` (ex. `refs-de-geracao-da-biblioteca/`) e listar esses paths em `refs`.
- Preencher `caminhoLocal` com o MP4/JPEG interno.

Itens capturados/vistos na página: `origem = pagina`; prompt/modelo/refs vazios salvo o que já existir.

Leitura: se `origem` ausente no JSON → `desconhecido`.

### Filtro na UI

Nas seções **Vídeos** e **Imagens** (subfiltros Histórico e Favoritos):

- Chips horizontais: **Todos | Higgsfield | Sjinn | Topview | Página | Desconhecido**.
- Filtram a lista atual (`recentes` / favoritos) pela `origem`.
- Preferência: só mostrar chip se houver pelo menos 1 item daquela origem (se o custo for baixo); senão mostrar todos os chips sempre.

Sem tags livres neste MVP.

### Cartão

- Toque curto → abre mídia (comportamento atual).
- **ⓘ** → abre modal de detalhe (não abre a mídia).
- Chip/rótulo pequeno de origem no cartão **opcional** se não poluir; origem completa no modal.

Favoritar / remover do histórico permanecem.

### Modal de detalhe

Receita visual canônica:

1. `Dialog(activity)`
2. Layout na família do editor (`layout_caixa_de_dialogo_do_editor_de_imagem` ou layout dedicado **com o mesmo visual**: fundo `#1A1D24`, radius 20dp, window transparente, largura ~420dp)
3. Botões no estilo Chromebrothers (`backgroundTintList = null`, etc.)
4. **Proibido** `AlertDialog.Builder` neste fluxo

Conteúdo:

- Preview/miniatura
- Origem + modelo
- Prompt completo (scroll) + **Copiar prompt**
- Grade das refs (se houver); toque abre a imagem (visor/modal existente se couber)
- Local: “No aparelho (pasta do app)” + path resumido, ou “Só link”
- Ações: **Baixar** (quando aplicável) · **Copiar caminho** · **Fechar**

Abrir a mídia continua sendo o toque no cartão (não obrigatório “Abrir” no modal).

### Baixar / localizar

| Caso | Ação |
|------|------|
| Vídeo gerado com bytes no app | Exportar cópia para `Downloads/ChromeBrothers` via MediaStore; marcar `exportado`; usuário vê em **Baixados** |
| Imagem gerada com bytes no app | Exportar para `Pictures/ChromeBrothers` (reusar fluxo de galeria existente); marcar `exportado` / `naGaleria` |
| Já exportado | UI indica “Já na pasta pública”; permitir reexportar se simples |
| Só link remoto (página) | **Fora do corte mínimo da Fase 1** se o download remoto complicar — botão desabilitado ou oculto com texto claro; gerados MCP são o foco |
| Copiar caminho | Clipboard do `caminhoLocal` / `file://` quando existir |

Não mudar a aba Baixados para “Downloads raiz”; não introduzir SAF nesta fase.

### Arquitetura (Fase 1)

```
[Save MCP vídeo/imagem]
    → Gravacao…(origem, prompt, modelo, refs copiadas, caminhoLocal)
    → JSON biblioteca vídeo / imagem

[Gaveta Histórico/Favoritos]
    → chips filtram por origem
    → ⓘ → Dialog detalhe (padrão Chromebrothers)
         → Copiar prompt / Copiar caminho / Baixar (export público)
```

Classes tocadas (orientação, não lista fechada):

- `BibliotecaDeVideosDoNavegador` / `ItemDaBibliotecaDeVideos`
- `BibliotecaDeImagensCapturadasDoNavegador` / item de imagem
- `GravacaoDaBibliotecaDeVideosNoArmazenamentoDoAplicativo`
- `GravacaoDaBibliotecaDeImagensCapturadasNoArmazenamentoDoAplicativo`
- `GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers` (e saves Sjinn/Topview que passam por ela)
- `PainelDaGavetaDaBibliotecaDeVideos` (+ layouts novos do modal/chips se preciso)
- Serviço/ponte de geração: passar origem/modelo/prompt/refs até o save

### Critérios de aceite — Fase 1

1. Gerar vídeo e imagem em Higgsfield, Sjinn e Topview → histórico mostra origem correta; chips filtram.
2. ⓘ mostra prompt completo e miniaturas das refs usadas naquela geração.
3. Baixar vídeo → arquivo em `Downloads/ChromeBrothers` e listável em Baixados; baixar imagem → `Pictures/ChromeBrothers`.
4. Nenhum `AlertDialog` novo neste fluxo.
5. JSON antigo abre sem crash; itens pré-feature em **Desconhecido**.
6. Toque no cartão (fora do ⓘ) ainda abre a mídia.

## Fase 2 — Arraste do histórico (visão; implementar depois)

### Intenção

Toque longo no cartão da gaveta (imagem **e** vídeo) inicia thumbnail translúcida; soltar:

- no **gerador MCP** de imagem/vídeo da coleção → adiciona como **referência**;
- em **input web** que aceita arquivo/colar (ex. ChatGPT) → entrega arquivo via DnD de sistema.

### Distinção técnica importante

| Pipeline | O que é |
|----------|---------|
| Lift-drag atual (coleção → editor) | Overlay nativo interno; **não** alimenta WebView/`input type=file` |
| Necessário para ChatGPT / file input | `View.startDragAndDrop` + `ClipData` + `content://` (FileProvider) |
| Vídeo no lift atual da coleção | Vira **frame JPEG**, não o MP4 |

### Gestos (Fase 2)

| Gesto | Ação |
|-------|------|
| Toque curto | Abrir mídia |
| ⓘ | Modal detalhe (Fase 1) |
| Toque longo | Iniciar arraste (não abrir modal) |

### Alvos

1. **Slots `#refs` do HTML MCP** (imagem e vídeo): hit-test / bridge `adicionarReferencia(uri)` — hoje soltar sobre a janela da coleção **cancela** no pipeline do editor; Fase 2 precisa alvo explícito.
2. **WebView da página**: DnD de sistema com MIME adequado (`image/*` ou `video/*`).
3. Editor de composição: reusar ou adaptar fantasma existente se fizer sentido (secundário).

### Vídeo

- Preferir arrastar o **arquivo de vídeo** quando o alvo aceitar `video/*`.
- Se o alvo só aceitar imagem (ex. ref de image-to-video que exige frame): usar frame/JPEG com feedback claro — detalhar no plano da Fase 2.

### Fora da Fase 2 (até reavaliar)

- Tags livres
- Downloads raiz / SAF
- Substituir o lift-drag coleção→editor pelo DnD de sistema em todos os lugares

### Critérios de aceite — Fase 2 (quando for implementar)

1. Long-press em cartão de imagem/vídeo do histórico inicia fantasma translúcido.
2. Soltar no gerador MCP adiciona ref sem passar pelo picker.
3. Soltar em página tipo ChatGPT anexa/cola o arquivo quando o site aceitar drop de arquivo.
4. ⓘ e toque curto permanecem intactos.

## Fora de escopo (geral)

- Tags/categorias editáveis pelo usuário
- Abrir visualizador de Baixados na raiz de Downloads
- Backfill de metadados em itens antigos
- Implementação da Fase 2 no mesmo PR/ciclo da Fase 1

## Riscos

| Risco | Mitigação |
|-------|-----------|
| JSON antigo quebra parse | Campos opcionais + default `desconhecido` |
| Refs grandes enchem disco | Copiar só no save MCP; limitar N refs ao que o gerador já limita |
| Confusão ⓘ vs long-press (Fase 2) | Documentar gestos; Fase 2 só após ⓘ estável |
| DnD web inconsistente entre sites | Fase 2: aceitar melhor esforço + FileProvider; testar ChatGPT como alvo canônico |

## Ordem de trabalho sugerida

1. Spec aprovada (este arquivo).
2. Plano de implementação **somente Fase 1**.
3. Implementar + instalar Fase 1.
4. Novo plano + implementação da Fase 2.
