# Task 3 — Save MCP com origem, modelo, prompt e refs — Relatório

**Status:** DONE. Commits: nenhum (a pasta nem é repositório git).

## Alterações

1. `regras-de-video-do-navegador/.../BibliotecaDeImagensCapturadasDoNavegador.kt`
   - Novo `atualizarMetadadosDeGeracao(id, origem?, modelo?, prompt?, refs?, caminhoLocal?)`: atualiza no lugar (mantém posição/instante/favorito), origem passa por `OrigemDaMidiaNaBibliotecaDoNavegador.parse`, args nulos preservam o valor anterior, devolve `null` se o id não existe. Defaults compatíveis.
   - Teste novo em `TesteDaBibliotecaDeImagensCapturadasDoNavegador.kt` (`atualizarMetadadosDeGeracaoMantemPosicaoEPreservaCamposNulos`).
2. `app/.../GravacaoDaMidiaGeradaPeloHiggsfieldNaBibliotecaDoChromebrothers.kt`
   - `guardarVideoComMetadados(bytes, prompt, origem, modelo = "", refsBytes = emptyList())`: chave `"$origemNormalizada-$uuid"`, pasta continua `biblioteca-de-videos-higgsfield`, `titulo = prompt.take(80)` (fallback "Vídeo gerado"), repassa origem/modelo/prompt completo/refs/caminhoLocal (absoluto do mp4) ao `registrar`.
   - `guardarImagemComMetadados(...)` mesma assinatura: após `guardarQuadro`, chama `atualizarMetadadosDeGeracao` (caminhoLocal = jpeg absoluto) e regrava a biblioteca.
   - Refs copiadas para `filesDir/refs-de-geracao-da-biblioteca/<chave>-ref-<i>.<ext>`; a lista `refs` guarda só os nomes dos arquivos. Extensão: do nome (2º do par) ou do mime se começar com `image/`; padrão `jpg`.
   - Wrappers `guardarImagem` / `guardarVideo`: `origem` com default `higgsfield`, `modelo` e `refsBytes` opcionais.
3. `app/.../ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`
   - Imagem: origem = `pedido.mcp`; modelo = `pedido.modelo` (sjinn) ou `resolverModeloImagem` (higgsfield); refs = `listaRefs`.
   - Vídeo higgsfield / sjinn / topview: origem = id do MCP, modelo resolvido (`modelo` já resolvido no higgsfield; `pedido.modelo` já mapeado por `modeloLocalParaMcp` nos outros), refs = `listaRefs`.
4. `app/.../PainelDoChatDaOpenRouterNoNavegador.kt` (chat)
   - Imagem Higgsfield: origem higgsfield, modelo resolvido, ref (anexo/biblioteca) se houver.
   - Vídeo Higgsfield: origem higgsfield + modelo resolvido. Vídeo Topview: origem topview + `GeracaoDeVideoPeloTopviewNoChat.resolverModelo`. Refs vazias. Import de `OrigemDaMidiaNaBibliotecaDoNavegador` adicionado.

## Verificação
`gradle :regras-de-video-do-navegador:test :app:compileDebugKotlin` → BUILD SUCCESSFUL (testes regras passam, compile OK).

## Preocupações / notas
- Imagens: `guardarQuadro` já grava o JSON uma vez sem metadados e depois regrava com metadados (duas escritas; janela mínima de inconsistência).
- Chave de vídeo mudou de `higgsfield-<uuid>` para `<origem>-<uuid>` (esperado pelo brief); itens antigos mantêm a chave antiga.
- O chat Sjinn não gera mídia por esse caminho (só Higgsfield/Topview), então não há call site de chat para sjinn.
- Refs que falham ao copiar são omitidas da lista silenciosamente.

## Correção de corrida no índice JSON

`guardarImagemComMetadados` e `guardarVideoComMetadados` faziam `carregar()` + mutação + `gravar()` sem sincronização. O serviço gera vídeos em threads paralelas e podia perder atualizações do índice.

- `companion object` com `travaDaBibliotecaGerada` (compartilhada entre instâncias).
- Corpo inteiro de `guardarImagemComMetadados` e, em `guardarVideoComMetadados`, tudo depois do early return de `bytes.isEmpty()`, dentro de `synchronized(travaDaBibliotecaGerada)`.
- `titulo = prompt.take(80).ifBlank { "Vídeo gerado" }` mantido.
- Sem teste de instrumentação Android. Sem commit.
- `:app:compileDebugKotlin` → BUILD SUCCESSFUL (7s).
