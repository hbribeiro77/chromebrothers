# Histórico de conversas do chat — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Persistir conversas do chat OpenRouter no aparelho (lista global, retomar, nova conversa, excluir, resumir, medidor circular de uso).

**Architecture:** Regras puras serializam mensagens/índice/medidor/compactação (TDD em `regras-de-video-do-navegador`). App grava em `filesDir/conversas_do_chat/`, expõe listener in-process e o `PainelDoChatDaOpenRouterNoNavegador` passa a ter sessão (`idDaConversaAtual`), save pós-turno e UI de histórico.

**Tech Stack:** Kotlin JVM (regras), Android View/Kotlin, OkHttp/`ClienteDeChatDaOpenRouter`, JSON manual via `TextoJsonDoChat` (sem Room).

**Spec:** `docs/superpowers/specs/2026-11-02-historico-de-conversas-do-chat-da-openrouter-design.md`

## Global Constraints

- pt-BR em strings e mensagens ao usuário
- Nomes de arquivo longos e descritivos
- PowerShell / Windows; Gradle: `$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"; & "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" …`
- Commits **somente** se o usuário pedir
- Lista **global**; anexos persistidos; sem limpeza automática
- Mesma conversa em várias janelas: última gravação vence; listener notifica excluir/resumir
- Limpar → Nova conversa
- Tetos v1: `MAXIMO_DE_CONVERSAS = 40`, `TETO_DE_BYTES_TOTAIS = 200L * 1024 * 1024`, `TETO_DURO_POR_CONVERSA = 50L * 1024 * 1024`, atenção ≥ 80%, cheio ≥ 100%

## File structure

| Arquivo | Responsabilidade |
|---------|------------------|
| `regras/…/PersistenciaDasMensagensDoChatDaOpenRouter.kt` | JSON ↔ `List<MensagemDeChat>` |
| `regras/…/TituloDaConversaDoChatDaOpenRouter.kt` | Título pela 1ª msg user |
| `regras/…/IndiceDasConversasDoChatDaOpenRouter.kt` | Entradas do índice + CRUD puro |
| `regras/…/MedidorDeUsoDoHistoricoDoChatDaOpenRouter.kt` | % e faixa ok/atenção/cheio |
| `regras/…/CompactacaoDaConversaDoChatDaOpenRouter.kt` | Prompt + compactar histórico |
| `app/…/RepositorioDasConversasDoChatNoAparelho.kt` | I/O + listeners |
| `app/…/VistaDoMedidorCircularDoHistoricoDoChat.kt` | View do arco |
| `PainelDoChatDaOpenRouterNoNavegador.kt` | Sessão, save, UI |
| Layout + strings | Nova conversa, medidor, lista |

---

### Task 1: Persistência das mensagens + título

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/PersistenciaDasMensagensDoChatDaOpenRouter.kt`
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/TituloDaConversaDoChatDaOpenRouter.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaPersistenciaDasMensagensDoChatDaOpenRouter.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoTituloDaConversaDoChatDaOpenRouter.kt`

**Interfaces:**
- Produces:
  - `PersistenciaDasMensagensDoChatDaOpenRouter.escrever(mensagens: List<MensagemDeChat>): String`
  - `PersistenciaDasMensagensDoChatDaOpenRouter.ler(bruto: String): List<MensagemDeChat>` (inválido → lista vazia)
  - `TituloDaConversaDoChatDaOpenRouter.gerar(mensagens: List<MensagemDeChat>, maximo: Int = 60): String`

- [ ] **Step 1: Teste que falha — roundtrip user com texto**

```kotlin
@Test
fun roundtripDeMensagemUserComTexto() {
    val original = listOf(MensagemDeChat(papel = "user", conteudo = "Olá \"mundo\""))
    val json = PersistenciaDasMensagensDoChatDaOpenRouter.escrever(original)
    val lido = PersistenciaDasMensagensDoChatDaOpenRouter.ler(json)
    assertEquals(original, lido)
}
```

- [ ] **Step 2: Rodar e ver falha**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaPersistenciaDasMensagensDoChatDaOpenRouter.roundtripDeMensagemUserComTexto"
```

Expected: compile error / unresolved `PersistenciaDasMensagensDoChatDaOpenRouter`.

- [ ] **Step 3: Implementar escrever/ler**

Formato:

```json
{"mensagens":[{"papel":"user","conteudo":"...","idDaChamada":"","partes":[],"chamadas":[]}]}
```

Cada parte: `tipo`, `texto`, `mime`, `nome`, `dados`.  
Cada chamada: `id`, `nome`, `argumentos`.  
Usar `TextoJsonDoChat.escapar`, `objetosDaChave`, `valorDaChave`. Array raiz via chave `mensagens`.

- [ ] **Step 4: Testes extras no mesmo arquivo**

- Roundtrip com `partes` (imagem mime+dados) e com `chamadas` + mensagem `tool` com `idDaChamada`.
- `ler("{}")` e `ler("lixo")` → `emptyList()`.

- [ ] **Step 5: Título**

```kotlin
@Test
fun tituloPegaPrimeiraMensagemUserCortada() {
    val msgs = listOf(
        MensagemDeChat("system", "x"),
        MensagemDeChat("user", "a".repeat(100)),
    )
    val t = TituloDaConversaDoChatDaOpenRouter.gerar(msgs, maximo = 60)
    assertEquals(60, t.length)
    assertTrue(t.startsWith("aaa"))
}

@Test
fun semUserUsaConversa() {
    assertEquals("Conversa", TituloDaConversaDoChatDaOpenRouter.gerar(emptyList()))
}
```

Implementar: primeira `papel == "user"`, `conteudo` trim, colapsar espaços, cortar em `maximo`; se vazio → `"Conversa"`.

- [ ] **Step 6: Rodar suíte dos dois testes**

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaPersistenciaDasMensagensDoChatDaOpenRouter" --tests "com.chromebrothers.navegador.regras.TesteDoTituloDaConversaDoChatDaOpenRouter"
```

Expected: PASS.

---

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

### Task 3: Compactação + prompt de resumo

**Files:**
- Create: `…/CompactacaoDaConversaDoChatDaOpenRouter.kt`
- Test: `…/TesteDaCompactacaoDaConversaDoChatDaOpenRouter.kt`

**Interfaces:**
- Produces:
  - `CompactacaoDaConversaDoChatDaOpenRouter.ULTIMAS_TROCAS_USER_ASSISTANT = 2`
  - `promptParaResumir(mensagens: List<MensagemDeChat>): List<MensagemDeChat>` — system/user pedindo resumo fiel pt-BR do histórico (texto das msgs user/assistant; anexos descritos como `[imagem]`/`[arquivo]` sem base64)
  - `aplicar(resumo: String, mensagensOriginais: List<MensagemDeChat>): List<MensagemDeChat>` — `[MensagemDeChat("assistant", resumoTrim)]` + últimas até 2 pares user→assistant **sem** `partes` (copiar texto só)

- [ ] **Step 1: Teste aplicar**

```kotlin
@Test
fun compactaMantemUltimasDuasTrocasSemPartes() {
    val hist = listOf(
        MensagemDeChat("user", "u1", partes = listOf(ParteDeConteudoDoChat("imagem", mime = "image/png", dados = "xx"))),
        MensagemDeChat("assistant", "a1"),
        MensagemDeChat("user", "u2"),
        MensagemDeChat("assistant", "a2"),
        MensagemDeChat("user", "u3"),
        MensagemDeChat("assistant", "a3"),
    )
    val out = CompactacaoDaConversaDoChatDaOpenRouter.aplicar("RESUMO", hist)
    assertEquals("RESUMO", out.first().conteudo)
    assertEquals("assistant", out.first().papel)
    assertTrue(out.drop(1).none { it.partes.isNotEmpty() })
    assertEquals(listOf("u2", "a2", "u3", "a3"), out.drop(1).map { it.conteudo })
}
```

Regra das “últimas 2 trocas”: percorrer de trás; coletar mensagens `user`/`assistant` (ignorar tool/system de tools); agrupar em pares quando possível; ficar com no máximo 4 mensagens finais nessa ordem cronológica.

- [ ] **Step 2: Teste prompt**

`promptParaResumir` retorna ≥1 mensagem; conteúdo menciona resumir / português; **não** contém a string base64 `"xx"` do anexo do hist de exemplo.

- [ ] **Step 3: Implementar + rodar teste**

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDaCompactacaoDaConversaDoChatDaOpenRouter"
```

Expected: PASS.

---

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

### Task 5: Painel — sessão, save, nova conversa, remontar bolhas

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDoChatDaOpenRouterNoNavegador.kt`
- Modify: `app/src/main/res/values/strings_do_navegador_chromebrothers.xml` (string Nova conversa)
- Modify: `layout_atividade_principal_do_navegador_chromebrothers.xml` (texto do botão limpar → nova conversa; id pode permanecer ou renomear com cuidado)

**Interfaces:**
- Consumes: `RepositorioDasConversasDoChatNoAparelho`
- Produces (comportamento):
  - campo `idDaConversaAtual: String?`
  - `repositorio` criado no init do painel
  - após turno completo bem-sucedido (onde hoje finaliza `pedirResposta` sem erro): `persistirConversaAtual()`
  - `novaConversa()` substitui `limparConversa()`: se `historico.isNotEmpty()` → persistir; depois limpa como hoje + `idDaConversaAtual = null`
  - `carregarConversa(id)`: lê repo, seta id+historico, `lista.removeAllViews()`, para cada msg `user`/`assistant` com conteúdo (ou partes) chama `adicionarBolha`
  - registra ouvinte: se `conversaExcluida`/`conversaResumida` e id == atual → carregar de novo ou nova sessão

- [ ] **Step 1: Trocar string `limpar_o_chat_da_openrouter` para `Nova conversa` (ou nova string `nova_conversa_do_chat` e apontar o botão)**

- [ ] **Step 2: Wire repositório + persistir + novaConversa + carregar**

Toast se `bloqueadoPorTetoDuro` ou `erro`; Toast se `avisoDeLimite` (“Histórico cheio: exclua ou resuma conversas”).

- [ ] **Step 3: compileDebugKotlin**

Expected: SUCCESS.

---

### Task 6: Medidor circular, lista, excluir, resumir

**Files:**
- Create: `app/src/main/kotlin/com/chromebrothers/navegador/VistaDoMedidorCircularDoHistoricoDoChat.kt`
- Create: `app/src/main/res/layout/layout_item_da_lista_de_conversas_do_chat.xml` (título, data, botão resumir, botão excluir)
- Modify: layout do header do chat — inserir `VistaDoMedidorCircular…` (ex. 28dp) antes do botão Nova conversa; toque abre lista
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — Dialog/Bottom-style lista (`AlertDialog` com `ListView`/`Recycler` simples ou `LinearLayout` em `ScrollView` dentro de dialog)
- Strings: histórico, excluir, resumir, confirmar resumo, erros

**Medidor:** `onDraw` arco de fundo + arco de progresso; cor por faixa (OK `#8AB4F8`, ATENCAO `#FDD663`, CHEIO `#F28B82`); `contentDescription` com percentual; `setProgresso(percentual, faixa)`.

**Lista:** `repositorio.listar()` ordenado por `atualizadaEm` desc; toque item → `carregarConversa`; excluir → confirm → `repositorio.excluir` + atualizar medidor; resumir → confirm → `cliente.enviar(chave, modelo, Compactacao…promptParaResumir(msgs), comFerramentas=false)`, ao terminar sem erro → `aplicar` → `salvar(id, compactado)` com flag resumida no índice → notificar ouvintes → atualizar medidor.

- [ ] **Step 1: View do medidor + layout header**

- [ ] **Step 2: Dialog da lista + excluir + resumir**

- [ ] **Step 3: Atualizar medidor em `ligar()`, pós-save, pós-excluir/resumir**

- [ ] **Step 4: assembleDebug + install**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
if ($LASTEXITCODE -eq 0) {
  & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
}
```

Expected: BUILD SUCCESSFUL; `Success` no adb.

**Teste manual:** nova conversa com texto+anexo → matar app → retomar; medidor; resumir; excluir; duas janelas mesma conversa.

---

## Self-review (spec coverage)

| Spec | Task |
|------|------|
| JSON mensagens + índice em filesDir | 1, 2, 4 |
| Título 1ª user | 1 |
| Save pós-turno / nova conversa | 5 |
| Lista global retomar/excluir | 6 |
| Medidor + alerta limite | 2, 5, 6 |
| Resumir compacta + listener multi-janela | 3, 4, 6 |
| Teto duro bloqueia | 4, 5 |
| Sem referenciar outra conversa | (fora) |
