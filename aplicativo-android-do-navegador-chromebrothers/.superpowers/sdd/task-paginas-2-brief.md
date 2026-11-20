### Task 2: Persistência no app

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo.kt`

**Produces:**
```kotlin
class GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo(contexto: Context) {
    fun carregar(): BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador
    fun gravar(biblioteca: Biblioteca…)
    fun lerHtml(id: String): String?  // File(pasta, "$id.html")
    fun escreverHtml(id: String, html: String)
    fun apagarArquivos(id: String)
}
```

Índice: `filesDir/biblioteca-de-paginas-da-colecao/itens.json` com `id`, `titulo`, `atualizadoEm` (html em `$id.html`, não no JSON gigante se possível — ou html no JSON se mais simples; **preferir arquivo `.html` por id**).

- [ ] **Step 1:** Implementar + smoke via assemble (sem teste instrumentado obrigatório)
- [ ] **Step 2:** Wire carregar no `AtividadePrincipal` junto às outras bibliotecas

---

