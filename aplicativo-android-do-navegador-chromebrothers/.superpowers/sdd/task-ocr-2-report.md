# Task 2 — Modal da extração de texto da imagem

**Status:** DONE_WITH_CONCERNS  
**Commits:** none  
**Verificação:** `:app:assembleDebug` — BUILD SUCCESSFUL in 13s (37 tasks; 12 executed). Avisos de `statusBarColor` / `navigationBarColor` são de outros arquivos, anteriores a esta tarefa.

## O que foi entregue

Modal só de interface. Não chama a API e não entra no menu da imagem.

- `ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador(activity, aoCopiar, aoEnviarAoChat)`
- `mostrarCarregando()` — ProgressBar + “Extraindo texto…”, esconde o texto e os botões Copiar / Enviar. Fechar continua visível.
- `mostrarResultado(texto)` e `mostrarErro(mensagem)` — escrevem no `TextView` selecionável e ligam Copiar / Enviar com `ExtracaoDeTextoDeImagemViaIaDoNavegador.botoesDeAcaoHabilitados`.
- `fechar()` — `dismiss`. Toque fora e voltar também fecham; o diálogo é recriado na próxima abertura.
- Largura 420 dp, `ColorDrawable` transparente, fundo `fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem`, botões transparentes com `backgroundTintList = null` e `stateListAnimator = null`, no mesmo jeito de `CopiaDeImagemDaPaginaParaABibliotecaDoNavegador`.

`SEM_TEXTO` e string em branco deixam Copiar / Enviar visíveis e desabilitados (alpha 0,4). Durante o carregamento esses dois botões ficam `GONE`.

## Arquivos

- Criado: `app/src/main/res/layout/layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml`
- Criado: `app/src/main/kotlin/com/chromebrothers/navegador/ModalDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt`
- Alterado: `app/src/main/res/values/strings_do_navegador_chromebrothers.xml`

Strings do brief: `extrair_texto_da_imagem`, `titulo_da_extracao_de_texto_da_imagem`, `extraindo_texto_da_imagem`, `copiar_texto_extraido_da_imagem`, `enviar_texto_extraido_ao_chat`, `texto_extraido_copiado`.

## Desvios em relação ao brief

1. String extra `fechar_modal_da_extracao_de_texto_da_imagem` = “Fechar”. O brief pede o botão e não traz o recurso. Não reaproveitei `fechar_lista_de_extensoes` para não amarrar este modal a outra tela.
2. Classe extra `RolagemComTetoDe320DpDoTextoExtraidoDaImagemDoNavegador` no mesmo arquivo Kotlin. O `ScrollView` mede o filho sem limite e se limita a 320 dp (`AT_MOST`), então texto curto não deixa um vão e texto longo rola. `maxHeight` no `TextView` cortaria a rolagem do pai. `textIsSelectable="true"` ficou no `TextView`.
3. Botão desabilitado usa alpha 0,4, igual ao botão de resumir do histórico do chat. O brief só pede habilitar via a regra.

## Auto-revisão

- Copiar e Enviar só disparam o callback se `botoesDeAcaoHabilitados(textoAtual)` for verdadeiro. O texto passado é o que está na tela, sem passar de novo por `textoParaExibir`.
- O modal não copia para a área de transferência e não mostra o toast `texto_extraido_copiado`. Isso fica com quem passar `aoCopiar` (Task 3).
- Copiar e Enviar não fecham o diálogo. Quem chama pode usar `fechar()`.
- `mostrarErro` usa a mesma regra dos botões. `FALHA_GENERICA` e `FALHA_MODELO_SEM_VISAO` não são `SEM_TEXTO`, então Copiar e Enviar ficam ligados e dá para copiar ou mandar a mensagem de erro ao chat. É o contrato do brief; a Task 3 pode querer outro texto, ou uma regra que desligue botão em falha.
- `extrair_texto_da_imagem` e `texto_extraido_copiado` ainda não têm uso na UI. Entram nesta tarefa porque o brief manda criá-las.
- Não há teste de UI. O diálogo depende de `AppCompatActivity`. A checagem foi o `assembleDebug`.
- O modal não observa o ciclo da activity. Se a activity morrer com ele aberto, o vazamento de janela é o mesmo dos outros `Dialog` do app. A Task 3 deve guardar a instância na activity e chamar `fechar()` se precisar.
- Enquanto o diálogo está aberto, `mostrarCarregando` / `mostrarResultado` / `mostrarErro` reaproveitam a mesma instância e reaplicam a largura de 420 dp.

## Correção — finding Important (`mostrarErro`)

`mostrarErro` não reaproveita mais o mesmo caminho de habilitação de `mostrarResultado`. A mensagem de falha continua visível e selecionável, mas Copiar e Enviar ficam visíveis, `isEnabled = false` e `alpha = 0.4`, mesmo se `botoesDeAcaoHabilitados` for verdadeiro (`FALHA_GENERICA`, `FALHA_MODELO_SEM_VISAO`). `acionarSeHabilitado` também recusa clique enquanto `erroEmExibicao` é verdadeiro, então o texto de erro não pode ser copiado nem enviado como OCR.

**Verificação:** `:app:assembleDebug` — BUILD SUCCESSFUL in 3s (37 tasks; 4 executed, 33 up-to-date). Sem commit.
