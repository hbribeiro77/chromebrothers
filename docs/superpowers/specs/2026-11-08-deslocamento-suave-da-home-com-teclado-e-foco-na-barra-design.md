# Deslocamento suave da home com teclado (foco na barra)

**Status:** aprovado (“bora”, 2026-11-08)

- Com foco no chrome nativo (barra/omnibox): **não** deslocar a página (`deslocamentoY = 0`); só clipping do IME.
- Com foco na página: automático atual (medir campo + pan).
- `NAO_DESLOCAR` continua sem pan/clipping.
- Select: overlay na raiz (não empurra a página); animação só slide/fade.
