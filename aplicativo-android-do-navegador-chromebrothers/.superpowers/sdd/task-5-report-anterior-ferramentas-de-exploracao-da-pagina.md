# Task 5 — Registrar ferramentas de exploração da página (relatório)

**Status:** DONE

## Alterações
- `FerramentasDeLeituraDoChatDaOpenRouter.kt`
  - Constantes `TEXTO_PAGINADO="ler_texto_da_pagina"`, `ELEMENTOS="consultar_elementos"`, `ROLAR="rolar_pagina"`; incluídas em `permitidas`.
  - `json()`: três novas definições via `definicaoComProps` (helper privado, mesmo estilo de `AcoesConfirmadasDoChatDaOpenRouter.definicao`).
    - `ler_texto_da_pagina`: `offset`, `limite` (integer), sem obrigatórios.
    - `consultar_elementos`: `seletor` (string, obrigatório), `offset`, `limite`.
    - `rolar_pagina`: `seletor`, `y`, `paginas`, `pixels`, sem obrigatórios.
  - Descrição de `ler_texto_visivel` aponta para as novas ferramentas.
  - `instrucao()`: bloco sobre página grande / lazy-load / não afirmar que não vê tudo sem tentar.
  - `rotuloVisivel` e `rotuloEmAndamento` cobrem as três.
- Novo teste: `TesteDasFerramentasDeExploracaoDaPaginaNoChatDaOpenRouter.kt` (6 testes, kotlin.test).

## Verificação
- RED: teste não compilava antes da implementação (constantes inexistentes).
- GREEN: classe nova 6/6 passa; `:regras-de-video-do-navegador:test` completo BUILD SUCCESSFUL.

## Pendências / observações
- Só o registro do schema: ainda é preciso despachar as três ferramentas (execução no WebView) onde `permitidas` é consumida; sem isso, a chamada não terá handler.
- Sem commits (sem git).
