### Task 6: Serviço facade

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`

**Interfaces:**
- Consumes: `PedidoDeGeracao…`, `ClienteMcp…`, `Sessao…`, `Gravacao…`, bytes da Activity
- Produces:
  - `listarMcpsJson(): String`
  - `listarModelosJson(mcpId: String): String`
  - `definirReferencia(bytes, nome, mime)` / `limparReferencia()` / `temReferencia(): Boolean`
  - `gerarEmBackground(pedidoJson, aoAndamento: (String)->Unit, aoFim: (String)->Unit)` — thread; aoFim com JSON final `{ok, itens:[{idBiblioteca, previewDataUrl}], erro?}`

Preview: JPEG ≤320px largura, Base64 NO_WRAP, `data:image/jpeg;base64,…`.

Sem sessão → erro `GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO`.

Loop: `repeat(quantidade) { gerarImagem(...); guardar; add item }` — se falhar no meio, `ok:false` + itens parciais.

- [ ] **Step 1: Escrever classe**
- [ ] **Step 2: compileDebugKotlin**

---

