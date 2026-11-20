### Task 2: Índice + medidor

**Files:**
- Create: `…/IndiceDasConversasDoChatDaOpenRouter.kt`
- Create: `…/MedidorDeUsoDoHistoricoDoChatDaOpenRouter.kt`
- Test: `…/TesteDoIndiceDasConversasDoChatDaOpenRouter.kt`
- Test: `…/TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter.kt`

**Interfaces:**
- Produces:
  - `data class EntradaDoIndiceDaConversaDoChat(id, titulo, criadaEm, atualizadaEm, bytesAproximados, resumida: Boolean = false)`
  - `IndiceDasConversasDoChatDaOpenRouter.MAXIMO_DE_CONVERSAS = 40`
  - `TETO_DE_BYTES_TOTAIS`, `TETO_DURO_POR_CONVERSA` (Longs acima)
  - `escrever(entradas) / ler(bruto)`
  - `criar(entradas, id, titulo, agoraMs, bytes): List<…>` (nova no início)
  - `atualizar(entradas, id, titulo?, atualizadaEm, bytes, resumida?): List<…>`
  - `excluir(entradas, id): List<…>`
  - `bytesTotais(entradas): Long`
  - `ultrapassouQuantidade(entradas): Boolean` (≥ MAX após incluir)
  - `ultrapassouBytesTotais(entradas): Boolean`
  - `ultrapassouTetoDuroDaConversa(bytes): Boolean`
  - `enum class FaixaDoMedidorDoHistoricoDoChat { OK, ATENCAO, CHEIO }`
  - `MedidorDeUsoDoHistoricoDoChatDaOpenRouter.percentual(bytesTotais, teto): Int` (0..100, teto≤0 → 100)
  - `faixa(percentual): Faixa…` (≥100 CHEIO, ≥80 ATENCAO, senão OK)

- [ ] **Step 1: Testes índice**

```kotlin
@Test
fun criarAtualizarExcluirEBytes() {
    var idx = emptyList<EntradaDoIndiceDaConversaDoChat>()
    idx = IndiceDasConversasDoChatDaOpenRouter.criar(idx, "a", "T1", 10L, 100L)
    assertEquals(1, idx.size)
    assertEquals(100L, IndiceDasConversasDoChatDaOpenRouter.bytesTotais(idx))
    idx = IndiceDasConversasDoChatDaOpenRouter.atualizar(idx, "a", atualizadaEm = 20L, bytes = 200L, resumida = true)
    assertEquals(200L, idx.single().bytesAproximados)
    assertTrue(idx.single().resumida)
    idx = IndiceDasConversasDoChatDaOpenRouter.excluir(idx, "a")
    assertTrue(idx.isEmpty())
}

@Test
fun roundtripJsonDoIndice() {
    val original = listOf(EntradaDoIndiceDaConversaDoChat("id1", "Oi", 1L, 2L, 3L, false))
    val json = IndiceDasConversasDoChatDaOpenRouter.escrever(original)
    assertEquals(original, IndiceDasConversasDoChatDaOpenRouter.ler(json))
}
```

Formato índice: `{"conversas":[{...}]}`. `ler` inválido → `emptyList()`.

- [ ] **Step 2: Implementar índice + medidor (TDD)**

Medidor:

```kotlin
@Test
fun faixasDoMedidor() {
    assertEquals(FaixaDoMedidorDoHistoricoDoChat.OK, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(50))
    assertEquals(FaixaDoMedidorDoHistoricoDoChat.ATENCAO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(80))
    assertEquals(FaixaDoMedidorDoHistoricoDoChat.CHEIO, MedidorDeUsoDoHistoricoDoChatDaOpenRouter.faixa(100))
}
```

- [ ] **Step 3: Rodar testes**

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoIndiceDasConversasDoChatDaOpenRouter" --tests "com.chromebrothers.navegador.regras.TesteDoMedidorDeUsoDoHistoricoDoChatDaOpenRouter"
```

Expected: PASS.

---

