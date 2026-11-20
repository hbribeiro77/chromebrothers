# Suporte ao MCP do Sjinn — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Gerar imagens na coleção via Sjinn MCP (`create_image_task` / `get_task` / `upload_asset` / `models_list`), com OAuth CIMD e seletor já existente.

**Architecture:** Sessão + cliente Sjinn espelhando Higgsfield; `ServicoDeGeracao…` roteia por `pedido.mcp`; regras JVM puras para parse de catálogo/tarefa/resolução.

**Tech Stack:** Kotlin Android, OkHttp, MCP streamable HTTP, OAuth PKCE + CIMD.

## Global Constraints

- Não alterar a área do prompt HTML nem remover `selMcp`.
- MVP só imagem.
- Filenames longos e descritivos.
- TDD nas regras JVM.

---

### Task 1: Pedido + okMcps aceitam sjinn

**Files:**
- Modify: `PedidoDeGeracaoDeImagemViaMcpNaColecao.kt`
- Modify: `TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt`

- [ ] Teste: `mcp:sjinn` Ok; default modelo `gpt-image-2` quando vazio
- [ ] `MCP_SJINN`, `MCPS_CONHECIDOS`, `modeloPadrao(mcp)`, `okMcps()` com higgsfield+sjinn
- [ ] Run tests

### Task 2: Listagem + extração + resolução Sjinn

**Files:**
- Create: `ListagemDeModelosDoSjinnNoChat.kt` + teste
- Create: `ExtracaoDaUrlDoResultadoDaTarefaSjinn.kt` + teste
- Create: `ResolucaoDoPedidoDeGeracaoParaOMcpDoSjinn.kt` + teste (1k→1K)

### Task 3: Sessão + Cliente MCP Sjinn

**Files:**
- Create: `SessaoDaContaSjinnNoAparelho.kt`
- Create: `ClienteMcpDoSjinnParaGeracaoNoChatDoChromebrothers.kt`
- CIMD JSON + constante URL

### Task 4: Roteamento serviço + login UI

**Files:**
- Modify: `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`
- Modify: `AtividadePrincipal…` wiring
- Modify: layout + strings + `PainelDoChat…` botões Ligar/Desligar Sjinn

### Task 5: Build + install

- [ ] `:regras…:test` + `assembleDebug` + `adb install`
