# Task 5 — UI: chips de origem, ⓘ, modal de detalhe, ações

## Status: DONE (build + install OK; sem teste visual no aparelho)

## Arquivos
- **Criado:** `app/src/main/res/layout/layout_modal_do_detalhe_da_midia_na_biblioteca.xml`
- **Alterado:** `app/src/main/res/layout/layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` (HorizontalScrollView `faixa_chips_origem_da_biblioteca` + LinearLayout `linha_chips_origem_da_biblioteca`, abaixo de Recentes/Favoritos, `gone` por padrão)
- **Alterado:** `app/src/main/res/values/strings_do_navegador_chromebrothers.xml` (15 strings novas do detalhe/chips/export)
- **Alterado:** `PainelDaGavetaDaBibliotecaDeVideos.kt`

## Implementação
1. **Chips**: `origemFiltroAtual = "todos"`. `desenharChipsDeOrigem()` (chamado em `atualizar()`) conta origens da sublista **não filtrada** (recentes/favoritos de vídeos ou imagens); mostra Todos + origens com count > 0 (Higgsfield, Sjinn, Topview, Página, Desconhecido). Selecionado: negrito, texto `#8AB4F8`, fundo `#2A3447`. Se o filtro ativo some da sublista, volta para "todos". Faixa oculta em Baixados/Páginas. `desenharLista()` aplica `FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos/filtrarImagens`.
2. **ⓘ**: `botaoDeInformacao()` (TextView "ⓘ" 40dp, contentDescription "Detalhe") antes da estrela em `cartao()` e `cartaoDeImagem()`. Click próprio — não chama `aoAbrir`; toque curto no cartão continua abrindo.
3. **Modal**: `Dialog(activity)` + layout inflado, janela transparente, 420dp, fundo `fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem` (#1A1D24). Campos: título, origem (+ " · modelo"), prompt (ScrollView com altura máx 220dp; "—" se vazio), thumbs de refs (64dp, decode em thread, resolve `filesDir/refs-de-geracao-da-biblioteca/<nome>` ou caminho absoluto; sem tap), caminho ("No aparelho: …encurtado" / `file://` / "Só link"). Botões: Fechar (#8A9099), Copiar prompt, Copiar caminho (oculto se não houver caminho), Baixar (#8AB4F8). Estilo sem tint / sem stateListAnimator.
4. **Baixar vídeo** (`mostrarModalDoDetalheDoVideo`): só visível se `caminhoLocal` existe como arquivo > 0 bytes. Chama `ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.exportar(activity, File, nome)`; sucesso → `biblioteca.marcarExportado(chave)` + `gravacao.gravar(biblioteca)` + Toast; falha → Toast de erro. Itens só-link (sem arquivo local) → Baixar oculto.
5. **Baixar imagem** (`mostrarModalDoDetalheDaImagem`): reutiliza `baixar(item)` (agora retorna `Boolean`; callers antigos ignoram o retorno). Dentro de `baixar` adicionei `marcarExportado` + `gravar` se ainda não exportado. Arquivo local = `gravacaoDeImagens.arquivo(nomeDoArquivo)` ou `caminhoLocal`.
6. Se já exportado / `naGaleria`: botão exibe "Já na pasta pública" mas permite re-exportar (duplica o arquivo no MediaStore — conforme brief "re-export OK").

## Restrições
- **Zero AlertDialog novo**: os 3 usos remanescentes (linhas ~1286, 1309, 2740) + import já existiam (renomear/excluir download, renomear imagem).
- Sem Fase 2 drag; sem commits (workspace nem é repo git).

## Build / install
- `gradle :app:assembleDebug` → **BUILD SUCCESSFUL** (só warnings antigos de deprecação).
- `adb install -r app-debug.apk` → **Success** (device RX2T7007XMD).
- Testes unitários não rodados nesta task (nenhuma regra JVM alterada; a lógica nova é UI).

## Concerns
- Sem verificação visual no aparelho (chips, modal, scroll do prompt, thumbs) — recomendada checagem manual.
- Em telas < 420dp de largura, `setLayout(420dp)` segue o padrão do `mostrarCaixaDoEditor` (pode estourar em telas estreitas; a gaveta é landscape/tablet pelo layout).
- Linha de botões do modal está num HorizontalScrollView para caber 4 botões em 420dp; pode precisar rolar horizontalmente com textos longos.
- Re-exportar vídeo/imagem cria cópia duplicada em Downloads/Pictures.
- Chips: o filtro de origem é compartilhado entre Vídeos e Imagens (reset automático se a origem não existir na outra aba).
