# Biblioteca de páginas HTML da coleção — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Commits só se o usuário pedir. Steps use `- [ ]`.

**Goal:** Guardar/reabrir/apagar páginas HTML da coleção flutuante numa seção Páginas da gaveta; chat lista/guarda/abre/apaga; editar com `publicar_pagina` não persiste até guardar de novo.

**Architecture:** Regras puras (`BibliotecaDePaginas…` + parse de tools) em `regras-de-video-do-navegador`; persistência JSON+HTML em `app` (padrão imagens); UI na gaveta + botão na coleção; wire em `PainelDoChat` / liberação / instrução.

**Tech Stack:** Kotlin, kotlin.test, SharedPreferences/filesDir, Android WebView coleção existente, OpenRouter tools.

**Spec:** `docs/superpowers/specs/2026-11-01-biblioteca-de-paginas-html-da-colecao-flutuante-design.md`

## Global Constraints

- Título ≤ 120; HTML ≤ 300_000; máx **40** itens
- Guardar: botão coleção + tool chat; abrir na **mesma flutuante** (modo Página)
- `publicar_pagina` não grava na biblioteca; só `guardar` (explícito)
- Guardar com `idOrigem` → atualiza; sem → cria
- Apagar na gaveta: remove disco; coleção ao vivo **mantém** HTML, limpa só o vínculo de origem
- Confirmação nas ações (exceto site liberado); `listar` é leitura
- Sem favoritos/versionamento/mídia no save (v1)
- pt-BR; nomes de arquivo longos; Windows PowerShell; sem commit salvo pedido

## File map

| Arquivo | Papel |
|---------|--------|
| `BibliotecaDePaginasHtmlDaColecaoFlutuanteDoNavegador.kt` | Item + biblioteca em memória |
| `InterpretacaoDasAcoesDaBibliotecaDePaginasDoChat.kt` (ou em AcoesConfirmadas) | parse args |
| `GravacaoDaBibliotecaDePaginasHtmlDaColecaoNoArmazenamentoDoAplicativo.kt` | disco |
| `PainelDaColecaoFlutuante…` | botão guardar + idOrigem + carregar |
| `PainelDaGavetaDaBibliotecaDeVideos.kt` + layout/strings | seção Páginas |
| `AcoesConfirmadas…`, `PedidoDeAcao…`, `Liberacao…`, `Ferramentas…instrucao` | chat |
| `PainelDoChat…`, `AtividadePrincipal…` | callbacks |

---

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

### Task 3: Coleção — idOrigem + guardar + carregar

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: layout da coleção + `strings.xml`

**Produces:**
- `var idOrigemDaBiblioteca: String?`
- `fun carregarPaginaDaBiblioteca(item: Item…): String` — set titulo/html, modo Página, idOrigem=item.id, mostrar
- `fun guardarNaBiblioteca(biblioteca, gravacao, agora, novoId): String` — usa htmlDaPagina + titulo; sanitizar; guardar; gravar disco; toast via retorno
- Botão Guardar; limpar idOrigem ao limpar coleção; ao `publicarPagina` **não** limpar idOrigem (continua “este id” até guardar/abrir outro/limpar)

- [ ] **Step 1:** UI + métodos
- [ ] **Step 2:** Compile

---

### Task 4: Seção Páginas na gaveta

**Files:**
- Modify: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- strings

**Behavior:**
- Botão seção Páginas
- Lista itens (titulo + data); clique → callback `aoAbrirPagina(item)` → Activity carrega na coleção e mostra flutuante
- Long-press ou botão apagar → apaga biblioteca+disco; se idOrigem == id, `painelDaColecao.limparVinculoBiblioteca()` only

- [ ] **Step 1:** UI + wire
- [ ] **Step 2:** Compile

---

### Task 5: Tools do chat + liberação + instrução

**Files:**
- Modify: `AcoesConfirmadasDoChatDaOpenRouter.kt`, `PedidoDeAcaoConfirmadaDoChat`, `LiberacaoDeNavegacaoDosSitesParaOChat.kt`
- Modify: `FerramentasDeLeituraDoChatDaOpenRouter.kt` (listar como leitura **ou** ação sem confirmação)
- Modify: `PainelDoChat…`, `AtividadePrincipal…`
- Tests: interpretar args; liberação inclui guardar/abrir/apagar; listar em `permitidas`/json

**Tools:**
- `listar_paginas_da_biblioteca` — leitura (como listar_midia)
- `guardar_pagina_da_colecao` — ação confirmada, args opcionais vazios
- `abrir_pagina_da_biblioteca` — `id` ou `titulo`
- `apagar_pagina_da_biblioteca` — `id` obrigatório

`listar` pode ser tool de leitura pura no Painel (callback), não em AcoesConfirmadas.

- [ ] **Step 1:** Testes de interpretação + nomes no json/instrucao
- [ ] **Step 2:** Wire executarAcao / leitura
- [ ] **Step 3:** `:regras-de-video-do-navegador:test` + `:app:assembleDebug` + `adb install -r`

---

## Spec coverage

| Spec | Task |
|------|------|
| Modelo + limite 40 | 1 |
| Disco | 2 |
| Botão guardar + idOrigem + abrir flutuante | 3–4 |
| Tools + liberação + publicar não salva | 5 |
| Apagar mantém página ao vivo | 4–5 |
| Fora de escopo | não fazer |

## Self-review

- Sem TBD; nomes alinhados à spec
- HTML sanitizado no caminho de guardar (Task 3/5), não na biblioteca pura
