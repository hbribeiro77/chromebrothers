# Review — Task 4: Cliente MCP — `count` + múltiplas URLs

Gate da tarefa, não revisão de merge. Fonte: brief, relatório e o arquivo `ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.kt`. Conferidos `gerarImagem`, `gerar`, o PUT da referência e `ResultadoDaGeracaoMcpHiggsfield.Pronto`. Chamadas existentes em `diagnosticarGeracaoComReferenciaDeTeste` e em `PainelDoChatDaOpenRouterNoNavegador.kt` lidas para ver se o parâmetro novo quebra o chat. `assembleDebug` não foi reexecutado nesta revisão; o relatório registra BUILD SUCCESSFUL.

## Conformidade com o brief

**Sim.** O mínimo do brief está no fonte.

- **Step 1.** Em `gerar`, depois de `aspect_ratio`, se `imagem && quantidade > 1`: `params.put("count", quantidade.coerceIn(1, 4))` (linhas 299–302).
- **Step 2.** `gerarImagem(..., quantidade: Int = 1)` repassa `quantidade = quantidade` para `gerar` (linhas 86–103). `gerar` declara o mesmo default (linha 279).
- **Step 3.** O relatório marca `:app:assembleDebug` como BUILD SUCCESSFUL. Esta revisão não recompilou.

**`params.count` intacto no caminho pedido.** Vídeo chama `gerar(..., imagem = false)` e não manda `quantidade`, então cai no default `1` e não envia `count`. Quantidade `<= 1` também não envia `count`. O chat atual (`PainelDoChatDaOpenRouterNoNavegador`, por volta da linha 1778) chama `gerarImagem` sem `quantidade`; o default `1` preserva o comportamento de uma imagem.

**`If-None-Match` intacto.** O PUT da referência continua com os dois headers exigidos pela URL assinada (linhas 370–376):

```kotlin
.header("Content-Type", tipoUpload)
.header("If-None-Match", "*")
.put(bytes.toRequestBody(tipoUpload.toMediaType()))
```

O comentário `SignedHeaders=content-type;host;if-none-match` permanece. Nada nesse bloco foi removido nem trocado.

**`Pronto` inalterado.** Continua um job e uma URL: `bytes`, `enderecoRemoto`, `jobId`, `imagem` (linhas 809–815). `esperarUrlDoJob` devolve a primeira URL que `extrairUrlHttp` aceita (`firstOrNull`). Isso é o mínimo do brief: passar `count` e seguir extraindo a primeira URL. O loop interno com fallback não foi feito; o brief trata esse loop como preferência e aceita o mínimo.

**Desvio justificado:** o checkbox do step 1 escreve `params.put("count", quantidade)`. A linha de contrato mínimo do mesmo brief escreve `quantidade.coerceIn(1, 4)`. O código segue o contrato mínimo. Com o `if (quantidade > 1)`, o piso `1` do `coerceIn` não dispara; o teto `4` é o que importa e alinha com a faixa 1–4 da task 1.

## Pontos fortes

- Parâmetro novo no fim, com default `1`. Chat, diagnóstico com referência e vídeo compilam sem passar `quantidade` e não ganham `count` sem querer.
- `count` só entra em imagem e só quando a quantidade passa de 1. Valor acima de 4 vira 4 em vez de ir cru para o MCP.
- Upload da referência (Content-Type, `If-None-Match: *`, PUT, `media_confirm`) ficou de fora da mudança.
- Relatório honesto: várias URLs do mesmo job são descartadas, e avisa para a task 6 não repetir a chamada já passando `quantidade > 1`.

## Problemas

### Crítico

Nenhum.

### Importante

Nenhum.

### Menor

1. **URLs extras do mesmo job continuam descartadas**
   - Arquivo: `ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.kt` (`extrairUrlHttp`, linhas 568–580; `Pronto`, linhas 809–815)
   - `firstOrNull` devolve uma URL. Se o MCP honrar `count` e devolver várias, o cliente baixa só a primeira.
   - É o mínimo escrito no brief, e o relatório já registra. Não bloquear esta task. A task 6 deve obter N imagens com N chamadas em `quantidade` default `1`, sem passar `quantidade > 1` dentro do `repeat`.

2. **Piso do `coerceIn` é código morto**
   - Arquivo: o mesmo, linhas 300–301
   - Dentro de `quantidade > 1`, `coerceIn(1, 4)` só aperta o teto. Não muda o JSON de 2, 3 ou 4. Deixar como está.

## Recomendações

- Seguir para a task 5. Esta task não muda o HTML.
- Na task 6, o loop deve ser `repeat(quantidade) { gerarImagem(...) }` sem o argumento `quantidade`. Cada volta fica em `count` ausente e em um `Pronto`. Passar `quantidade > 1` no loop pede N imagens por volta e guarda uma.
- Não promover `Pronto` para `urls: List<String>` nesta tarefa. O brief já aceitou um `Pronto` por job.

## Avaliação

**Pode seguir para a próxima tarefa?** Sim.

**Spec:** ✅

**Qualidade:** Approved

**Motivo:** `gerarImagem` propaga `quantidade`, `params.count` só entra em imagem com quantidade maior que 1 (teto 4), e o header `If-None-Match: *` do PUT da referência está no mesmo lugar. `Pronto` segue com uma URL, como o mínimo do brief. O risco de multiplicar geração fica para o uso do parâmetro na task 6, já anotado no relatório.
