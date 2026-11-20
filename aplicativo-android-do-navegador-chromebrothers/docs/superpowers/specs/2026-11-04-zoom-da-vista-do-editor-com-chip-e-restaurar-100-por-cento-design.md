# Zoom da vista do editor com chip e restaurar 100%

Data: 2026-11-04  
Status: implementado (2026-11-04)

## Problema

O zoom da vista do editor de imagem está limitado a 100%–800% (100% = encaixe na tela). Não dá para afastar o quadro abaixo do encaixe, não há indicação de porcentagem e não há atalho para voltar ao encaixe centralizado.

## Objetivo

Permitir zoom da **vista** abaixo de 100%, mostrar um chip com a porcentagem atual e permitir restaurar a vista a 100% + centro com um toque no chip — sem alterar moldura, peças nem histórico de edição.

## Fora de escopo

- Botões − / + ou slider permanente na barra
- Alterar tamanho em pixels da moldura ou das peças (“encaixar imagem na moldura”)
- Registrar zoom/pan no histórico de desfazer
- Duplo toque no canvas para restaurar

## Abordagem

**A — Chip de zoom**  
Pinça com range ampliado; chip flutuante com `%`; toque no chip restaura 100% + centro.

## Comportamento do zoom

- Range: **0,25–8,0** (25%–800%). `1,0` = encaixe na tela (`escalaBase`).
- Pinça de dois dedos: mesma lógica atual, mínimo passa de `1f` para `0,25f`.
- Abaixo de 100%: quadro menor; quando cabe na tela, pan zerado/centralizado (já tratado em `calcularDestino`).
- **Restaurar**: `zoom = 1`, `panX = 0`, `panY = 0`, recalcular destino. Não mexe em moldura, peças nem pilha de undo.
- Abrir / trocar imagem / resets que já zeram zoom: continuam em **100%**.

## Chip de %

- Overlay no canto do canvas (ex.: superior direito), texto `67%` (inteiro arredondado de `zoom * 100`).
- Aparece ao mudar o zoom; some ~1 s após o último gesto.
- Em 100% após restaurar: mostra `100%` brevemente e some.
- Toque no chip = restaurar vista.
- Visual: família das caixas do editor (fundo escuro arredondado, texto claro). Sem `AlertDialog`.
- Toque só na área do chip; resto do canvas inalterado.
- `contentDescription` acessível: zoom atual + “toque para restaurar 100%”.

## Regras (módulo `regras-de-video-do-navegador`)

- Clamp do zoom no intervalo `[0,25, 8]`.
- Formatação do rótulo percentual.
- Função/valor de restaurar ⇒ `1f` (e conceito de pan zerado fica no editor).

## Encaixe técnico

- `EditorDeRecorteETracoDaImagemDaBiblioteca`: clamp, `restaurarZoomDaVista()`, notificação do chip.
- Chip: `TextView` no `FrameLayout` pai do editor (`layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`) ou equivalente ligado pelo painel.
- Strings pt-BR para % e acessibilidade.
