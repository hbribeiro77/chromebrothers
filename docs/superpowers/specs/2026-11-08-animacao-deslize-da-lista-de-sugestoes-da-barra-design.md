# Animação da lista de sugestões da barra

**Status:** aprovado (“fecho”, 2026-11-08)

- Abrir: slide de cima → baixo (~−40% altura ou −28 dp) + alpha 0→1, ~200 ms, FastOutSlowIn.
- Fechar: inverso ~160 ms; depois GONE + limpar filhos.
- Já visível (só atualizar texto): sem reanimar.
- Cancelar animação em curso se abrir/fechar rápido.
- Moldura com clip abaixo da barra: o deslize “sai” da barra e não desenha por cima dela.
- Overlay na raiz (fora do fluxo da coluna): não empurra o conteúdo da página.
