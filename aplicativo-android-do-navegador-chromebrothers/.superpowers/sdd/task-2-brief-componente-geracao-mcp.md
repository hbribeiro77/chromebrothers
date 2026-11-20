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

