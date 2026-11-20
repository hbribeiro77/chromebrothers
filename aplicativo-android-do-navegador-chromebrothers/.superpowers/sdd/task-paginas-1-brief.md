### Task 1: Biblioteca em memória (regras)

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt`
- Create: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt`

**Produces:**
```kotlin
data class ItemDaBibliotecaDePaginasHtmlDaColecao(
    val id: String,
    val titulo: String,
    val html: String,
    val atualizadoEm: Long,
)

sealed class ResultadoDaBibliotecaDePaginasHtml {
    data class Ok(val item: ItemDaBibliotecaDePaginasHtmlDaColecao, val criou: Boolean) : …
    data class Recusada(val mensagem: String) : …
}

class BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador(
    inicial: List<ItemDaBibliotecaDePaginasHtmlDaColecao> = emptyList(),
    private val limite: Int = 40,
) {
    fun listar(): List<…> // mais recentes primeiro
    fun buscar(id: String): Item…?
    fun buscarPorTitulo(titulo: String): Item…? // match trim ignoreCase; primeiro
    fun guardar(
        titulo: String,
        html: String,
        idExistente: String?,
        agora: Long,
        novoId: () -> String,
    ): ResultadoDaBibliotecaDePaginasHtml
    fun apagar(id: String): Boolean
    fun substituirPor(itens: List<…>)
}
```

Regras de `guardar`:
- título trim; vazio → Recusada; >120 → Recusada
- html trim; vazio → Recusada; >300_000 → Recusada
- se `idExistente` e item existe → atualiza titulo/html/atualizadoEm, move pro topo, `criou=false`
- se `idExistente` inexistente ou null → cria com `novoId()`, se `listar().size >= limite` e não é update → Recusada "A biblioteca de páginas chegou ao limite."
- sanitização HTML **não** é desta task (fica no app ao guardar, usando sanitizer existente)

- [ ] **Step 1:** Testes — criar; atualizar por id; limite 40; título/html inválidos; apagar; buscar título
- [ ] **Step 2:** RED → implementar → GREEN  
Gradle: `:regras-de-video-do-navegador:test --tests "…TesteDaBibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador"`

---

