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

