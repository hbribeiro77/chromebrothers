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

