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
