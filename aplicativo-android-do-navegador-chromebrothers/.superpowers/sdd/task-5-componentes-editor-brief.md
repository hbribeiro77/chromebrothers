### Task 5: Fechar janelas ao sair do editor

**Files:**
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` (passar `encerrarTodas` ou callback)

- [ ] **Step 1:** Callback `aoEncerrarEdicaoDeImagem: (() -> Unit)?` ou ligar direto `painelDaColecao.encerrarTodas()`.

- [ ] **Step 2:** Invocar ao abandonar/cancelar/concluir edição e em `esconder()` quando estava editando — alinhado a `abandonarEdicaoNaMemoria` / `cancelarEdicao` / ramo de edição em `tratarVolta`.

Spec: **fechar** janelas abertas nesse contexto (não deixar órfãs).

---

