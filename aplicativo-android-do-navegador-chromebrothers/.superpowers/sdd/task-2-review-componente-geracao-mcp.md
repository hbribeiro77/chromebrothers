# Review — Task 2: catálogo JSON para a ponte

Gate da tarefa, não revisão de merge. Fonte: brief, relatório, pacote (arquivos completos) e o call site já existente em `ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.listarModelos`. Árvore de trabalho não foi alterada. Suíte completa não foi reexecutada. O XML citado no relatório confere o GREEN: `tests="5"` `failures="0"` `errors="0"`.

## Conformidade com o brief

**Sim.**

- Arquivos pedidos, só regras puras, sem commit.
- `itensParaSelect(bruto): List<Pair<String, String>>` reusa `extrairItens` (continua privado). `first` = id, `second` = `name`. Item sem id continua fora.
- `jsonDosItensParaPonte` monta `{"ok":true,"itens":[{"id":"…","nome":"…"},…]}` com `TextoJsonDoChat.escapar` em id e nome.
- O teste do brief está igual. O caso `soul_2` / `Soul` passa por `contains`.
- YAGNI: nenhuma função extra, nenhum campo além de `id` e `nome`. O `…` do design (`itens:[{ id, nome, … }]`) não foi preenchido aqui; a task 2 não pedia.

**Desvio justificado:** o brief escreve `List<Pair<id,nome>>`. Em Kotlin o tipo é `List<Pair<String, String>>`. A chamada e a ordem do par são as do contrato.

**TDD:** RED foi falha de compilação (`Unresolved reference 'jsonDosItensParaPonte'`), o FAIL esperado para método novo. GREEN no mesmo teste, com os quatro testes antigos ainda verdes. O relatório não disfarça o RED.

## Filtro de imagem (pedido explícito)

**Menor. Aceitável neste call site. Não é Crítico nem Importante.**

`itensParaSelect` e `jsonDosItensParaPonte` devolvem todo item com id, inclusive `output_type=video`. `formatarCatalogo` já descarta vídeo no filtro vazio/`image`/`imagem`. O brief desta tarefa não pede esse filtro, e o teste só tem um item de imagem.

O catálogo “type image” do design é propriedade da chamada MCP, não deste parser. `listarModelos` já envia `models_explore` com `type=image` quando o filtro está vazio ou é `image`/`imagem` (`ClienteMcpDoHiggsfieldParaGeracaoNoChatDoChromebrothers.kt`, ramo `action=list`). A task 6 (`listarModelosJson`) deve usar esse mesmo caminho. O bruto que chega na ponte da geração de imagem costuma ser só imagem. Filtrar de novo aqui duplicaria a política do chat e sairia do YAGNI do brief.

Risco residual, conhecido: se o MCP ignorar `type` e misturar vídeo no JSON, o select da coleção mostraria modelo de vídeo. Isso se observa no tablet (task 8), não se antecipa nesta função. Endurecer depois só se o bruto real vier misto — aí o filtro de `formatarCatalogo` (`outputType` vazio ou `image`) cabe em `itensParaSelect`, com teste que inclui `seedance`/`video` e exige ausência.

## Pontos fortes

- Escopo fechado: duas funções públicas em cima do extrator que já existia.
- Escape real (`"`, `\`, quebras, controles `< 0x20`) via `TextoJsonDoChat.escapar`, o mesmo helper da task 1.
- Ordem do array `items` preservada. Sem `items` ou com array inválido, o JSON é `{"ok":true,"itens":[]}` — erro de sessão/rede fica no serviço, como o brief desenha.
- Relatório honesto sobre o que o teste não trava.

## Problemas

### Crítico

Nenhum.

### Importante

Nenhum. Nada aqui impede a task 3. O filtro de imagem ausente não entra neste nível.

### Menor

1. **Teste do brief não rejeita vídeo nem trava o JSON exato**
   - Arquivo: `TesteDaListagemDeModelosDoHiggsfieldNoChat.kt` (`jsonDosItensParaPonteExtraiIdENome`)
   - `contains("\"ok\":true")`, `contains("soul_2")` e `contains("Soul")` passam mesmo se um item de vídeo entrar ou se a forma do objeto mudar.
   - Por que não sobe de nível: é o teste prescrito. A folga é do plano. Não acrescentar asserção fora do brief nesta tarefa.

2. **`itensParaSelect` sem asserção própria**
   - Só é exercido por dentro de `jsonDosItensParaPonte`. O par `(id, name)` está correto no código lido. Anotar; não bloquear.

3. **Nome em branco vira `"nome":""`**
   - Arquivo: `ListagemDeModelosDoHiggsfieldNoChat.kt` (`itensParaSelect` / `jsonDosItensParaPonte`)
   - `formatarCatalogo` omite nome em branco na linha de texto; o JSON da ponte não faz fallback para o id.
   - O brief pede o campo `name` serializado como `nome`. Select com rótulo vazio é incômodo de UI, não quebra o contrato. Tratar na task 3 se o HTML precisar de rótulo.

## Recomendações

- Seguir para a task 3.
- Na task 6, chamar `models_explore` com `type=image` e passar o bruto a `jsonDosItensParaPonte` sem filtro extra.
- Não reabrir o parser por causa de vídeo até um bruto real misturar `output_type`.

## Avaliação

**Pode seguir para a próxima tarefa?** Sim.

**Spec:** cumprido.

**Qualidade:** aprovada.

**Motivo:** a API pública, o formato `ok`/`itens`/`id`/`nome` e o escape cumprem o brief; o RED/GREEN está documentado. A ausência de filtro de imagem é defesa duplicada em relação ao `type=image` do call site, não um furo do contrato desta tarefa.
