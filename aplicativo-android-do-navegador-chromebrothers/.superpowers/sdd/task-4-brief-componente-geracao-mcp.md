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

