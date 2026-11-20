# Design: motor de carimbos e Estúdio de pincéis do editor de imagem

Data: 2026-11-14
Status: aprovado; fases 1, 2 e 3 implementadas (2026-11-14)

> **Fase 3 — exportar/importar (feito):** `.chromebrush` = zip com `pinceis.json` (`{"versao":1,"pinceis":[…]}`) e `imagens/<nome>` (formas e granulados usados). Exportar todos os "Meus pincéis" (painel do pincel) ou o pincel aberto (botão "Exportar" no Estúdio) para `Downloads/ChromeBrothers`. Importar pelo seletor de arquivos: só entram o manifesto e imagens com nome seguro, até 5 MB cada e 200 pincéis; imagens são decodificadas e regravadas com nome novo; id que já existe ganha id novo e "(importado)"; imagem que faltar faz a forma voltar a redonda / o granulado sumir (`PacoteDePinceisChromebrush`, `PacoteDePinceisNoAparelho`).

> **Fase 3 — borracha com pincel do motor (feita):** tocar na borracha já ativa abre o painel "Borracha" (comum ou qualquer pincel do motor, prontos e meus). O traço da borracha guarda o pincel e apaga com os carimbos dele por `DST_OUT`, limitado pela opacidade da borracha (via limites do pincel), com tamanho da borracha e pressão. Em andamento a tinta da camada é desenhada numa camada isolada e a borracha fura só ela; ao soltar, o traço final apaga a tinta; em camada de imagem apaga os pixels. A borracha comum (redonda) continua igual.
>
> **Fase 3 — granulado (feito):** `FonteDoGranulado` (nenhum, papel e tela gerados pelo app — ladrilham sem emenda —, ou imagem da galeria), modo preso ao papel (máscara `DST_IN` ladrilhada nas coordenadas da folha, aplicada na camada do traço; escala passada por quem desenha: tinta, imagem, sobreposição) ou acompanhando (textura embutida na forma do carimbo), escala e intensidade. Seção "Granulado" no Estúdio. O Giz pronto passou a usar granulado de papel.
>
> **Fase 3 — dinâmica de velocidade (feita):** `velocidadeNoTamanho` e `velocidadeNaOpacidade` (0..1). A velocidade (dp/ms entre eventos do toque, suavizada) é aplicada na largura/opacidade do ponto no momento em que ele é criado, como a pressão: o desenho continua repetível e o rascunho não muda de formato. Rápido = até `1 − 0,9 × sensibilidade` a partir de 2,5 dp/ms. Seção "Dinâmica" no Estúdio; a área de teste registra o tempo dos pontos e o traço de exemplo tem velocidade variando.

> **Fase 2 como implementada:** `EstudioDePinceisDoEditorDeImagem` (tela cheia) com seções Traço, Afinamento, Forma (redonda/quadrada/granulada/imagem da galeria, dureza, arredondamento, girar com o traço, ângulo, rotação aleatória), Caneta e Limites (tamanho base, mín/máx de tamanho e opacidade), mais área de teste ao vivo. Pincel pronto é editado como cópia em "Meus pincéis" (`CatalogoDePinceisDoMotor`, `pinceis/meus-pinceis.txt`); formas de imagem viram máscara 256×256 (claro = tinta) em `pinceis/formas/`, com nome único e nunca sobrescritas. O rascunho guarda a cópia de cada versão de pincel usada (`pinceldef <chave> <json>`; traço aponta pela chave `id~hash`), então editar ou apagar um pincel não muda traços já feitos. Apagar usa "toque de novo para confirmar" (sem `AlertDialog`).

> **Fase 1 como implementada:** em vez de refazer caneta/marcador/nanquim/lápis no motor (risco de mudar o visual sem ganho imediato), essas pontas continuam no desenho atual e o motor estreia com **cinco pincéis novos**: Nanquim afinado, Pena caligráfica, Pincel seco, Giz e Aerógrafo (`PinceisPredefinidosDoMotor`). A borracha também continua no caminho atual; ela passa para o motor na fase 2, quando der para escolher o pincel da borracha. Rascunho: `traco … pincel=<id> semente=<n>` (campos opcionais, separados antes da leitura por posição).

## Problema

O editor desenha cada traço como uma linha de largura variável (`drawLine` ponto a ponto). Os pincéis atuais (caneta, marcador, nanquim, lápis) só mudam largura, ponta e transparência. Não dá para ter textura, granulado, dispersão, afinamento real nem criar pincéis próprios, que é o que faz o Brush Studio do Procreate ser útil.

## Objetivo

1. **Motor de carimbos:** o traço passa a ser feito carimbando uma forma ao longo do caminho, com espaçamento e variações controladas por parâmetros.
2. **Estúdio de pincéis:** tela para criar e ajustar pincéis com uma área de teste ao vivo.
3. **Biblioteca de pincéis:** pincéis prontos + os do usuário, com criar, duplicar, renomear, apagar e exportar.

Os pincéis de hoje viram predefinições do motor novo e devem continuar com a mesma cara.

## Fora de escopo (por enquanto)

- Mistura molhada (wet mix), dinâmica de cor, materiais 3D, pincéis duplos (dual brush).
- Importar `.brush` do Procreate: avaliar depois da fase 3 (é um zip com `Shape.png`, `Grain.png` e um plist binário; o mapeamento dos parâmetros é aproximado).

## Modelo

```
PincelDoEditor
  id, nome, predefinido: Boolean
  forma:      fonte (REDONDA | QUADRADA | IMAGEM:<arquivo>), dureza 0..1, rotação (FIXA | SEGUE_O_TRAÇO), rotaçãoGraus,
              dispersão 0..1, quantidade 1..8, inverterX/Y aleatório
  granulado:  fonte (NENHUM | IMAGEM:<arquivo>), modo (ACOMPANHA | TEXTURA_FIXA), escala, intensidade 0..1
  traço:      espaçamento (% do diâmetro), tremor (jitter) de posição, suavização (estabilização já existente)
  afinamento: início 0..1, fim 0..1 (tamanho e opacidade), também com o dedo (sem pressão)
  dinâmica:   tremor de tamanho, tremor de opacidade, velocidade → tamanho / opacidade
  caneta:     pressão → tamanho, pressão → opacidade, pressão → fluxo, inclinação → tamanho / opacidade
  limites:    tamanho mín/máx (sobre a curva de 1..100%), opacidade mín/máx
  renderização: fluxo 0..1, modo (NORMAL | ACUMULA_ATÉ_OPACIDADE — "glazed" simplificado)
```

Funções puras no módulo `regras-de-video-do-navegador` (testáveis): espaçamento ao longo do caminho, afinamento, curvas de pressão/velocidade, tremores determinísticos a partir de uma semente.

## Motor de carimbos

- **Carimbo:** bitmap da forma (gerado para redonda/quadrada com dureza; carregado para IMAGEM), tingido com a cor do traço via `PorterDuffColorFilter`.
- **Caminho:** pontos do traço reamostrados a cada `espaçamento × diâmetro`; em cada amostra: posição (+ dispersão), tamanho, opacidade, rotação, calculados pelos parâmetros + pressão/velocidade daquele ponto + tremor.
- **Afinamento do fim ao vivo:** o traço em andamento é gerado já com o afinamento do fim. Os carimbos antes da cauda (`inicioDaCauda`) são definitivos e vão para a camada do traço; a cauda é refeita a cada quadro por cima. Ao soltar, o traço não muda. O ponto único usa o mesmo afinamento do início do carimbo 0 (antes saía em tamanho cheio e deixava uma "bolota" até soltar).
- **Afinamento do fim curto (até 3 diâmetros; o do início vai até 8):** como o fim afina ao vivo, uma cauda longa fazia o traço "encher" atrás da caneta até ficar uniforme (com 95% eram ~8 diâmetros, quase o traço inteiro). Traços antigos com afinamento do fim longo passam a afinar num trecho menor.
- **Pressão ancorada na pressão normal (40%):** nela o pincel tem o tamanho/opacidade escolhidos; apertar engrossa até `1 + sensibilidade` (2× com 100%) e aliviar afina; a opacidade só clareia abaixo da normal. (Antes a pressão máxima valia 100% e a sensibilidade só reduzia, então aumentar a sensibilidade afinava o traço inteiro desenhado com pressão normal.)
- **Pincéis prontos recalibrados** para a pressão normal: tamanho base Nanquim afinado 0,55, Pena 1,0, Pincel seco 1,4, Giz 1,4, Aerógrafo 2,6 (fluxo 0,07), para ficarem como eram com pressão normal antes da pressão ser ancorada.
- **Determinismo:** cada traço guarda `idDoPincel`, uma **cópia dos parâmetros** (para o traço não mudar se o pincel for editado depois) e uma **semente**. Redesenhar (desfazer, rascunho, recomposição) dá exatamente o mesmo resultado.
- **Opacidade do traço:** carimbos acumulam com `fluxo` numa camada própria do traço e o resultado é limitado pela opacidade escolhida (como no Procreate: o traço não fica mais escuro que a opacidade, a não ser no modo que acumula).
- **Granulado:** aplicado como máscara sobre os carimbos (`ACOMPANHA`: textura presa ao carimbo; `TEXTURA_FIXA`: textura presa ao papel, coordenadas da folha).
- **Borracha:** usa o mesmo motor com `DST_OUT`, então ganha forma e textura do pincel escolhido (como "apagar com este pincel" no Procreate).
- **Desempenho:** desenho incremental na tinta da camada como hoje (`aplicarTrechoAtualNaTinta`); só os carimbos novos de cada quadro. Carimbos em cache por (forma, dureza, tamanho arredondado). Meta: 60 fps num traço de 300 dp com espaçamento 10% no tablet.

## Compatibilidade

- Traços antigos (sem `pincel` no rascunho) continuam desenhados pelo caminho atual (`drawLine`). Nada muda em rascunhos existentes.
- O formato do rascunho ganha campos opcionais na linha `traco` (`pincel=<id> semente=<n> params=<base64>`); leitores antigos ignoram.
- Caneta, marcador, nanquim e lápis viram pincéis predefinidos com parâmetros que reproduzem o visual atual; teste visual comparando antes/depois.

## Estúdio de pincéis

- Abre pelo ícone do pincel (toque longo) ou por "Editar pincel" na lista de pincéis.
- **Layout:** à esquerda as seções (Traço, Afinamento, Forma, Granulado, Dinâmica, Caneta, Limites, Sobre); no meio os controles da seção; à direita a **área de teste** (desenha com o pincel em edição; botão limpar; troca de cor rápida).
- Controles: deslizantes com valor numérico, alternâncias, e para Forma/Granulado o editor da fonte (escolher da galeria/biblioteca, inverter, prévia).
- Padrão Chromebrothers (família das caixas do editor, sem `AlertDialog`).
- "Desfazer tudo" volta o pincel ao estado em que o estúdio foi aberto.

## Biblioteca de pincéis

- Lista com prévia do traço de cada pincel, agrupada (Predefinidos / Meus pincéis).
- Ações: duplicar, renomear, apagar (só os meus), exportar (`.chromebrush` = zip com `pincel.json` + imagens) e importar o mesmo formato.
- Guardados em `filesDir/pinceis/<id>/` (JSON + imagens de forma/granulado).

## Fases

| Fase | Entrega | Critério de aceite |
|------|---------|--------------------|
| 1 | Motor de carimbos + predefinidos equivalentes aos atuais + borracha no motor | Traços novos com caneta/marcador/nanquim/lápis ficam visualmente iguais; rascunhos antigos abrem iguais; 60 fps |
| 2 | Estúdio com Traço, Afinamento, Forma, Caneta e Limites + área de teste | Criar um pincel do zero (forma de imagem + afinamento + pressão) e desenhar com ele |
| 3 | Granulado, Dinâmica, biblioteca com exportar/importar | Pincel com granulado fixo no papel; exportar e importar `.chromebrush` |

## Riscos

| Risco | Mitigação |
|-------|-----------|
| Desempenho do carimbo em pincéis grandes/densos | Cache de carimbos, espaçamento mínimo, incremental na tinta; medir na fase 1 antes de seguir |
| Visual dos predefinidos mudar | Parâmetros calibrados por comparação de imagens antes/depois |
| Rascunho crescer com parâmetros por traço | Guardar parâmetros uma vez por pincel usado no documento, traço referencia por índice |
| Escopo do estúdio crescer | Fases fechadas; wet mix e afins só com spec própria |

## StreamLine (por pincel)

- Campo `streamline` (0..1) no `PincelDoMotor`, salvo no JSON (pincéis antigos leem 0). Seção "Estabilização" no Estúdio.
- Regra pura `StreamLineDoTraco`: atraso exponencial **por tempo** (τ = 110 ms × s^1,6), igual para dedo e caneta; ao levantar a caneta, `alcancar` continua seguindo a ponta parada em passos de 4 ms até chegar nela (máx. 150 passos), então o fim não fica curto.
- No editor entra depois da estabilização de corda (geral) e fica desligado com a régua. A área de teste do Estúdio aplica o mesmo cálculo nos pontos gravados.
- Predefinidos: Nanquim afinado 30%, Pena 20%, Pincel seco 10%, Giz e Aerógrafo 0.
