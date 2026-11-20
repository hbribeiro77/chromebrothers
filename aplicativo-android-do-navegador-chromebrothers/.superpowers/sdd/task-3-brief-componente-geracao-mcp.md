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

