# Biblioteca de componentes — relatório

Opção A aplicada. Sem commit.

## Renomeação

Ferramentas: `listar_componentes_da_biblioteca`, `guardar_componente_da_colecao`, `abrir_componente_da_biblioteca`, `apagar_componente_da_biblioteca`.

Tipos: `BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador`, `ItemDaBibliotecaDeComponentesHtmlDaColecao` (`nome`, `descricao`, `tags`), `GravacaoDaBibliotecaDeComponentesHtmlDaColecaoNoArmazenamentoDoAplicativo`.

Pasta em disco continua `biblioteca-de-paginas-da-colecao`. O índice novo grava `nome`, `descricao`, `tags[]`; o HTML segue em `$id.html`.

## Modelo e migração

Limites: nome 120, descrição 500, até 8 tags de 40 caracteres, HTML 10 MB. Atualizar sem descrição/tags preserva as anteriores.

`LeituraEEscritaDoIndiceJsonDaBibliotecaDeComponentesHtml` lê `titulo` ou `nome` (nome vence) e o teste cobre a migração.

## Chat

- `ler_codigo_do_componente` (leitura): HTML; acima de 100 000 caracteres devolve o começo e `codigo truncado, total=N`.
- `capturar_componente` (leitura): carrega o componente na WebView da coleção se o id for outro, desenha o bitmap visível, grava `file://` e devolve imagem + relato (mesmo caminho de compactação da aba).
- `guardar_componente_da_colecao` aceita `descricao` e `tags` (array ou texto separado por vírgula).
- `publicar_pagina_da_colecao` continua só na sessão até guardar.
- Continuação passa a falar em componente (abrir, guardar, apagar, ler código, capturar).

## UI

Gaveta: **Componentes**. Cartão mostra nome, descrição e tags. Botão da coleção: **Guardar componente**.

## Verificação

`:regras-de-video-do-navegador:test` e `:app:assembleDebug` concluíram com sucesso. `adb install -r` do APK debug: Success.
