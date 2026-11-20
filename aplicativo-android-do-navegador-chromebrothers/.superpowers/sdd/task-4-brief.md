### Task 4: Repositório no aparelho + listeners

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/RepositorioDasConversasDoChatNoAparelho.kt`

**Interfaces:**
- Consumes: Persistencia*, Indice*, Titulo*, Medidor*, Compactacao* (Task 1–3)
- Produces:
  - `class RepositorioDasConversasDoChatNoAparelho(context: Context)`
  - pasta `context.filesDir/conversas_do_chat/`; arquivos `indice.json`, `conversa_<id>.json`
  - `fun listar(): List<EntradaDoIndiceDaConversaDoChat>`
  - `fun carregar(id: String): List<MensagemDeChat>?` (null se ilegível/ausente)
  - `fun salvar(id: String?, mensagens: List<MensagemDeChat>): ResultadoDoSaveDaConversa`  
    - se `id` null e mensagens não vazias → gera id (`UUID` ou `System.currentTimeMillis().toString()`), título via Titulo*, grava arquivo, atualiza índice, retorna id  
    - se bytes da conversa > TETO_DURO → `Resultado` com `bloqueadoPorTetoDuro = true` e **não** grava  
    - senão grava; `avisarLimite = ultrapassouQuantidade || ultrapassouBytesTotais` (após merge no índice)
  - `fun excluir(id: String)`
  - `fun bytesTotais(): Long` / `fun percentualDoMedidor(): Int` / `fun faixaDoMedidor(): Faixa…`
  - `fun registrarOuvinte(ouvinte: OuvinteDasConversasDoChat)` / `removerOuvinte`
  - `fun interface OuvinteDasConversasDoChat { fun conversaExcluida(id: String); fun conversaResumida(id: String) }`
  - `data class ResultadoDoSaveDaConversa(val id: String?, val avisoDeLimite: Boolean, val bloqueadoPorTetoDuro: Boolean, val erro: String? = null)`

`excluir` e (na Task 6) resumir bem-sucedido chamam os ouvintes no thread que invocou (painel usa main).

- [ ] **Step 1: Implementar repositório** (I/O simples `File.readText`/`writeText`; sync `synchronized` no objeto para índice)

- [ ] **Step 2: Sem teste JVM obrigatório** (I/O Android). Verificar compile:

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Expected: SUCCESS.

---

