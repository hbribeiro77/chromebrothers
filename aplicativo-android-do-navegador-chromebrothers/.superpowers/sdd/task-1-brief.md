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

