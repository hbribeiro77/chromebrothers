# Task F2 – Fantasma "+" e clipboard no arraste da gaveta

## O que mudou
- `AtividadePrincipal`: campos separados `fantasmaDoArrasteDaBiblioteca`, `maisDoFantasmaDaBiblioteca`, `estadoDoArrasteDaBibliotecaAtivo`, `bitmapDoFantasmaDaBiblioteca`; funções `iniciarArrasteVisualDaBiblioteca`, `atualizarArrasteVisualDaBiblioteca` (+ verde sobre gerador MCP, amarelo caso contrário), `soltarArrasteVisualDaBiblioteca`, `encerrarFantasmaDaBiblioteca`; `dispatchTouchEvent` consome toques enquanto o arraste está ativo (envia um ACTION_CANCEL ao toque pendente da gaveta uma vez).
- Drop refatorado: `soltarItemDaBibliotecaComoReferenciaDaGeracaoNucleo(painel, estado, tipoDoGerador, uri?, mime, permissao?)`; a versão com `DragEvent` chama o mesmo núcleo.
- `GestorDasJanelasFlutuantes`: `painelSobPonto` e `tipoGeradorMcpSobPonto`.
- `PainelDaGaveta`: `aoIniciarArrasteDaBiblioteca` (lambda ligada pela Activity); `iniciarArrasteDeArquivoLocalDaBiblioteca` agora copia imagem (ou frame JPEG do vídeo) para o clipboard, monta o bitmap do fantasma (miniatura ou decode) e chama o fantasma. `startDragAndDrop` removido. Sem Toast extra (haptic basta).
- Sem AlertDialog novo, sem commits; ⓘ e toque curto intocados; fantasma coleção→editor intocado.

## Verificação
- `:app:compileDebugKotlin` OK, `:app:assembleDebug` OK, `adb install -r` Success (RX2T7007XMD).
- Não há testes unitários cobrindo esses arquivos; teste manual no aparelho pendente.
