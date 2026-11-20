# Review — Task 1: parse do pedido e JSON de resposta

Gate da tarefa, não revisão de merge. Fonte: brief, relatório e pacote (arquivos novos). Árvore de trabalho não foi alterada. Suíte completa não foi reexecutada. O XML já gravado confere o GREEN: `tests="2"` `failures="0"` `errors="0"` `time="0.045"`.

## Conformidade com o brief

**Sim.**

- Arquivos pedidos, nomes longos em pt-BR, só regras puras, sem Android e sem commit.
- Contrato público: `PedidoDeGeracaoDeImagemViaMcpNaColecao`, `ResultadoDoPedidoDeGeracaoDeImagemViaMcp.Ok/Recusado`, `ler(json)`, `RespostaJsonDaPonteDeGeracaoMcp.erro/okAndamento/okMcps/okLimpar`.
- Os dois testes do brief estão iguais, e o GREEN bate com eles.
- Regras de `ler` iguais às do brief: prompt vazio, limite 4000 via `LIMITE_DO_PROMPT`, MCP fora de `higgsfield`, quantidade fora de 1..4; default de MCP em branco = `higgsfield`, quantidade ausente = 1, modelo ausente = `""`.
- YAGNI: nenhum arquivo existente mudou; reuso de `TextoJsonDoChat` e do limite já existente.

**Desvio justificado:** o brief declara `data class` e `object` com o mesmo nome no topo. Kotlin não compila isso. `ler`, `MCP_HIGGSFIELD` e `MCPS_CONHECIDOS` foram para o `companion object`. A chamada `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler(json)` e o tipo do pedido continuam os do teste. Melhoria necessária, não fuga de escopo.

**TDD:** o RED foi falha de compilação (`Unresolved reference`), não de asserção. Com tipos novos, é o FAIL que o passo 2 do brief produz. O relatório não disfarça isso.

## Pontos fortes

- Escopo fechado no parse do pedido de imagem e nos quatro JSON da ponte.
- Companion object preserva a API que as tarefas seguintes vão chamar.
- Ordem de recusa e mensagens batem com o código do brief.
- `erro` passa por `TextoJsonDoChat.escapar` (aspas, barra, quebras e controles `< 0x20`).
- O produtor previsto na task 3 é `JSON.stringify` sem espaço, e o caso do brief `"temReferencia":true` passa.
- Relatório honesto sobre o que o teste não trava.

## Problemas

### Crítico

Nenhum.

### Importante

Nenhum. Nada aqui impede a task 2.

### Menor

1. **`temReferencia` booleano é um `contains` frágil**
   - Arquivo: `PedidoDeGeracaoDeImagemViaMcpNaColecao.kt:19-20`
   - `TextoJsonDoChat.valorDaChave` só lê string entre aspas. O booleano cai em `json.contains("\"temReferencia\":true")`.
   - `"temReferencia": true` (espaço, como no exemplo pretty-print do design) vira `false`. Um `true` compacto em outro ponto do texto também acende a flag.
   - Por que não sobe de nível: está copiado do brief, e o HTML planejado usa `JSON.stringify` compacto (`"temReferencia":true`). O caminho da v1 funciona.
   - É defeito do plano, executado fielmente. Não corrigir nesta tarefa se a correção mudar o código prescrito. Se for endurecer depois, ler o token `true`/`false` com espaço permitido (há um `booleanoDaChave` privado em `IndiceDasConversasDoChatDaOpenRouter`, não reutilizável daqui).

2. **Helpers de resposta sem asserção**
   - Arquivo: `PedidoDeGeracaoDeImagemViaMcpNaColecao.kt:40-46` e `TesteDoPedidoDeGeracaoDeImagemViaMcpNaColecao.kt`
   - `erro`, `okAndamento`, `okMcps` e `okLimpar` compilam e copiam o brief, mas nenhum teste trava o JSON.
   - O passo de teste desta tarefa não pedia isso. As strings estão certas para o contrato (`ok`/`erro`/`itens`/`andamento`). Anotar; não bloquear.

3. **O teste do brief não trava mensagem, modelo nem o limite de 4000**
   - `exigePromptEQuantidadeEntre1e4` só exige `Recusado` no prompt vazio e na quantidade 5, e no sucesso só `mcp`, `quantidade` e `temReferencia`.
   - O limite e o default de MCP/quantidade estão no `ler` e não neste arquivo de teste. O teste pedido foi entregue; a folga é do próprio brief.

## Recomendações

- Seguir. Não reabrir o companion object.
- Não expandir o parser nesta tarefa. O `contains` fica como limitação conhecida do plano até alguém mudar o brief.

## Avaliação

**Pode seguir para a próxima tarefa?** Sim.

**Motivo:** a API, as recusas e os JSON da ponte cumprem o brief; o único desvio estrutural é o companion object, obrigatório para compilar. Os achados menores já estavam no código prescrito ou fora do teste exigido.
