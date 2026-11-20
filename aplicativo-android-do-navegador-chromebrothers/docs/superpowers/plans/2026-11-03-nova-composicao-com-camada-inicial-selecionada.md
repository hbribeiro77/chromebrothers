# Plano: Nova composição com camada inicial

**Goal:** Abrir Nova composição já com uma `Folha` ativa para desenhar de imediato.

## Arquivos

- `regras/.../BarraContextualDoEditorDeImagemEDestinoDoToqueNaGaveta.kt` — regra `AbrirComposicaoVaziaDaEdicao`
- `regras/.../TesteDaBarraContextualDoEditorEDoToqueNaImagemDaGaveta.kt` — teste
- `PainelDaGavetaDaBibliotecaDeVideos.kt` — `abrirComposicaoVazia()` chama `criarCamadaVazia()` se a regra pedir

## Tarefas

1. Teste: `AbrirComposicaoVaziaDaEdicao.criaCamadaInicialSelecionada()` é `true`
2. Implementar a regra
3. Em `abrirComposicaoVazia`, após `definirMolduraVazia()`, se a regra, `editor.criarCamadaVazia()` + `atualizarListaDeCamadas()`
4. assembleDebug + adb install
