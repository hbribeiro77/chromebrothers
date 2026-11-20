# Seleção e exclusão em lote no Visualizador de mídia — Design

## Objetivo

Permitir marcar cartas no Visualizador e excluir os arquivos do aparelho em lote, sem usar toque longo (já reservado ao arraste fantasma).

## Decisões

- Entrada: botão **Selecionar** (injetado no Visualizador).
- Marcador no canto superior esquerdo de cada `.carta` só no modo seleção.
- Toque curto no modo seleção marca/desmarca; fora do modo, comportamento atual.
- Toque longo permanece arraste fantasma.
- Única ação da barra: **Excluir (N)** + sair/cancelar.
- Confirmação nativa antes de apagar.
- Escopo de arquivo: URIs `content://` / `file://` listados na pasta (inclui pasta virtual `chromebrothers://midia`).
- Após sucesso: remove cartas da grade e/ou reentrega listagem da pasta virtual.

## Ponte

- `chromebrothers.excluirArquivos(jsonUris)` — JSON array de strings; devolve `{ok, excluidos, falhas}`.
- App apaga via `ContentResolver.delete` (MediaStore) ou `File.delete` (`file://`).

## UI injetada

Script nativo (como miniaturas): barra Selecionar / Excluir / Cancelar; checkboxes nas `.carta`; usa `data-cb-uri` ou `src` da mídia.

## Fora de escopo

Copiar, enviar ao editor, guardar na biblioteca, selecionar tudo (pode vir depois).
