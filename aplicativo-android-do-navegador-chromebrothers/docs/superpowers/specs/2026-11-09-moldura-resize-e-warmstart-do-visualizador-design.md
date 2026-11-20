# Moldura da modal, resize leve e warm-start do Visualizador — Design

**Status:** aprovado (conversa 2026-11-09)

## Objetivo

Pacote curto prazo no Visualizador (ainda em WebView HTML):

1. **Moldura** na thumb de origem enquanto a modal de opções estiver aberta.
2. **Resize** sem relayout contínuo da WebView.
3. **Segundo componente preto**: warm-start sem superfície morta.

Fora de escopo: galeria nativa (`RecyclerView`), pré-render de todas as thumbs invisíveis.

## 1. Moldura

- Cor `#f5c518`, ~3dp (outline/box-shadow na `.carta`).
- Ao abrir `mostrarMenuDeContextoDaMidiaDoComponente`, destacar a carta cuja URI bate com a da modal (comparar sem query `cb_thumb`).
- Limpar ao dismiss da modal (qualquer ação ou cancelar).
- API: `Painel.destacarMidiaNaGrade(uri)` / `limparDestaqueNaGrade()`; `Gestor` propaga para as janelas.

## 2. Resize

- Em `ACTION_MOVE` da alça: **não** alterar `layoutParams` do painel/WebView.
- Mostrar **outline** (View leve no mesmo parent) com a geometria pretendida.
- Em `ACTION_UP`/`CANCEL`: aplicar width/height **uma vez**, esconder outline, `persistirDimensoesAtuais()`.

## 3. Warm-start / preto

- `carregarComponenteEmWarmStart`: painel em `INVISIBLE` (não `GONE`), mantendo layout; selo continua `GONE`.
- Ao revelar (`mostrar` / `crescer` / fim do warm): `webDaPagina.onResume()`, `resumeTimers()`, `invalidate()`.
- Fallback só se necessário: reload controlado no revelar após warm-start (não no caminho feliz de janela já aberta).

## Critérios de aceite

- Long-press sem arrastar → modal + moldura dourada na thumb certa; some ao fechar.
- Arrastar a alça de resize → gesto fluido; tamanho definitivo só no soltar.
- Abrir segundo Visualizador (ou revelar warm-start) → grade visível, sem tela preta persistente.
