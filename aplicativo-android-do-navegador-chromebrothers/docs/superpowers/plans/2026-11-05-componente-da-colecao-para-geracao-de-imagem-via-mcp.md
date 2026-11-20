# Componente da coleção — geração de imagem via MCP

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Componente HTML na coleção flutuante para gerar imagens via MCP (v1 Higgsfield): prompt, referência opcional (galeria/biblioteca), modelo da lista, quantidade 1–4, preview no componente + gravação na biblioteca — sem chat/LLM e sem confirmação extra.

**Architecture:** HTML minimalista + ponte `chromebrothers` → facade nativa que reusa `ClienteMcpDoHiggsfield…` / sessão / gravação. Geração é **assíncrona**: a ponte valida e devolve `{ok, andamento:true}`; ao terminar injeta `window.__chromebrothersGeracaoMcp(json)`.

**Tech Stack:** Kotlin, OkHttp/MCP Higgsfield existente, WebView + `@JavascriptInterface`, módulo `regras-de-video-do-navegador` (TDD), Gradle 8.11.1 / JDK 21.

**Spec:** `docs/superpowers/specs/2026-11-05-componente-da-colecao-para-geracao-de-imagem-via-mcp-design.md`

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- TDD nas regras antes do código de produção correspondente
- Após mudanças no app: testes afetados + `assembleDebug` (+ `adb install -r` quando validar no tablet)
- Sem commits a menos que o usuário peça
- Windows PowerShell; Gradle: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" … -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"` com `JAVA_HOME` Adoptium 21
- YAGNI: só imagem; só Higgsfield na lista de MCPs; sem params extras por modelo; sem confirmação de crédito
- Id estável do componente: `geracao-mcp-imagem`; seed **não** sobrescreve HTML existente
- PUT de referência mantém `If-None-Match: *`

## File map

| Arquivo | Papel |
|---------|--------|
| `regras-…/PedidoDeGeracaoDeImagemViaMcpNaColecao.kt` | Parse/validação do `pedidoJson` + JSON helpers de resposta |
| `regras-…/ListagemDeModelosDoHiggsfieldNoChat.kt` | Expor `itensDoCatalogo` (id/nome) para JSON da ponte |
| `regras-…/HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt` | String HTML empacotada + constantes id/nome/tags |
| `app/…/ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt` | Facade: listar MCPs/modelos, segurar bytes da ref, gerar N, gravar, preview data-URL |
| `app/…/ClienteMcpDoHiggsfield…kt` | `count` em `params`; extrair várias URLs se vierem |
| `app/…/GravacaoDaMidiaGeradaPeloHiggsfield…kt` | `guardarImagemComId` → id + caminho |
| `app/…/PainelDaColecaoFlutuante…kt` | Métodos na `PonteJavascriptDaPaginaDaColecao` + pickers |
| `app/…/AtividadePrincipal…kt` | Wire do serviço + seed idempotente + pickers galeria/biblioteca |
| Testes em `regras-…/src/test/…` | Pedido, HTML seed, itens do catálogo |

---

### Task 1: Regras — parse do pedido e JSON de resposta

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/PedidoDeGeracaoDeImagemViaMcpNaColecao.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt`

**Interfaces:**
- Produces: `PedidoDeGeracaoDeImagemViaMcpNaColecao` (data), `ResultadoDoPedidoDeGeracaoDeImagemViaMcp` (Ok/Recusado), `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(json)`, `RespostaJsonDaPonteDeGeracaoMcp.ok/erro/itens…`

- [ ] **Step 1: Teste falhando**

```kotlin
@Test
fun exigePromptEQuantidadeEntre1e4() {
    assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
        PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"","quantidade":1}"""),
    )
    assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(
        PedidoDeGeracaoDeImagemViaMcpNaColecao.ler("""{"mcp":"higgsfield","prompt":"x","quantidade":5}"""),
    )
    val ok = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
        """{"mcp":"higgsfield","prompt":"gato","modelo":"nano_banana_2_1","quantidade":2,"temReferencia":true}""",
    )
    assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok>(ok)
    assertEquals("higgsfield", ok.pedido.mcp)
    assertEquals(2, ok.pedido.quantidade)
    assertTrue(ok.pedido.temReferencia)
}

@Test
fun recusaMcpDesconhecido() {
    val r = PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(
        """{"mcp":"outro","prompt":"x","quantidade":1}""",
    )
    assertIs<ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado>(r)
}
```

- [ ] **Step 2: Rodar teste — esperado FAIL**

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao -p "D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers"
```

- [ ] **Step 3: Implementação**

```kotlin
// PedidoDeGeracaoDeImagemViaMcpNaColecao.kt
data class PedidoDeGeracaoDeImagemViaMcpNaColecao(
    val mcp: String,
    val prompt: String,
    val modelo: String,
    val quantidade: Int,
    val temReferencia: Boolean,
)

sealed class ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
    data class Ok(val pedido: PedidoDeGeracaoDeImagemViaMcpNaColecao) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
    data class Recusado(val mensagem: String) : ResultadoDoPedidoDeGeracaoDeImagemViaMcp()
}

object PedidoDeGeracaoDeImagemViaMcpNaColecao {
    const val MCP_HIGGSFIELD = "higgsfield"
    val MCPS_CONHECIDOS = setOf(MCP_HIGGSFIELD)

    fun ler(json: String): ResultadoDoPedidoDeGeracaoDeImagemViaMcp {
        val mcp = TextoJsonDoChat.valorDaChave(json, "mcp")?.trim()?.lowercase().orEmpty().ifBlank { MCP_HIGGSFIELD }
        val prompt = TextoJsonDoChat.valorDaChave(json, "prompt")?.trim().orEmpty()
        val modelo = TextoJsonDoChat.valorDaChave(json, "modelo")?.trim().orEmpty()
        val q = TextoJsonDoChat.inteiroDaChave(json, "quantidade") ?: 1
        val temRef = TextoJsonDoChat.valorDaChave(json, "temReferencia")?.equals("true", true) == true
            || json.contains("\"temReferencia\":true")
        return when {
            prompt.isEmpty() -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("Faltou o prompt.")
            prompt.length > GeracaoDeImagemEVideoPeloHiggsfieldNoChat.LIMITE_DO_PROMPT ->
                ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("O prompt passou do limite.")
            mcp !in MCPS_CONHECIDOS -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("MCP não suportado: $mcp")
            q !in 1..4 -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Recusado("A quantidade deve ser entre 1 e 4.")
            else -> ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok(
                PedidoDeGeracaoDeImagemViaMcpNaColecao(mcp, prompt, modelo, q, temRef),
            )
        }
    }
}

object RespostaJsonDaPonteDeGeracaoMcp {
    fun erro(mensagem: String) =
        """{"ok":false,"erro":"${TextoJsonDoChat.escapar(mensagem)}"}"""
    fun okAndamento() = """{"ok":true,"andamento":true}"""
    fun okMcps() = """{"ok":true,"itens":[{"id":"higgsfield","nome":"Higgsfield"}]}"""
    fun okLimpar() = """{"ok":true}"""
}
```

- [ ] **Step 4: Rodar testes — PASS**

---

### Task 2: Catálogo — itens JSON para a ponte

**Files:**
- Modify: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ListagemDeModelosDoHiggsfieldNoChat.kt`
- Modify: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaListagemDeModelosDoHiggsfieldNoChat.kt`

**Interfaces:**
- Consumes: `extrairItens` (tornar útil via API pública)
- Produces: `ListagemDeModelosDoHiggsfieldNoChat.itensParaSelect(bruto): List<Pair<id,nome>>` e `jsonDosItensParaPonte(bruto): String`

- [ ] **Step 1: Teste**

```kotlin
@Test
fun jsonDosItensParaPonteExtraiIdENome() {
    val bruto = """{"items":[{"id":"soul_2","name":"Soul","output_type":"image"}]}"""
    val json = ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte(bruto)
    assertTrue(json.contains("\"ok\":true"))
    assertTrue(json.contains("soul_2"))
    assertTrue(json.contains("Soul"))
}
```

- [ ] **Step 2: Implementar** — reusar lógica de `extrairItens`; montar `{"ok":true,"itens":[{"id":"…","nome":"…"},…]}` com `TextoJsonDoChat.escapar`.

- [ ] **Step 3: PASS** nos testes de listagem.

---

### Task 3: HTML empacotado do componente

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt`

**Interfaces:**
- Produces: `HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID`, `NOME`, `TAGS`, `html()`

- [ ] **Step 1: Teste**

```kotlin
@Test
fun htmlTemFormularioEChamaPonte() {
    assertEquals("geracao-mcp-imagem", HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID)
    val h = HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.html()
    assertTrue(h.contains("chromebrothers.listarMcpsGeracao"))
    assertTrue(h.contains("chromebrothers.listarModelosGeracao"))
    assertTrue(h.contains("chromebrothers.escolherImagemGeracao"))
    assertTrue(h.contains("chromebrothers.gerarImagensMcp"))
    assertTrue(h.contains("__chromebrothersGeracaoMcp"))
    assertTrue(h.length < 200_000)
}
```

- [ ] **Step 2: HTML mínimo** (uma coluna; selects MCP/modelo; textarea; botões Galeria/Biblioteca/Limpar; number 1–4; Gerar; `#status`; `#grade`):

```javascript
window.__chromebrothersGeracaoMcp = function (json) {
  // parse; se ok, append imgs com previewDataUrl; senão mostra erro; reabilita Gerar
};
async function gerar() {
  const pedido = JSON.stringify({
    mcp: selMcp.value, prompt: ta.value, modelo: selModelo.value,
    quantidade: +qtd.value, temReferencia: !!temRef
  });
  const r = JSON.parse(chromebrothers.gerarImagensMcp(pedido));
  if (!r.ok) { status.textContent = r.erro; return; }
  btn.disabled = true; status.textContent = "Gerando…";
}
```

Carregar MCPs/modelos no `DOMContentLoaded`. Estilo: fundo `#f6f7f9`, texto `#1a1a1a`, sem roxo.

- [ ] **Step 3: PASS**

---

### Task 4: Cliente MCP — `count` + múltiplas URLs

**Files:**
- Modify: `app/.../ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.kt`
- Modify: `ResultadoDaGeracaoMcpHiggsfield.Pronto` se necessário para `urls: List<String>` **ou** manter um `Pronto` por job e loop no serviço (preferência: parâmetro `quantidade` no `gerarImagem` que faz loop interno com `count` na 1ª tentativa e fallback loop)

**Interfaces:**
- Produces: `gerarImagem(..., quantidade: Int = 1): ResultadoDaGeracaoMcpHiggsfield`  
  - Preferência prática: serviço chama `gerarImagem` N vezes se `count` não devolver N URLs; **mínimo:** passar `params.put("count", quantidade.coerceIn(1,4))` e continuar extraindo a 1ª URL (serviço faz o restante das chamadas).

- [ ] **Step 1:** Em `gerar(...)`, após `aspect_ratio`, se `imagem && quantidade > 1`: `params.put("count", quantidade)`.
- [ ] **Step 2:** Assinatura `gerarImagem(..., quantidade: Int = 1)` propaga para `gerar`.
- [ ] **Step 3:** `assembleDebug` compila.

---

### Task 5: Gravação com id + preview

**Files:**
- Modify: `app/.../GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`

**Interfaces:**
- Produces: `data class ImagemHiggsfieldGuardada(val id: String, val caminhoArquivo: String)`  
  `fun guardarImagemComMetadados(bytes: ByteArray, prompt: String): ImagemHiggsfieldGuardada?`

- [ ] **Step 1:** Implementar usando `guardarQuadro` existente; caminho = `File(pastaDaBiblioteca, item.nomeDoArquivo).absolutePath`.
- [ ] **Step 2:** Manter `guardarImagem` antigo delegando para o novo (texto de sucesso igual).

---

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

### Task 8: Verificação no tablet

- [ ] Abrir coleção → componente “Geração MCP — imagem” na biblioteca
- [ ] Sem Higgsfield ligado → erro claro ao listar/gerar
- [ ] Com sessão: listar modelos preenche select
- [ ] Gerar 1 sem ref → aparece na grade + biblioteca
- [ ] Gerar com ref (galeria) e (biblioteca) → OK
- [ ] Quantidade 2 → dois itens (ou fallback de 2 jobs)

---

## Spec coverage (self-review)

| Spec | Task |
|------|------|
| Ponte listarMcps/modelos/escolher/limpar/gerar | 1,2,6,7 |
| pedidoJson + quantidade 1–4 | 1,4,6 |
| UI HTML minimalista | 3,7 |
| Preview + biblioteca | 5,6,7 |
| Galeria + biblioteca | 7 |
| Sem confirmação | 6,7 |
| Seed id estável sem overwrite | 3,7 |
| count / fallback N | 4,6 |
| Erros SEM_SESSAO / parciais | 6 |
| If-None-Match regressão | (já no cliente; não remover na Task 4) |

## Execution

Após salvar este plano: escolher **subagent-driven-development** ou **executing-plans (inline)**.
