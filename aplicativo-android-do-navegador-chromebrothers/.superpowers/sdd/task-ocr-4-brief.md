### Task 4: `abrirComTextoNoCampo` no chat

**Files:**
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDoChatDaOpenRouterNoNavegador.kt`

**Interfaces:**
- Produces: `fun abrirComTextoNoCampo(texto: String)` — se gaveta fechada chama lógica de `abrir()`; `campoMensagem.setText(texto)`; cursor no fim; foco no campo.

- [ ] **Step 1: Implement**

Tornar `abrir()` acessível (já private — extrair ou chamar de método público novo). Não chamar `enviar()`.

- [ ] **Step 2: Compile** via assembleDebug (junto Task 5 ok).

---

