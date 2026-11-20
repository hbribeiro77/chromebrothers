# Coleção só WebView + HTML 10 MB

Status: concluído. Sem commit.

## Limites

- `PublicacaoDaPaginaDaColecaoFlutuanteDoChat.LIMITE_DO_HTML` = `10 * 1024 * 1024`
- `BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.LIMITE_DO_HTML` = o mesmo
- Título continua ≤ 120
- `LIMITE_DE_ITENS` e a recusa de quantidade em `guardar` foram removidos

## Removido das ferramentas

- `montar_colecao`, `organizar_colecao`, `tirar_da_colecao`, `abrir_modo_da_colecao`
- Pedidos `MontarColecao`, `OrganizarColecao`, `TirarDaColecao`, `AbrirModoDaColecao`
- `ModoDaColecaoFlutuante`, `lerModo`, `mensagemDoModo`
- Heurísticas de montar/organizar/tirar na continuação; pedido de coleção de vídeos aponta para `publicar_pagina_da_colecao`
- Parâmetros do chat: `colocarNaColecao`, `organizarColecao`, `tirarDaColecao`, `abrirModoDaColecao`

## UI

- Coleção abre sempre a WebView
- Botões Mídia e Página ficam `gone`
- Lista de miniaturas fica `gone`
- Guardar, PDF, bolha, carga de HTML e interceptação `file://` permanecem
- `temConteudo` depende só do HTML
- Botão Coleção do toque longo (vídeo e imagem) fica `gone`

## Verificação

- `:regras-de-video-do-navegador:test` — 79 suítes, 440 testes, 0 falhas, 0 erros
- `:app:assembleDebug` — sucesso
- `adb install -r app-debug.apk` — Success

## Ressalvas

- `colocar`, `organizar`, `tirar` e o desenho de miniaturas continuam no painel, sem ligação no chat e com a lista oculta. O toque longo ainda tem o método, mas o botão não aparece.
- `MontagemDaColecaoFlutuanteDoChat` permanece para o toque longo e para os testes do parser.
- A instalação no aparelho foi confirmada; a WebView da coleção não foi exercitada na tela.
