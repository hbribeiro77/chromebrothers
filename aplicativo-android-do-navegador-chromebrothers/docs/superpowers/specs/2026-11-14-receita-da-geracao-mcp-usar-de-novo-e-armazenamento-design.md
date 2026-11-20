# Design: receita da geração MCP, "Usar de novo" e gerenciamento de armazenamento

Data: 2026-11-14
Status: aprovado (2026-11-14)

## Problema

1. **Resultados somem entre sessões.** `CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho.atualizar` apaga o card quando ele vira `pronto`. Ao reabrir o gerador, só os cards com falha (ou "Demorou") voltam.
2. **As referências não sobrevivem ao pedido.** As refs ficam só na memória do serviço (`ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.referencias`). Os vídeos de referência ficam em `cacheDir/referencias-de-video`, que o Android pode limpar. Um card que falhou não pode ser tentado de novo com o mesmo contexto depois que o app fecha.
3. **A biblioteca guarda um contexto incompleto.** Desde a spec de 07/11 (metadados da gaveta), cada item gerado guarda `origem`, `modelo`, `prompt` e cópias das refs, mas **referência de vídeo vira só um quadro JPEG** e os parâmetros do pedido (proporção, duração etc.) não são guardados.
4. **Não existe "gerar de novo"** a partir de um resultado ou de uma falha.
5. **Não há visão do espaço usado.** A mídia gerada, as refs, os rascunhos e o cache crescem sem que o usuário veja ou controle.

## Objetivo

| # | Entrega |
|---|---------|
| A | **Receita** persistente por pedido: tudo o que foi enviado ao MCP (prompt, MCPs, modelo, parâmetros, refs de imagem **e vídeo**) |
| B | **Cards prontos persistem** no gerador (imagem e vídeo), com miniatura e vínculo com o item da biblioteca |
| C | **"Usar de novo"** no card (pronto/falho) e no ⓘ da biblioteca → abre o gerador certo **preenchido**, para revisar e gerar |
| D | **"Tentar de novo"** no card com falha: reenvia a mesma receita, sem abrir nada |
| E | **Armazenamento**: ver quanto cada parte ocupa e liberar espaço com segurança |

## Decisões (conversa de 2026-11-14)

| Tema | Escolha |
|------|---------|
| "Usar de novo" | **Preenche** o gerador para revisão; não dispara sozinho |
| Gerar direto | Só o **"Tentar de novo"** em card com falha (mesma receita, um toque) |
| Onde rever resultados | **Gerador e biblioteca** (o card pronto aponta para o item da biblioteca) |
| Espaço em disco | Sem limpeza silenciosa: tela de **Armazenamento** com uso por categoria e ações de limpeza; limpeza automática **opcional e desligada** por padrão |
| Favoritos | **Nunca** são apagados por limpeza automática ou em lote |

## A — Receita da geração

### Modelo

```
ReceitaDeGeracaoMcp
  id: String                    // curto, como o id do card
  tipo: "imagem" | "video"
  criadoEmMs: Long
  pedidoJson: String            // o JSON exatamente como o componente enviou (mcps, modelo, prompt,
                                // quantidade, proporção, duração…), para o preenchimento ser fiel
  refs: List<RefDaReceita>

RefDaReceita
  tipo: "imagem" | "video"
  nome: String                  // nome mostrado no slot
  hash: String                  // sha-256 do conteúdo
  extensao: String
  duracaoMs: Long               // vídeos; 0 para imagem
  idDoItemDaBiblioteca: String? // se veio da biblioteca (ver "Refs vindas da biblioteca")
```

Guardar o `pedidoJson` bruto, em vez de mapear campo a campo, mantém a receita compatível com parâmetros novos que os geradores ganharem, sem migração.

### Onde fica

- `filesDir/receitas-de-geracao/<id>.json`: uma receita por arquivo.
- `filesDir/refs-das-receitas/<hash>.<ext>`: **armazenamento por conteúdo**. A mesma imagem usada em 10 receitas ocupa espaço uma vez só.
- Contagem de uso: calculada ao varrer as receitas (não há contador gravado que possa dessincronizar).

### Refs vindas da biblioteca

> **Mudança na implementação (fase 1, 2026-11-14):** a ideia original era apontar para o item da biblioteca e movê-lo ao apagar. Na prática, o slot de referência já recebe sempre uma **cópia** (inclusive de itens da biblioteca), sem guardar de onde veio. Por isso, toda ref é copiada para `refs-das-receitas/<hash>.<ext>` e o dedupe por hash evita repetir o mesmo arquivo entre receitas. Custo: uma ref tirada da biblioteca ocupa espaço duas vezes (item + ref). Ganho: apagar algo da biblioteca nunca quebra uma receita, e não há aviso extra na exclusão. Se o espaço pesar, a fase 3 mostra isso em "Referências das gerações".

- Toda ref (galeria, página, câmera, biblioteca) é **copiada** para `refs-das-receitas/` no momento do Gerar.
- `cacheDir/referencias-de-video` continua sendo só a área temporária do slot. No Gerar, o vídeo vai para `refs-das-receitas/` e a geração passa a usar essa cópia (limpar o slot no meio da geração não quebra mais o envio).
- Vídeo guarda também o quadro JPEG (`hashDoQuadro`), que serve de prévia e de metadado da biblioteca.

### Quem aponta para a receita

| Registro | Campo novo |
|----------|------------|
| `CardDoPedidoDeVideoViaMcp` (vídeo e imagem) | `idDaReceita` |
| `ItemDaBibliotecaDeVideos` / item de imagem | `idDaReceita` (opcional; itens antigos ficam sem) |

Todos os cards de um mesmo pedido (quantidade × MCPs) compartilham **uma** receita.

Os campos `prompt`/`refs`/`modelo` dos itens da biblioteca continuam sendo gravados (o ⓘ atual segue funcionando e itens antigos não quebram).

## B — Cards prontos persistentes

- `atualizar` deixa de remover o card `pronto`. Ele continua no JSON com `idBiblioteca`, `fileUrl` e o caminho de uma **miniatura em disco** (não o `previewDataUrl` base64, que incharia o JSON).
- O limite `LIMITE_DE_CARDS_GUARDADOS = 60` vale para os prontos também. Ao estourar, saem os prontos mais antigos (falhas e em andamento têm prioridade para ficar). Tirar o card do gerador **não** apaga o item da biblioteca.
- `restaurarCards()` no HTML pinta os prontos como hoje (`pintarCardPronto`): tocar abre no player/visor.
- Se o item da biblioteca sumiu, o card mostra "Removido da biblioteca" e mantém "Usar de novo" (a receita continua existindo).
- Botão ✕ do card: tira do gerador, sem apagar da biblioteca.

## C — "Usar de novo"

### Onde aparece

1. Card do gerador: prontos e falhos.
2. Modal ⓘ da gaveta (vídeos e imagens geradas).

### O que faz

1. Abre (ou traz para frente) o componente gerador do `tipo` da receita na coleção.
2. Entrega a receita ao componente pela ponte: `ponte.receitaParaPreencher()` devolve a receita pendente uma vez (o componente consulta ao carregar e ao receber um evento `cb-receita`).
3. O componente preenche prompt, MCPs marcados, modelo, quantidade e parâmetros a partir do `pedidoJson`. O serviço recoloca as refs nos slots a partir dos arquivos da receita (`referencias` em memória são substituídas).
4. O usuário revisa, muda o que quiser e aperta **Gerar**. Isso cria uma **receita nova** (a antiga não é alterada).

### Casos especiais

| Caso | Comportamento |
|------|---------------|
| Ref com arquivo ausente | Preenche o resto; o slot mostra "Referência não encontrada" e um aviso no topo |
| MCP sem sessão / modelo que não existe mais | Preenche e mostra o aviso que o gerador já mostra hoje ao escolher |
| Item antigo sem receita | Monta uma receita "parcial" na hora com `prompt`, `origem`, `modelo` e as refs de imagem que o item tiver; avisa que parâmetros e vídeos de referência não foram guardados naquela época |
| Gerador com conteúdo já digitado | Substitui; o texto anterior pode voltar com um "Desfazer" no aviso |

## D — "Tentar de novo" (card com falha)

- Reenvia o `pedidoJson` da receita **só para o MCP daquele card**, com quantidade 1, usando as mesmas refs.
- O card falho é substituído por um card novo em `enviando`, ligado à mesma receita. *(Implementado com número novo da sequência, sem o sufixo `#12·2`, para não mudar o formato do número nos cards.)*
- Só aparece em card **com falha** e com receita. Em "Demorou", a tarefa ainda pode terminar, então o botão continua sendo "Verificar" (evita gerar em dobro).
- "Verificar" (cards com tarefa que "Demorou") continua igual.

## E — Armazenamento

> **Como implementado (2026-11-14):** seção "Armazenamento" na Configuração do chat (resumo com o total, medido ao abrir a seção) + botão "Gerenciar armazenamento" que abre `PainelDoArmazenamentoDoAplicativo` em tela cheia. Medição pela pasta de dados inteira (`MedicaoELimpezaDoArmazenamentoDoAplicativo`, categorias em `ArmazenamentoDoAplicativo`), incluindo "Conversas do chat" e "Dados do navegador" (perfil do Gecko e WebView). Diferenças do plano: confirmações em dois toques no próprio botão (mostrando quanto libera), em vez de caixa de diálogo; o cache do Gecko e do WebView é limpo pela API deles (`StorageController.clearData`, `WebView.clearCache`) e não apagando as pastas; "Maiores arquivos" lista a mídia gerada (a mesma lista e a mesma exclusão do visualizador de mídia, que mantêm os índices das bibliotecas); "Referências sem uso" inclui as refs do formato antigo que nenhum item lista e miniaturas de cards que saíram, ignorando arquivos com menos de 10 minutos; o atalho na gaveta da biblioteca foi acrescentado depois (ícone ao lado do fechar, abre a mesma tela).

### Onde fica

Nova seção **Armazenamento** na tela de Configuração (mesmo padrão das seções colapsáveis da spec 2026-11-07 da configuração do chat). A linha-resumo mostra o total, ex.: "Armazenamento · 2,4 GB". Atalho também no topo da gaveta da biblioteca (ícone de disco), que abre a mesma tela.

### O que mostra

Barra empilhada com o total do app e uma lista por categoria (tamanho + quantidade):

| Categoria | Pastas |
|-----------|--------|
| Vídeos gerados | `biblioteca-de-videos-higgsfield/` |
| Vídeos salvos da web | `biblioteca-de-videos-com-miniatura/` |
| Imagens | `biblioteca-de-imagens-capturadas/` |
| Referências das gerações | `refs-das-receitas/` + `receitas-de-geracao/` + `miniaturas-dos-cards-mcp/` + `refs-de-geracao-da-biblioteca/` (legado) |
| Rascunhos do editor | `rascunhos-da-edicao-de-imagem/` |
| Páginas, roteiros e abas | `biblioteca-de-paginas-da-colecao/`, `roteiros-de-automacao/`, `abas-abertas/` (só informativo) |
| Cache | `cacheDir` inteiro (miniaturas, downloads temporários, diagnósticos) |

O cálculo roda em segundo plano ao abrir a tela. Enquanto isso, mostra "Calculando…" por categoria.

### Ações

| Ação | O que apaga | Segurança |
|------|-------------|-----------|
| **Limpar cache** | `cacheDir` exceto o que está em uso agora (slots de referência abertos, arraste em andamento) | Seguro; tudo é refeito sob demanda |
| **Referências sem uso** | Refs de receitas que não têm mais nenhum card nem item da biblioteca (`ReceitaDeGeracaoMcp.arquivosEmUso` já existe) | Mostra "Liberar X MB (N arquivos)" antes de confirmar |
| **Miniaturas órfãs** | `miniaturas-dos-cards-mcp/` de cards que saíram da fila pelo limite de 60 | Automático junto com "Limpar cache" (só prévias; refeitas não são necessárias) |
| **Maiores arquivos** | Lista dos itens gerados/salvos por tamanho (miniatura, tamanho, data, origem) com seleção em lote | Reusa a seleção em lote da spec 2026-11-09; favoritos marcados com ★ e fora da seleção "Selecionar tudo" |
| **Rascunhos antigos** | Rascunhos do editor não abertos há mais de 30 dias | Lista antes de confirmar |
| **Limpeza automática** (opção) | Refs sem uso e cache, a cada abertura do app, se tiverem mais de 30/90 dias | **Desligada** por padrão; nunca toca em mídia da biblioteca nem em favoritos |

Toda confirmação usa o `Dialog` no padrão Chromebrothers (família `layout_caixa_de_dialogo_do_editor_de_imagem`), sem `AlertDialog`, e diz quanto espaço será liberado.

### Fora desta seção

- Mover mídia para o cartão SD ou para pasta pública em lote (o "Baixar" individual já existe).
- Cota máxima com apagamento automático de mídia.

## Arquitetura

```
[Gerar no componente]
   → Servico.iniciar{Imagens,Videos}PorCards(pedidoJson)
       → ReceitasDeGeracaoMcpGuardadasNoAparelho.criar(tipo, pedidoJson, refs)
            → copia/aponta refs em refs-das-receitas/<hash>
       → cards.criar(..., idDaReceita)
       → ao ficar pronto: gravacao.guardar…ComMetadados(..., idDaReceita) + miniatura do card

[Usar de novo]  (card ou ⓘ)
   → Servico.prepararReceitaParaPreencher(idDaReceita | itemAntigo)
       → recoloca refs nos slots
   → abre componente do tipo → componente lê ponte.receitaParaPreencher() → preenche

[Tentar de novo]
   → Servico.repetirCard(idDoCard) → mesma receita, 1 card, mesmo MCP

[Armazenamento]
   → MedicaoDoArmazenamentoDoAplicativo (varre pastas; regras puras testáveis)
   → LimpezaDoArmazenamentoDoAplicativo (cache, refs sem uso, rascunhos antigos)
```

Classes novas (orientação):

- `regras/ReceitaDeGeracaoMcp.kt`: modelo + JSON + "refs sem uso" (função pura: receitas × cards × itens → hashes livres). Testes JVM.
- `ReceitasDeGeracaoMcpGuardadasNoAparelho.kt`: disco.
- `regras/MedicaoDoArmazenamentoDoAplicativo.kt`: agrupamento por categoria (pura, recebe a lista de arquivos). Testes JVM.
- `PainelDoArmazenamentoDoAplicativo.kt` + layout: tela.

Classes tocadas: `FilaDosCardsDosPedidosDeVideoViaMcpComEstadoPorCard`, `CardsDosPedidosDeVideoViaMcpGuardadosNoAparelho`, `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers`, `GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers`, `HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao` (+ o de imagem), `PainelDaColecaoFlutuanteDoChatDoNavegador` (ponte), `PainelDaGavetaDaBibliotecaDeVideos` (ⓘ, apagar com aviso).

## Fases

| Fase | Entrega | Por quê nessa ordem |
|------|---------|---------------------|
| 1 | A + B: receita, refs permanentes (incl. vídeo), cards prontos persistentes | Destrava todo o resto; resolve o "só a falha sobrevive" |
| 2 | C + D: "Usar de novo" (card e ⓘ) e "Tentar de novo" | Depende da receita |
| 3 | E: tela de Armazenamento e limpezas | A fase 1 passa a guardar vídeos de referência, então o controle de espaço vem logo em seguida |

## Critérios de aceite

1. Gerar 2 imagens e 1 vídeo, fechar o app (forçar parada) e reabrir: os cards prontos aparecem nos dois geradores com miniatura e abrem a mídia.
2. Gerar vídeo com 1 imagem + 1 vídeo de referência; no ⓘ do resultado, as duas refs aparecem e o vídeo de referência toca.
3. "Usar de novo" no card e no ⓘ preenche prompt, MCPs, modelo, quantidade, parâmetros e os dois slots; Gerar cria uma receita nova.
4. Card com falha + app reiniciado → "Tentar de novo" reenvia com as mesmas refs.
5. Apagar da biblioteca um item usado como referência → aviso; a receita continua abrindo com a ref.
6. Armazenamento mostra os totais por categoria; "Limpar cache" e "Referências sem uso" liberam o espaço anunciado; favoritos nunca entram em limpeza automática.
7. JSONs antigos (cards e biblioteca) abrem sem crash; item antigo usa a receita parcial.
8. Nenhum `AlertDialog` novo.

## Riscos

| Risco | Mitigação |
|-------|-----------|
| Vídeos de referência ocupam muito espaço | Dedupe por hash + "Referências sem uso" + tela de Armazenamento na fase 3 |
| Hash de vídeo grande trava a UI | Calcular no thread do Gerar (já fora da UI), em streaming |
| `pedidoJson` antigo com campo que o gerador não conhece mais | O componente ignora campos desconhecidos e usa o padrão para os ausentes |
| Ref apontando para item da biblioteca apagado por fora do fluxo (ex.: seleção em lote) | Toda exclusão de mídia passa pela mesma rotina que move refs em uso; teste cobrindo a exclusão em lote |
| Cards prontos enchendo o gerador | Limite de 60 com prioridade para falhas/andamento; ✕ tira do gerador |
