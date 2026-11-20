# Task 6 — Review (Serviço facade de geração via MCP)

**Spec: ✅**  |  **Quality: Approved** (com observações menores, nenhuma bloqueante)

Verificado: leitura integral de `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`,
diff conceitual de `ClienteMcp…` (`listarModelosDeImagemBruto`), assinaturas em `regras-de-video-do-navegador`
e `compileDebugKotlin --offline` (exit 0, reexecutado na revisão).

## Spec (brief + globais)

| Requisito | Resultado |
|---|---|
| `listarMcpsJson()` | ✅ `RespostaJsonDaPonteDeGeracaoMcp.okMcps()` |
| `listarModelosJson(mcpId)` | ✅ valida MCP, `SEM_SESSAO`, bruto → `jsonDosItensParaPonte` |
| `definirReferencia(bytes,nome,mime)` / `limparReferencia()` / `temReferencia()` | ✅ |
| `gerarEmBackground(pedidoJson, aoAndamento, aoFim)` em Thread | ✅ (callbacks fora da main, documentado) |
| Loop com `quantidade = 1` por chamada | ✅ `for (n in 1..pedido.quantidade)` → `gerarImagem(..., quantidade = 1)` |
| `SEM_SESSAO` | ✅ checado antes do loop (`GeracaoDeImagemEVideoPeloHiggsfieldNoChat.SEM_SESSAO`) e em `listarModelosJson` |
| Itens parciais em falha no meio | ✅ `Erro` ou falha ao salvar → `jsonFinal(itens, msg)` com `ok:false` + itens já gerados |
| Preview JPEG ≤320px, Base64 NO_WRAP, `data:image/jpeg;base64,` | ✅ `inSampleSize` + `createScaledBitmap`, q80 |
| JSON final `{ok, itens:[{idBiblioteca, previewDataUrl}], erro?}` | ✅ escape via `TextoJsonDoChat.escapar` |
| Pedido recusado / sem referência quando `temReferencia` | ✅ erro com `itens:[]` |
| YAGNI | ✅ sem fila, cancelamento, cache ou retry |

### `listarModelosDeImagemBruto` no cliente
Aceitável e necessário: `listarModelos()` devolve texto já formatado, e `jsonDosItensParaPonte`
precisa do bruto. O método é mínimo (list `type=image`), lança erro legível, não altera `listarModelos`.
Duplica ~5 linhas de chamada/validação de `listarModelos` — tolerável (refatorar seria escopo extra).
O `error(SEM_SESSAO)` interno é redundante com o check do serviço, mas inofensivo (defesa para outros chamadores).

## Quality — observações (não bloqueantes)

1. **Perda de parciais em exceção inesperada** (Minor): em `gerarEmBackground`, o `catch (Throwable)` devolve
   `jsonFinal(emptyList(), …)`. Se `gravacao.guardarImagemComMetadados` (IO) ou `aoAndamento` lançar, os itens já
   salvos na biblioteca somem do JSON (as imagens ficam gravadas, mas a UI não as mostra). `gerarImagem` já captura
   `Exception` internamente, então o risco é pequeno. Correção simples se quiser: manter `itens` fora de `gerarEAgregar`
   (ou try/catch dentro do loop) e usar no catch.
2. **`mime` guardado mas não usado** (Minor): o cliente deduz content-type pela extensão do `nome`. Se a Task 7 passar
   nome sem extensão com mime PNG, subirá como `image/jpeg`. Sugestão para Task 7: garantir extensão coerente no `nome`.
3. **`previewDataUrl` público** (Nit): poderia ser `private`/`internal`; sem impacto.
4. **`jsonAndamento` próprio vs `okAndamento()`** (Nit): `RespostaJsonDaPonteDeGeracaoMcp.okAndamento()` não é usado
   aqui; o serviço emite `{atual,total}`, que é mais útil. Apenas note que a Task 7 deve tratar este formato.
5. **Sem guarda de concorrência** (YAGNI aceito): dois `gerarEmBackground` simultâneos compartilham `referencia` e
   `mcpSessionId`/`rpcSeq` do cliente (`garantirSessaoMcp` reseta por iteração). A UI da Task 7 deve desabilitar o botão
   durante a geração.
6. `listarModelosJson` pode devolver `itens:[]` com `ok:true` se o bruto não tiver `items` (não verificado contra MCP
   real; o relatório já declara). A Task 7/UI deve tolerar lista vazia.
7. Sem teste unitário (usa `Bitmap`/Android) — coerente com a regra de teste; lógica pura já testada nas regras.

## Veredito
Spec ✅ — Quality **Approved**. Observação 1 é a única que vale ajuste opcional; pode seguir para a Task 7.
