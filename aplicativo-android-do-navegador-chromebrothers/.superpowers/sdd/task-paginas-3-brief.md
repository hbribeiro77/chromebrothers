### Task 3: Coleção — idOrigem + guardar + carregar

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: layout da coleção + `strings.xml`

**Produces:**
- `var idOrigemDaBiblioteca: String?`
- `fun carregarPaginaDaBiblioteca(item: Item…): String` — set titulo/html, modo Página, idOrigem=item.id, mostrar
- `fun guardarNaBiblioteca(biblioteca, gravacao, agora, novoId): String` — usa htmlDaPagina + titulo; sanitizar; guardar; gravar disco; toast via retorno
- Botão Guardar; limpar idOrigem ao limpar coleção; ao `publicarPagina` **não** limpar idOrigem (continua “este id” até guardar/abrir outro/limpar)

- [ ] **Step 1:** UI + métodos
- [ ] **Step 2:** Compile

---

