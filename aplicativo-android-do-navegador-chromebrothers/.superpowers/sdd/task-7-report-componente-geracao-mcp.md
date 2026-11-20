# Task 7 — Ponte JS + pickers + seed (relatório)

Status: DONE_WITH_CONCERNS

## Arquivos modificados
- `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`: métodos `@JavascriptInterface` `listarMcpsGeracao`, `listarModelosGeracao`, `escolherImagemGeracao`, `limparImagemGeracao`, `gerarImagensMcp`; ligações `ligarServicoDeGeracaoDeImagemViaMcp`, `ligarEscolherImagemDeReferenciaDaGeracao`, `ligarAoTerminarGeracaoDeImagemViaMcp`; `entregarImagemDeReferenciaDaGeracao(json)` (público) e helpers de `evaluateJavascript` com `JSONObject.quote`.
- `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`: repassa as três ligações a todas as janelas (existentes e novas, via `aplicarPontes`).
- `AtividadePrincipalDoNavegadorChromebrothers.kt`: serviço (`by lazy`), seed, picker de galeria (`GetContent("image/*")`), diálogo da biblioteca, wiring.

## Comportamento
- **Seed**: após `carregar()`, se `buscar("geracao-mcp-imagem") == null` → `guardar(..., novoId = { "geracao-mcp-imagem" })` + `gravar`. Nunca sobrescreve se o id existe (sem alterar `guardar`).
- **listarModelosGeracao**: síncrono. O `@JavascriptInterface` roda na thread da ponte do WebView (não na UI), então a rede bloqueante é segura (mesma lógica do `listarPasta`, mas sem precisar de latch).
- **escolherImagemGeracao**: valida origem; retorna `{ok:true,aguardando:true}` imediatamente e pede ao Activity na UI.
  - galeria: `GetContent`; leitura em Thread (limite 20 MB), mime via `contentResolver`, nome via `OpenableColumns`.
  - biblioteca: `AlertDialog` com até 100 itens (nome exibido ou data, ★ se favorito); `ler(nomeDoArquivo)` em Thread, mime `image/jpeg`.
  - Em ambos: gera `previewDataUrl`; se vazio → erro e **não** define referência; senão `definirReferencia` + callback JS `__chromebrothersImagemGeracao({ok,previewDataUrl,nome})`. Cancelamento → `{ok:false,erro:"Nenhuma imagem escolhida."}`.
- **gerarImagensMcp**: valida com `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler` (recusa síncrona); guarda contra geração concorrente (`AtomicBoolean`); inicia `gerarEmBackground` e retorna `okAndamento()`. `aoFim` → `__chromebrothersGeracaoMcp(JSONObject.quote(json))` na UI e `sincronizarBibliotecaDeVideos()` (recarrega a biblioteca de imagens em memória, já que o serviço grava direto no disco).
- **Adaptação de progresso**: o serviço emite `{andamento:true,atual,total}`, mas o HTML só entende `{progresso:"texto"}` (senão trataria como final e reabilitaria o botão). A ponte converte para `{ok:true,progresso:"Gerando imagem n de N…"}`. HTML não foi alterado.

## Build / instalação
- `gradle.bat assembleDebug` → BUILD SUCCESSFUL.
- `adb install -r` no dispositivo `RX2T7007XMD` → Success.
- Sem teste unitário novo (código dependente de Android/WebView); sem teste automatizado alterado. Sem commit.
- Não validado manualmente no tablet (UI/MCP real com sessão Higgsfield).

## Concerns
1. Referência é única por serviço (compartilhada entre todas as janelas da coleção); a geração concorrente também é bloqueada globalmente por painel (flag por painel, não global entre janelas).
2. `__chromebrothersImagemGeracao` com erro de cancelamento aparece em vermelho no status do HTML (o HTML não limpa "Aguardando…" de outra forma).
3. `listarModelosGeracao` bloqueia o JS da página enquanto a rede responde (carregado em `DOMContentLoaded`); sem timeout próprio além do OkHttp do cliente.
4. Componente semeado só entra se o id não existir; instalações com versão antiga do HTML (já semeadas) não são atualizadas por design.
5. Se a janela fechar durante a geração, o resultado ainda é gravado na biblioteca, mas o callback JS é descartado (`encerrada`).
