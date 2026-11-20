### Task 7: Ponte JS + pickers + seed

**Files:**
- Modify: `app/.../PainelDaColecaoFlutuanteDoChatDoNavegador.kt` (`PonteJavascriptDaPaginaDaColecao`)
- Modify: `app/.../AtividadePrincipalDoNavegadorChromebrothers.kt` (wire serviço, seed, Activity Result galeria, diálogo lista biblioteca)

**Interfaces:**
- Ponte (todas retornam String JSON):
  - `listarMcpsGeracao()`
  - `listarModelosGeracao(mcpId: String?)`
  - `escolherImagemGeracao(origem: String?)` — `"galeria"` abre GetContent; `"biblioteca"` lista ids/nomes da biblioteca de imagens e grava bytes no serviço; retorno imediato `{ok:true,aguardando:true}` **ou** se síncrono após escolha via callback + `evaluateJavascript` notificando o HTML (`window.__chromebrothersImagemGeracao`)
  - `limparImagemGeracao()`
  - `gerarImagensMcp(pedidoJson: String?)` → valida; se ok inicia `gerarEmBackground`; retorna `RespostaJsonDaPonteDeGeracaoMcp.okAndamento()`; no fim `webDaPagina.evaluateJavascript("window.__chromebrothersGeracaoMcp && window.__chromebrothersGeracaoMcp("+JSONObject.quote(json)+")", null)`

**Picker galeria:** `ActivityResultContracts.GetContent()` `"image/*"` → bytes → `servico.definirReferencia` → JS callback com preview data-URL.

**Picker biblioteca:** dialog/lista dos itens de `GravacaoDaBibliotecaDeImagensCapturadas…` → ler arquivo → mesma definição de referência.

**Seed:** após `bibliotecaDeComponentesDaColecao = carregar()`, se `buscar("geracao-mcp-imagem") == null`, `guardar(nome=…, html=Html….html(), idExistente=null, …, novoId={ "geracao-mcp-imagem" })` — **forçar id** (ajustar `guardar` para aceitar id fixo **ou** inserir item direto e `gravar`). Preferência: se `novoId` pode retornar `geracao-mcp-imagem`, usar isso na criação.

- [ ] **Step 1:** Seed idempotente na Activity
- [ ] **Step 2:** Métodos da ponte + callbacks UI
- [ ] **Step 3:** HTML do componente trata `__chromebrothersImagemGeracao` para preview da ref
- [ ] **Step 4:** `assembleDebug` + `adb install -r`

---

