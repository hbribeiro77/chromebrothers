# Task OCR-5 — Extrair texto no menu de toque longo da imagem da página

**Status:** DONE (sem commit).

## Alterações
- `layout_caixa_de_opcoes_da_imagem_tocada_na_pagina_do_navegador.xml`: botão `botao_extrair_texto_da_imagem_da_pagina_no_toque_longo` (`@string/extrair_texto_da_imagem`, `gone` por padrão) antes de Cancelar.
- `CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt`: enum ganhou `EXTRAIR_TEXTO`; construtor ganhou `temChave: () -> Boolean` (3º parâmetro); em `perguntar` a visibilidade usa `ExtracaoDeTextoDeImagemViaIaDoNavegador.mostrarAcao(temChave())` e o clique chama `escolher(EXTRAIR_TEXTO)` (dismiss + baixar).
- `AtividadePrincipalDoNavegadorChromebrothers.kt`: `cofreDaChaveDaOpenRouter` (lazy, compartilhado com o painel do chat); `orquestradorDaExtracaoDeTextoDeImagem` (lazy; modelo via `preferencias.modeloDoChat`, `painelDoChat.abrirComTextoNoCampo`); `receberImagemDaPagina` despacha `EXTRAIR_TEXTO` → `extrairDeBytes(bytes)`.

## Verificação
`gradle :app:assembleDebug` (gradle-8.11.1 em %LOCALAPPDATA%) → BUILD SUCCESSFUL. Sem testes novos (só wiring de UI).

## Observações
- Menu da biblioteca (Task 6) não tocado.
- Lazy evita problema de ordem de criação do `painelDoChat`; o botão fica oculto sem chave.
