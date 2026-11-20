# Task 8 — Revisão crítica do relatório de verificação E2E (Geração MCP — imagem)

**Spec: ✅ (6/6 itens atendidos, 2 com ressalvas)**
**Qualidade: Approved (com concerns registrados, nenhum bloqueante)**

Revisão das evidências (relatório + 11 screenshots `t8_*.jpg`), não do código. Consultei só `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt` para resolver uma ambiguidade do item 2.

## Checklist do spec

| # | Item | Veredito | O que a evidência mostra |
|---|------|----------|--------------------------|
| 1 | Componente "Geração MCP — imagem" na biblioteca | ✅ | `t8_2.jpg`: aba Componentes, primeiro item, tags mcp/higgsfield/imagem. `t8_6.jpg`: janela aberta com MCP, Prompt, Referência (Galeria/Biblioteca/Limpar), Modelo, Gerações e Gerar. |
| 2 | Sem Higgsfield → erro claro ao listar/gerar | ✅ (ressalva) | `t8_28.jpg`: o select mostra "Erro: Ligue o Higgsfield na engrenagem do chat." O prompt "teste sem sessao" está preenchido e a mensagem vermelha aparece abaixo de Gerar. A mensagem é igual nas duas chamadas, então o screenshot não separa as duas. O código fecha a lacuna: `listarModelosJson` (linha 36) e `gerarEAgregar` (linha 112) retornam `SEM_SESSAO` antes de qualquer rede. A sessão foi restaurada e `t8_31.jpg` mostra o select preenchido de novo. |
| 3 | Com sessão: listar modelos preenche select | ✅ | `t8_6.jpg` e `t8_31.jpg` mostram "Higgsfield Soul 2.0" no select. O relatório cita o catálogo completo, mas nenhum screenshot mostra o dropdown aberto. Isso é aceitável, porque o select preenchido já prova a listagem. |
| 4 | Gerar 1 sem ref → grade + biblioteca | ✅ | `t8_10.jpg`: "Pronto: 1 imagem(ns) salva(s) na biblioteca." com miniatura da maçã. `t8_26.jpg`: entrada de 23:34 em Imagens. |
| 5a | Gerar com ref (biblioteca) | ✅ | `t8_16.jpg` e `t8_18.jpg`: ref `quadro-….jpg` com miniatura, depois "Pronto: 1 imagem(ns)…" com 2 miniaturas na grade, porque ela acumula. `t8_26.jpg`: entrada de 23:40. O log citado (upload, PUT 200, confirm, `generate_image` com `medias`) é coerente. |
| 5b | Gerar com ref (galeria) | ✅ via JPEG (concern PNG) | `t8_22.jpg`: ref `1000043785.jpg` vinda da galeria, com miniatura. O fluxo completo passou com JPEG. O PNG (`t8_13.jpg`, `t8_14.jpg`) falhou no `media_confirm` do servidor, e o erro apareceu em vermelho sem quebrar a UI. Pelo critério da tarefa, o caminho JPEG passou, então não reprovo. |
| 6 | Quantidade 2 → dois itens | ✅ | `t8_22.jpg`: Gerações = 2 e "Pronto: 2 imagem(ns) salva(s)…". `t8_26.jpg`: duas entradas de 23:43. O código confirma um loop `for (n in 1..quantidade)` com `quantidade = 1` por chamada, ou seja, o fallback de 2 jobs. |

## Pontos críticos que encontrei

1. **Item 6, a grade não prova os dois itens novos.** A grade acumula, e em `t8_22.jpg` as duas miniaturas visíveis são provavelmente as maçãs de gerações anteriores. As duas novas estão cortadas fora da janela. A evidência real dos dois itens é a biblioteca em `t8_26.jpg`, com duas entradas de 23:43. Isso basta para passar, mas o relatório cita `t8_22.jpg` como prova da grade, e ele não prova.
2. **As duas entradas de 23:43 parecem cópias da imagem de referência.** Na biblioteca aparece o retrato pixel-art "CONTINUE? 9", idêntico à ref da galeria, apesar do prompt "a red apple on a white table". O código salva `resultado.bytes`, que é a saída gerada e não a ref, então não é bug de gravação. É provável que o Soul copie a referência quando ela tem peso alto. O relatório não comenta isso. Vale uma olhada manual se isso for um problema de produto. Para o checklist (existem 2 itens salvos via 2 jobs), não reprova.
3. **Item 2 sem como separar listar e gerar.** O relatório admite isso. O código cobre os dois caminhos, então aceito.
4. **O relatório cita logcat, mas não anexa nenhum.** Os trechos de log (`PUT referencia HTTP 200`, `Confirmed 1 upload`, `generate_image …`) não têm arquivo de evidência. Fica uma alegação sem prova auditável.
5. **Recorte da UI.** A janela abre parcialmente fora da tela e pequena. Nos screenshots, a grade de resultados fica cortada embaixo (`t8_10.jpg`, `t8_22.jpg`). É um concern de usabilidade, não falha de função.
6. **Falha do PNG não investigada.** Só uma tentativa. Pode ser do servidor, ou o app pode estar mandando MIME/bytes errados para PNG. Isso merece follow-up. Uma sugestão é testar outro PNG, ou converter para JPEG antes do upload.
7. **Item 5b não tem um "Gerar 1 com ref de galeria" isolado.** A galeria só foi exercitada com JPEG na rodada com quantidade 2. Isso cobre o caminho, mas não o isola.
8. **Higiene.** A sessão foi restaurada (`cmp` idêntico) e o backup foi apagado, o que é bom. Foram gastos créditos reais (4 jobs). O relatório documenta tudo e não vaza token.

## Decisão

- **Spec ✅.** Os 6 itens têm evidência visual no tablet, ou código que cobre a lacuna (item 2). As falhas e concerns (PNG, recorte da UI, grade cortada) não derrubam o checklist, porque o caminho JPEG e os demais itens passaram.
- **Quality: Approved**, com status DONE_WITH_CONCERNS mantido.

## Follow-ups recomendados (não bloqueantes)

1. Testar outro PNG da galeria. Se falhar de novo, converter para JPEG antes do upload.
2. Corrigir o posicionamento inicial da janela flutuante: limitar à tela e usar um tamanho padrão maior.
3. Anexar o logcat filtrado (`Higgsfield|GeracaoMcp|colecao|SEM_SESSAO`) como arquivo de evidência.
4. Registrar um screenshot que mostre os dois resultados novos do item 6 na grade, rolando a janela.
