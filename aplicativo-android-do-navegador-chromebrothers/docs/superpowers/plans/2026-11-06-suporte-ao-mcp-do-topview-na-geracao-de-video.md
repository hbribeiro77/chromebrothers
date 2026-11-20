# Topview MCP na geração de vídeo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax. **Não commit automático** (só se o usuário pedir).

**Goal:** Integrar Topview (OAuth MCP em `https://mcp.topview.ai/mcp`) no gerador de vídeo da coleção e no chat, no mesmo esquema do Higgsfield/Sjinn.

**Architecture:** Cliente MCP dedicado + sessão EncryptedSharedPrefs; catálogo estático de display names; `Servico…` despacha `topview`; chat com tool `gerar_video_topview` + liberação própria. Parsers JVM em `regras-de-video-do-navegador`.

**Tech Stack:** Kotlin, OkHttp, EncryptedSharedPrefs, WebView bridge, módulo `regras-de-video-do-navegador` (JVM tests).

## Global Constraints

- Auth = OAuth MCP PKCE (porta loopback **8788**); sem API Key/UID.
- Default modelo = **Seedance 2.5** (display name na API).
- 0 refs → text-to-video; 1+ → upload + image-to-video.
- Fora: marketing MCP, imagem Topview, refactor MCP genérico.
- Build: `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat"`; adb em `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.
- Respostas ao usuário em pt-BR.

## File map

| Arquivo | Papel |
|---------|--------|
| `regras-de-video/…/CatalogoDeModelosDeVideoDoTopviewNaColecao.kt` | Catálogo estático |
| `regras-de-video/…/ExtracaoDaUrlDoResultadoDaTarefaTopview.kt` | Parse submit/query |
| `regras-de-video/…/ExtracaoDoFileIdDoUploadTopview.kt` | Parse upload |
| `regras-de-video/…/GeracaoDeVideoPeloTopviewNoChat.kt` | Defaults + SEM_SESSAO |
| `regras-de-video/…/LiberacaoDoTopviewParaOChat.kt` | Confirmar/Liberar |
| `regras-de-video/…/PedidoDeGeracaoDeVideoViaMcpNaColecao.kt` | `MCP_TOPVIEW` |
| `regras-de-video/…/AcoesConfirmadasDoChatDaOpenRouter.kt` | `GERAR_VIDEO_TOPVIEW` |
| `regras-de-video/…/FerramentasDeLeituraDoChatDaOpenRouter.kt` | Schema da tool |
| `app/…/SessaoDaContaTopviewNoAparelho.kt` | Tokens + liberado |
| `app/…/ClienteMcpDoTopviewParaGeracaoNoChromebrothers.kt` | OAuth + tools |
| `app/…/ServicoDeGeracaoDeImagemViaMcp…kt` | listar + gerar Topview |
| `app/…/PainelDoChatDaOpenRouter…kt` + layout XML | Ligar/Desligar UI + tool |
| `HtmlDoComponenteDeGeracaoDeVideo…kt` | Aceitar topview no seletor (okMcps) |

---

### Task 1: Pedido + catálogo + parsers + liberação (JVM)

**Files:**
- Create: `CatalogoDeModelosDeVideoDoTopviewNaColecao.kt`, `ExtracaoDaUrlDoResultadoDaTarefaTopview.kt`, `ExtracaoDoFileIdDoUploadTopview.kt`, `GeracaoDeVideoPeloTopviewNoChat.kt`, `LiberacaoDoTopviewParaOChat.kt` (+ testes espelho)
- Modify: `PedidoDeGeracaoDeVideoViaMcpNaColecao.kt`, `AcoesConfirmadasDoChatDaOpenRouter.kt` (const + sealed se necessário), `FerramentasDeLeituraDoChatDaOpenRouter.kt`

**Produces:**
- `PedidoDeGeracaoDeVideoViaMcpNaColecao.MCP_TOPVIEW = "topview"`
- `MODELO_PADRAO_DA_COLECAO_TOPVIEW = "seedance-2-5"`
- `Catalogo…displayNameDe(id)`, `jsonDosItensParaPonte()`, `resolucaoInteira("480p") → 480`
- `ExtracaoDaUrl…taskId(json)`, `urlDoVideo(json)`, `concluida(json)`, `falhou(json)`
- `ExtracaoDoFileId…fileId(json)`
- `GeracaoDeVideoPeloTopviewNoChat.SEM_SESSAO`, `MODELO_PADRAO`, `resolverModelo(idOuNome)`
- `LiberacaoDoTopviewParaOChat.ehPedidoTopview` / `segueSemPerguntar` para `GerarVideoTopview`

- [ ] **Step 1:** Testes falhando — pedido aceita `mcp:topview`; `okMcps` contém topview; catálogo default Seedance 2.5; fixtures parse de taskId/url; liberação.

- [ ] **Step 2:** Implementar mínimos até testes passarem.

```kotlin
// Pedido — acrescentar:
const val MCP_TOPVIEW = "topview"
val MCPS_CONHECIDOS = setOf(MCP_HIGGSFIELD, MCP_SJINN, MCP_TOPVIEW)
const val MODELO_PADRAO_DA_COLECAO_TOPVIEW = "seedance-2-5"
fun modeloPadraoPara(mcp: String) = when (mcp) {
    MCP_HIGGSFIELD -> MODELO_PADRAO_DA_COLECAO_HIGGSFIELD
    MCP_TOPVIEW -> MODELO_PADRAO_DA_COLECAO_TOPVIEW
    else -> MODELO_PADRAO_DA_COLECAO_SJINN
}
// okMcps: incluir {"id":"topview","nome":"Topview"}
```

- [ ] **Step 3:** Rodar  
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "*Topview*" --tests "*PedidoDeGeracaoDeVideo*"`  
Expected: PASS

---

### Task 2: Sessão + Cliente MCP Topview (text-to-video)

**Files:**
- Create: `app/.../SessaoDaContaTopviewNoAparelho.kt`
- Create: `app/.../ClienteMcpDoTopviewParaGeracaoNoChromebrothers.kt`
- Espelhar estrutura de `SessaoDaContaHiggsfieldNoAparelho` / `ClienteMcpDoHiggsfield…` (OAuth register/authorize/token, MCP streamable HTTP)

**Produces:**
- `Sessao…temSessao()`, `gravarTokens`, `apagar`, `topviewLiberado` / `definirTopviewLiberado`
- `ligarConta(activity, aoFim)` — redirect `http://127.0.0.1:8788/callback`
- `gerarVideo(prompt, modelo, aspectRatio, resolution, duracao, refs: List<Pair<ByteArray,String>>): ResultadoDaGeracaoMcpHiggsfield`
- Interno: `tools/call` em `topview_text_to_video_submit` / `_query` (nomes confirmados via `tools/list` na 1ª sessão real; aliases se o server divergir)
- Poll ~4s, timeout ~10 min; download URL → bytes

- [ ] **Step 1:** Sessão EncryptedSharedPrefs `sessao_da_conta_topview`
- [ ] **Step 2:** Cliente OAuth + `gerarVideo` text-only (refs empty)
- [ ] **Step 3:** Compile `:app:compileDebugKotlin` — sem erros

---

### Task 3: Upload + image-to-video + serviço coleção

**Files:**
- Modify: `ClienteMcpDoTopview…` — upload base64 + image_to_video
- Modify: `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt` — injetar sessão/cliente Topview; `listarModelosDeVideoJson`; `gerarVideoEAgregar` ramo + `gerarVideosTopviewEmParalelo`

**Interfaces:**
- Consome: `Catalogo…`, `Cliente…gerarVideo`, `ResultadoDaGeracaoMcpHiggsfield`, `gravacao.guardarVideoComMetadados`
- Produz: paralelismo igual Sjinn (N threads × `gerarVideo`)

- [ ] **Step 1:** Upload → fileId(s); 1 ref = `firstFrameFileId`; 2+ = `referenceImageFileIds` quando o modelo/tool aceitar (senão first frame = primeira)
- [ ] **Step 2:** Serviço: sessão Topview; listar catálogo estático; when topview
- [ ] **Step 3:** HTML teste — `okMcps`/pedido já cobrem; ajustar HTML só se default JS hardcoded sem topview

---

### Task 4: Chat — UI engrenagem + tool + liberação

**Files:**
- Modify: `layout_atividade_principal_do_navegador_chromebrothers.xml` — bloco Topview (rótulo + Ligar/Desligar/Revogar) junto ao Higgsfield
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — wiring espelho Higgsfield
- Modify: `AcoesConfirmadas…`, `FerramentasDeLeitura…`, parsers de tool call / `PedidoDeAcaoConfirmadaDoChat.GerarVideoTopview`

**Produces:**
- Tool `gerar_video_topview` (prompt + modelo opcional); chat MVP sem refs obrigatórias (prompt only)
- Confirmar/Liberar/Cancelar via `LiberacaoDoTopviewParaOChat`

- [ ] **Step 1:** Sealed pedido + schema tool + testes JVM se houver parser
- [ ] **Step 2:** Layout + ligar/desligar/desenhar estado
- [ ] **Step 3:** Executar geração no fluxo de ação confirmada → `clienteTopview.gerarVideo` → gravar biblioteca

---

### Task 5: Verificação

- [ ] `& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug`
- [ ] `adb install -r app\build\outputs\apk\debug\app-debug.apk`
- [ ] Smoke manual: OAuth Topview; text-to-video coleção; 1 ref; chat Confirmar

## Spec coverage

| Spec | Task |
|------|------|
| OAuth MCP + porta 8788 | 2 |
| Catálogo estático Seedance 2.5 | 1 |
| Coleção 0/1+ refs + qty | 3 |
| Chat + liberação | 4 |
| Fora de escopo marketing/imagem | — (não criar) |
| Testes + install | 5 |
