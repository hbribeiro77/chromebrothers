# Nova composição com camada inicial selecionada

## Problema

Ao tocar em **Nova composição**, o editor abre moldura vazia sem camadas. O traço só pinta na camada ativa (`Folha`/`Imagem`), então o primeiro desenho não funciona até o usuário criar camada manualmente.

## Decisão

Depois de `definirMolduraVazia()`, chamar `criarCamadaVazia()` (mesma folha do botão **Nova camada**), com `idDaCamadaAtiva` apontando para ela. Sem marcar peça selecionada no quadro.

## Fora de escopo

- Auto-criar camada no primeiro traço
- Mudar o contrato de `definirMolduraVazia()` para outros fluxos futuros
