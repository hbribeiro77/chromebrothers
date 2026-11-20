# Task 3 — HTML empacotado do componente (relatório)

Status: concluído (sem commit).

## Arquivos
- `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt` — `ID="geracao-mcp-imagem"`, `NOME="Geração MCP — imagem"`, `TAGS=[mcp, higgsfield, imagem]`, `html()`.
- `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.kt` — 2 testes (kotlin.test, como os demais do módulo; o brief usava JUnit, não resolve no módulo).

## TDD
- Vermelho: teste falhou por compilação (objeto inexistente).
- Verde: `BUILD SUCCESSFUL`, 2 testes, 0 falhas.

## HTML
- Uma coluna, fundo `#f6f7f9`, texto `#1a1a1a`, sem roxo, labels em português.
- Ordem: MCP, Prompt, Imagem (preview + Galeria/Biblioteca/Limpar), Modelo, Gerações 1–4, Gerar, `#status`, `#grade`.
- Carrega MCPs/modelos no `DOMContentLoaded`; troca de MCP recarrega modelos; erro inline.
- `gerar()`: valida prompt, clamp 1–4, chama `gerarImagensMcp`; se `ok` desabilita botão e "Gerando…"; resultado via `window.__chromebrothersGeracaoMcp(json)` (append na grade, reabilita Gerar, erro/parcial mostrados).
- `window.__chromebrothersImagemGeracao(json)` atualiza preview com `previewDataUrl`/`uri`.
- Toque em item com `fileUrl` chama `chromebrothers.abrirImagemNoVisor`.
- Imagens montadas via DOM (sem innerHTML com dados externos); sem `$` no JS (raw string Kotlin).

## Observações
- Variável de status nomeada `elStatus` (evita colisão com `window.status`).
- `__chromebrothersGeracaoMcp` aceita também `{progresso:"1/N…"}` para atualizar status sem reabilitar Gerar (extensão opcional).
- Comportamento no WebView real não testado (só teste de string); validar no tablet.

## Correção (review Important): temRef com `aguardando`
- Problema: `aplicarImagem` definia `temRef=true` para qualquer `{ok:true}`, inclusive `{ok:true,aguardando:true}` antes de o usuário escolher.
- Correção em `aplicarImagem`: se `r.aguardando`, só mostra "Aguardando a escolha da imagem…" e retorna (temRef intacto); sem `previewDataUrl`/`uri`, retorna sem marcar; só então `temRef=true`, atualiza preview/nome e limpa status. Vale para retorno síncrono e para o callback `__chromebrothersImagemGeracao`.
- `limpar()` continua com `temRef=false` (agora coberto por teste).
- Novo teste `referenciaSoValeComPreviewEFimDaEspera` (verifica ordem aguardando/preview antes de `temRef = true` e `temRef = false` em limpar).
- Resultado: `TesteDoHtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao` — BUILD SUCCESSFUL, 3 testes, 0 falhas. Sem commit.
