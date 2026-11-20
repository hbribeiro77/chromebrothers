### Task 5: UI — chips, ⓘ, modal, ações

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

**Files:**
- Create: `app/src/main/res/layout/layout_modal_do_detalhe_da_midia_na_biblioteca.xml`
- Modify: `app/src/main/res/layout/layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `app/src/main/res/values/strings.xml` (and ids as needed)

**HARD RULE:** Zero `AlertDialog` in this flow. Use `Dialog(activity)` + Chromebrothers look like `mostrarCaixaDoEditor` (transparent window, 420dp width, fundo `#1A1D24` via `@drawable/fundo_arredondado_da_caixa_de_dialogo_do_editor_de_imagem`).

## 1. Chips de origem

In gaveta layout, under Histórico/Favoritos row (visible for Vídeos and Imagens sections): add `HorizontalScrollView` id `faixa_chips_origem_da_biblioteca` containing a `LinearLayout` id `linha_chips_origem_da_biblioteca` (horizontal).

In Painel:
- `private var origemFiltroAtual = "todos"`
- When rendering Vídeos recentes/favoritos: take list then `FiltroPorOrigemNaBibliotecaDeMidia.filtrarVideos(lista, origemFiltroAtual)`
- Same for Imagens with `filtrarImagens`
- Populate chips: Todos, Higgsfield, Sjinn, Topview, Página, Desconhecido — use `OrigemDaMidiaNaBibliotecaDoNavegador` consts + rotulo; "todos" for Todos
- Prefer showing only chips that have count > 0 in the **unfiltered** current sublist, always including Todos
- Chip click sets `origemFiltroAtual` and refreshes list; selected chip visually distinct (text color / bold)
- Hide chip row for Baixados/Páginas sections

## 2. ⓘ on cards

In `cartao(item: ItemDaBibliotecaDeVideos)` and `cartaoDeImagem(...)`: add ImageButton/TextView "ⓘ" (or info icon if exists) before/near favorite star.
- contentDescription e.g. "Detalhe"
- click → `mostrarModalDoDetalheDoVideo(item)` / `mostrarModalDoDetalheDaImagem(item)`
- Must NOT trigger card open (stop propagation: don't call aoAbrir)
- Card short click still opens media

## 3. Modal layout

`layout_modal_do_detalhe_da_midia_na_biblioteca.xml`:
- Root vertical LinearLayout with dialog background drawable
- titulo TextView "Detalhe"
- origemModelo TextView
- ScrollView with prompt TextView (full prompt, or "—" if blank)
- HorizontalScrollView `linha_refs_do_detalhe` for ref thumbs (ImageViews ~64dp)
- caminho TextView (path summary or "Só link" / "No aparelho …")
- Row of buttons: Fechar, Copiar prompt, Copiar caminho, Baixar
- Style buttons like editor dialog (transparent bg, no stateListAnimator, colors #8A9099 cancel / #8AB4F8 action)

## 4. Modal behavior

`mostrarModalDoDetalhe…`:
```kotlin
val dialogo = Dialog(activity)
val raiz = activity.layoutInflater.inflate(R.layout.layout_modal_do_detalhe_da_midia_na_biblioteca, null)
// fill fields from item
dialogo.setContentView(raiz)
dialogo.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
dialogo.show()
val largura = (420 * density).toInt()
dialogo.window?.setLayout(largura, WindowManager.LayoutParams.WRAP_CONTENT)
```

- Origem: `OrigemDaMidiaNaBibliotecaDoNavegador.rotulo(item.origem)` + modelo if not blank
- Prompt: `item.prompt.ifBlank { item.titulo }`
- Refs: resolve files under `filesDir/refs-de-geracao-da-biblioteca/<nome>` OR absolute if path; decode bitmap thumbs; tap optional open if easy else skip
- Caminho: if `item.caminhoLocal` non-blank and File exists → show shortened path + "No aparelho"; else if endereco file:// → that; else "Só link"
- Copiar prompt / Copiar caminho → ClipboardManager; Toast short OK
- **Baixar vídeo:** only visible/enabled if local file exists; call `ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.exportar(activity, File(caminho), nome)`; on success `biblioteca.marcarExportado` + gravar + Toast; on fail Toast erro
- **Baixar imagem:** reuse existing `baixar(item)` which exports Pictures/ChromeBrothers; also marcarExportado if not already via naGaleria
- If already exportado/naGaleria: button text "Já na pasta pública" still allows re-export OR disable — pick re-export OK
- Remote-only page items: hide/disable Baixar

## 5. Persist after marcarExportado

After marcarExportado on video, call `gravacao.gravar(biblioteca)` (same pattern as favoritar).

## 6. Build & install

```
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

Grep your changes: no new AlertDialog.

**NO git commits. Do not implement Fase 2 drag.**

Depends on Tasks 1–4 done: FiltroPorOrigem, Item fields, JSON, ExportacaoDeVideo…, OrigemDaMidia…
