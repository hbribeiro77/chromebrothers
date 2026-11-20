# Fase 2 Task 4 — Drop na coleção MCP → adicionarReferencia

**Status:** implementado (sem commits, sem AlertDialog). Não testado em aparelho.

## Exploração
- Geradores identificados pelo id fixo do componente em `PainelDaColecaoFlutuanteDoChatDoNavegador.idOrigemDaBiblioteca`
  (`geracao-mcp-imagem` / `geracao-mcp-video`, semeados na Activity).
- Classe da Task 3: `EstadoDoArrasteDaBiblioteca(tipo, caminhoLocal, id)` (arquivo `EstadoDoArrasteDaBibliotecaDaGaveta.kt`), usada como `localState` do `startDragAndDrop` (flags GLOBAL + GLOBAL_URI_READ).
- Não havia nenhum `OnDragListener` no app; o lift-drag fantasma coleção→editor é baseado em toque (`pontoSobreAlgumaJanelaDaColecao`), não em `DragEvent`, então não conflita.

## Arquivos
- NOVO `regras-de-video-do-navegador/.../DestinoDoArrasteDaBibliotecaParaReferenciaMcp.kt` — `decidir(tipoArraste, mime, tipoComponente)` (USAR_ARQUIVO / USAR_FRAME / RECUSAR) + `tipoDoComponentePeloId(id)`.
- NOVO `.../TesteDoDestinoDoArrasteDaBibliotecaParaReferenciaMcp.kt` — 8 testes (TDD: vermelho confirmado, depois verde).
- `PainelDaColecaoFlutuanteDoChatDoNavegador.kt` — `OnDragListener` em `painel` e `webDaPagina`; só age se a janela mostra um gerador MCP e o arraste é da biblioteca (localState ou clip image/* | video/*). STARTED/ENTERED/LOCATION/EXITED → true; DROP → delega à Activity. Caso contrário retorna false (WebView segue o fluxo normal).
- `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt` — `ligarSoltarItemDaBibliotecaParaReferenciaMcp` (propaga às janelas existentes e novas).
- `AtividadePrincipalDoNavegadorChromebrothers.kt` — `soltarItemDaBibliotecaComoReferenciaDaGeracao`: decide a ação; imagem → bytes do arquivo local (ou URI, limite 20 MB); vídeo → `ExtracaoDeQuadroJpegDeVideoLocalParaArraste.extrairParaArquivo` (fallback: copia a URI p/ cache), Toast "Usei um frame do vídeo como referência"; depois `definirReferenciaDaGeracaoEMontarJson` (→ `adicionarReferencia`) + `painel.entregarImagemDeReferenciaDaGeracao(json)`. I/O em thread; temporários apagados; retorna true.

## Verificação
- `:regras-de-video-do-navegador:test --tests "*DestinoDoArraste*"` → BUILD SUCCESSFUL.
- Suíte completa `:regras-de-video-do-navegador:test` + `:app:compileDebugKotlin` → BUILD SUCCESSFUL.

## Ressalvas
- Sem teste instrumentado/manual: comportamento real do DragEvent sobre o WebView (Chromium pode consumir o evento em algumas versões) precisa ser validado no aparelho.
- Sem destaque visual de "alvo de soltar" (não pedido).
- Limite de 4 referências é tratado por `adicionarReferencia`, erro volta via `entregarImagemDeReferenciaDaGeracao`.
- Arraste de fora do app (clip image/*|video/* sem localState) também é aceito via URI.
