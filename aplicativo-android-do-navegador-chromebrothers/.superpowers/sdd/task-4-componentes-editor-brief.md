### Task 4: Botão Componentes no chrome do editor

**Files:**
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml` (`barra_de_acoes_da_imagem_ampliada`)
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (se o abrir componente ficar na Activity)

- [ ] **Step 1: Layout**

`ImageButton` ou `Button` `@+id/botao_componentes_no_chrome_do_editor_de_imagem` na barra de ações da imagem ampliada, `contentDescription` / texto `componentes_no_chrome_do_editor_de_imagem`, visível no chrome de edição (junto de `mostrarChromeDeEdicao` / `GONE` no chrome de visita).

- [ ] **Step 2: Click → abrir componente**

Callback `aoAbrirComponentesDoEditor: (() -> Unit)?` no Painel, wired na Activity para:
1. Listar biblioteca (mesmo store da gaveta/overflow).
2. Mostrar seletor (preferir dialog/caixa **custom** do app, não `AlertDialog` do sistema — padrão já usado no overflow de componentes).
3. Ao escolher item: `painelDaColecao.carregarComponenteDaBiblioteca(item)` **sem** `painelDaBibliotecaDeVideos.esconder()` (diferente do `aoAbrirPagina` da gaveta, que esconde a biblioteca).

- [ ] **Step 3: Visibilidade**

Só mostrar o botão quando `editandoImagem` (chrome de edição).

---

