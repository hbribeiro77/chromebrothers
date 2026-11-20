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

