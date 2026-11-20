# Task F2-3 — Long-press na gaveta → startDragAndDrop

**Status:** DONE (sem commits; sem listener de drop/MCP).
**Compile:** `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin` → BUILD SUCCESSFUL (só warnings antigos).

## Arquivos
- NOVO `app/src/main/kotlin/com/chromebrothers/navegador/EstadoDoArrasteDaBibliotecaDaGaveta.kt` — `class EstadoDoArrasteDaBiblioteca(tipo, caminhoLocal, id)`.
- `PainelDaGavetaDaBibliotecaDeVideos.kt`:
  - `cartao(item)` e `cartaoDeImagem(item)`: `setOnLongClickListener` → `iniciarArrasteDoVideoDaBiblioteca` / `iniciarArrasteDaImagemDaBiblioteca`. Click de abrir e ⓘ/estrela/remover intactos (botões filhos consomem o próprio toque).
  - Helper comum `iniciarArrasteDeArquivoLocalDaBiblioteca`: resolve arquivo → `PreparacaoDeUriContentParaArrasteDaBiblioteca.deArquivoLocal` → `ClipData(ClipDescription(nome, [mime]), Item(uri))` → shadow → `startDragAndDrop(clip, shadow, EstadoDoArrasteDaBiblioteca, DRAG_FLAG_GLOBAL or DRAG_FLAG_GLOBAL_URI_READ)`; háptico LONG_PRESS se iniciou.
  - Vídeo: arquivo = `caminhoLocal` ou `file://` de `enderecoParaAbrir`; mime forçado `video/mp4`. Imagem: `gravacaoDeImagens.arquivo(nomeDoArquivo)` ou `caminhoLocal`.
  - Sem arquivo local / URI nula → Toast `arraste_da_biblioteca_so_link_baixe_ou_exporte_antes`.
  - Shadow: `DragShadowBuilder(ImageView da miniatura)` se tiver drawable; senão o próprio cartão.
- `strings_do_navegador_chromebrothers.xml`: nova string pt-BR do Toast.

## Não feito (conforme brief)
- `ExtracaoDeQuadroJpegDeVideoLocalParaArraste` não chamado (Task 4).
- Sem OnDragListener/MCP. Sem AlertDialog.

## Concerns
- Sem teste automatizado (UI/Android); não testado em aparelho.
- `file_paths` só cobre `files/biblioteca-de-imagens-capturadas`, `files/biblioteca-de-videos-higgsfield`, etc. Vídeo com `caminhoLocal` fora dessas pastas → `FileProvider` lança → Toast "Só link" (pode confundir).
- Shadow da miniatura usa o tamanho do ImageView (alpha não aplicado, API padrão).
- `startDragAndDrop` pode colidir com scroll vertical da lista em alguns aparelhos; long-click só dispara sem movimento, então esperado OK.
- Cartões de download/página não receberam drag (fora do escopo).
