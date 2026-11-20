### Task 4: Seção Páginas na gaveta

**Files:**
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- strings

**Behavior:**
- Botão seção Páginas
- Lista itens (titulo + data); clique → callback `aoAbrirPagina(item)` → Activity carrega na coleção e mostra flutuante
- Long-press ou botão apagar → apaga biblioteca+disco; se idOrigem == id, `painelDaColecao.limparVinculoBiblioteca()` only

- [ ] **Step 1:** UI + wire
- [ ] **Step 2:** Compile

---

