# Higgsfield imagem/vídeo no chat — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** No chat do ChromeBrothers, gerar imagem e vídeo via Higgsfield MCP (conta OAuth), com Confirmar/Liberar, salvando na biblioteca e mostrando no chat.

**Architecture:** Duas tools OpenRouter (`gerar_imagem_higgsfield`, `gerar_video_higgsfield`) com args `prompt` + `modelo` opcional. Confirmação no padrão `PedidoDeAcaoConfirmadaDoChat` + flag `LiberacaoDoHiggsfieldParaOChat`. App: sessão OAuth (device code / PKCE conforme metadata), cliente MCP mínimo em `https://mcp.higgsfield.ai/mcp`, download → biblioteca de imagens/vídeos.

**Tech Stack:** Kotlin, OkHttp, módulo `regras-de-video-do-navegador`, TDD nas regras, Android Custom Tab / device code OAuth.

**Spec:** `docs/superpowers/specs/2026-11-05-integracao-higgsfield-gerar-imagem-e-video-no-chat-do-chromebrothers-design.md`

## Global Constraints

- pt-BR; nomes de arquivo longos e descritivos
- TDD nas regras; após mudanças no app: testes relevantes + `assembleDebug` + `adb install`
- Sem commits não pedidos pelo usuário
- Windows PowerShell; Gradle: `$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat` com `JAVA_HOME` Adoptium 21
- YAGNI: só imagem e vídeo; sem Soul/upscale/áudio/listar modelos
- MCP URL: `https://mcp.higgsfield.ai/mcp`

## File map

| Arquivo | Papel |
|---------|--------|
| `regras-.../GeracaoDeImagemEVideoPeloHiggsfieldNoChat.kt` | Parse args, defaults, perguntas, texto de sucesso/erro |
| `regras-.../LiberacaoDoHiggsfieldParaOChat.kt` | Flag liberado / revogar / segueSemPerguntar |
| `regras-.../AcoesConfirmadasDoChatDaOpenRouter.kt` | Tools + pedidos + interpretar |
| `regras-.../LiberacaoDeNavegacaoDosSitesParaOChat.kt` | Não aplicar host a pedidos Higgsfield |
| `app/.../SessaoDaContaHiggsfieldNoAparelho.kt` | Tokens + prefs |
| `app/.../ClienteMcpDoHiggsfieldParaGeracaoNoChat.kt` | OAuth + tools/call + poll |
| `app/.../PainelDoChatDaOpenRouterNoNavegador.kt` | Engrenagem + confirmação + execução |
| `strings` / layout engrenagem | Ligar / Desligar / revogar liberação |

---

### Task 1: Regras — parse, liberação e pedidos

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/GeracaoDeImagemEVideoPeloHiggsfieldNoChat.kt`
- Create: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/LiberacaoDoHiggsfieldParaOChat.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaGeracaoDeImagemEVideoPeloHiggsfieldNoChat.kt`
- Test: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaLiberacaoDoHiggsfieldParaOChat.kt`

**Interfaces:**
- Produces: `PedidoDeAcaoConfirmadaDoChat.GerarImagemHiggsfield`, `GerarVideoHiggsfield` (via Acoes na Task 2); `GeracaoDeImagemEVideoPeloHiggsfieldNoChat.lerImagem/lerVideo`; `LiberacaoDoHiggsfieldParaOChat.liberar/revogar/estaLiberado`

- [x] **Step 1:** Testes falhando — prompt obrigatório; modelo opcional trim; limites; perguntas com modelo; liberar/revogar
- [x] **Step 2:** Implementação mínima
- [x] **Step 3:** Rodar testes do módulo `:regras-de-video-do-navegador:test` filtrando essas classes — PASS

### Task 2: Wire nas ações confirmadas do chat

**Files:**
- Modify: `AcoesConfirmadasDoChatDaOpenRouter.kt`
- Modify: `FerramentasDeLeituraDoChatDaOpenRouter.kt` (permitidas via nomes)
- Modify: testes de ações confirmadas existentes se quebrarem
- Modify: `LiberacaoDeNavegacaoDosSitesParaOChat.kt` — `hospedeiroDaAcao` / `segueSemPerguntar` não usam host para Higgsfield
- Modify: `PainelDoChat…` só o suficiente para `when` compilar se necessário na Task 4

- [ ] **Step 1:** Constantes `GERAR_IMAGEM_HIGGSFIELD` / `GERAR_VIDEO_HIGGSFIELD`, defs JSON, `interpretar`, sealed pedidos
- [ ] **Step 2:** Testes de interpretação + liberação Higgsfield vs site
- [ ] **Step 3:** PASS

### Task 3: Sessão + UI engrenagem (Ligar / Desligar / liberação)

**Files:**
- Create: `app/.../SessaoDaContaHiggsfieldNoAparelho.kt`
- Create: `app/.../EstadoDaLiberacaoDoHiggsfieldNoAparelho.kt`
- Modify: layout/strings da engrenagem do chat
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — botões

- [ ] **Step 1:** Persistência tokens + flag liberado
- [ ] **Step 2:** UI Ligar (dispara OAuth Task 4), Desligar, revogar liberação
- [ ] **Step 3:** assembleDebug

### Task 4: Cliente OAuth + MCP (imagem e vídeo)

**Files:**
- Create: `app/.../ClienteMcpDoHiggsfieldParaGeracaoNoChat.kt` (ou split OAuth/MCP se crescer)
- Modify: `PainelDoChat…` — após Confirmar, chamar cliente; poll; erros

**Interfaces:**
- `gerarImagem(prompt, modelo?, callback)` / `gerarVideo(...)` → URL ou bytes + metadados
- Sem sessão → mensagem da regra “Ligue o Higgsfield…”

- [ ] **Step 1:** Dynamic client registration + device_code ou auth code PKCE (Custom Tab)
- [ ] **Step 2:** `tools/list` (descobrir nomes reais) + `tools/call` gerar + poll
- [ ] **Step 3:** Mapear erros crédito/modelo/timeout

### Task 5: Biblioteca + chat + confirmação Liberar

**Files:**
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — chip Confirmar/Liberar/Cancelar; `segueSemPerguntar` Higgsfield
- Modify: gravacao biblioteca imagens/vídeos existente
- Modify: pergunta da confirmação mostra prompt+modelo

- [ ] **Step 1:** Liberar seta flag e segue
- [ ] **Step 2:** Sucesso → guardar na biblioteca → tool result com id + abrir se padrão do chat permitir
- [ ] **Step 3:** assembleDebug + adb install

### Task 6: Verificação

- [ ] Testes unitários das Tasks 1–2
- [ ] Smoke manual: ligar; 1 imagem; 1 vídeo; Liberar; Desligar

---

## Notas de execução

- Nomes reais das tools MCP só após `tools/list` autenticado — adaptar o cliente, manter nomes estáveis no OpenRouter.
- Defaults de modelo: constantes nas regras (ajustáveis quando soubermos IDs oficiais do server).
- Sem commit automático.
