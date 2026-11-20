# Task 7 — Revisão crítica (Ponte JS + pickers + seed)

**Spec: ✅ (cumpre o brief)**
**Qualidade: Approved (com ressalvas Medium/Low, nenhuma bloqueante)**

Escopo inspecionado: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`, `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`, `AtividadePrincipalDoNavegadorChromebrothers.kt`, `ServicoDeGeracaoDeImagemViaMcpParaAColecaoDoChromebrothers.kt`, `BibliotecaDeComponentesHtmlDaColecaoFlutuanteDoNavegador.kt` e o HTML do componente. Não executei build/instalação nesta revisão (o projeto não tem `gradle.bat` local); o BUILD SUCCESSFUL e o `adb install` são os do relatório.

## Verificações pedidas

### 1. Métodos novos da ponte (`PonteJavascriptDaPaginaDaColecao`) — ✅
| Método | Resultado |
|---|---|
| `listarMcpsGeracao()` | Retorna JSON; fallback de erro se o serviço não foi ligado. |
| `listarModelosGeracao(mcpId)` | Síncrono na thread da ponte (não é a UI). Try/catch devolve JSON de erro. |
| `escolherImagemGeracao(origem)` | Valida `galeria`/`biblioteca`, devolve `{ok:true,aguardando:true}` e despacha via `runOnUiThread` (com checagem de `encerrada`). |
| `limparImagemGeracao()` | Limpa a referência e devolve `okLimpar()`. |
| `gerarImagensMcp(pedidoJson)` | Valida de forma síncrona com `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler`. Usa `AtomicBoolean` contra concorrência, chama `gerarEmBackground` e devolve `okAndamento()`. Se a partida lançar exceção, a flag é liberada. |

Todos retornam String JSON, como o brief exige. Os nomes batem com o que o HTML chama (`chromebrothers.listarMcpsGeracao`, etc.).

### 2. Callback de `gerar` posta na thread de UI — ✅ confirmado
- `gerarEmBackground` chama `aoAndamento` e `aoFim` na thread própria do serviço (documentado e verificado no código).
- Ambos passam por `entregarGeracaoMcpAoHtml` → `executarNaPaginaDaColecao`, que faz `activity.runOnUiThread { if (encerrada || activity.isDestroyed) return; webDaPagina.evaluateJavascript(...) }`. Portanto o `evaluateJavascript` sempre roda na UI.
- `aoTerminarGeracaoDeImagemViaMcp` também é invocado em `activity.runOnUiThread`.
- O JSON entra no JS por `JSONObject.quote(json)`, que é literal de string seguro. O HTML faz `JSON.parse` quando recebe string.
- O `AtomicBoolean` é liberado antes da entrega (`set(false)` primeiro), então o botão reabilitado pelo HTML nunca encontra a flag presa.
- A adaptação `{andamento,atual,total}` → `{progresso:"..."}` está correta. Sem ela o HTML trataria o andamento como resultado final e reabilitaria o botão.

### 3. Seed em `AtividadePrincipal` — ✅ não sobrescreve
- `semearComponenteDeGeracaoDeImagemViaMcpSeAusente()` roda logo após `bibliotecaDeComponentesDaColecao = gravacaoDaBibliotecaDeComponentes.carregar()`.
- Faz `if (buscar(idFixo) != null) return` antes de qualquer escrita. `guardar` é chamado com `idExistente = null`, então só tem o caminho "criar" e nunca o de "atualizar". Edições do usuário no item semeado são preservadas.
- O id fixo entra via `novoId = { idFixo }`, sem alterar `guardar`. É a opção preferida do brief.
- `gravar` só roda se `Ok`. Se o HTML ou as tags forem recusados, o aplicativo não quebra, mas a falha é silenciosa (ver L3).
- Não há `substituirPor` nem reatribuição posterior da biblioteca de componentes que apague o item semeado. Só existem os `apagar` manuais do usuário.

### 4. Wiring do Gestor — ✅
- `ligarServicoDeGeracaoDeImagemViaMcp`, `ligarEscolherImagemDeReferenciaDaGeracao` e `ligarAoTerminarGeracaoDeImagemViaMcp` guardam o callback e o repassam às janelas existentes (`janelas.forEach`).
- `aplicarPontes` (linhas ~326-330) reaplica os três a janelas novas, no mesmo padrão dos demais callbacks.
- O Gestor embrulha `escolher` com `janela`, então o resultado do picker volta para a janela que pediu. Isso é correto para multi-janela.
- Na Activity, o `by lazy` do serviço é seguro, pois o wiring em ~1140 usa a mesma instância.

### 5. Pickers
- **Galeria** ✅: `GetContent("image/*")` registrado como propriedade, no momento certo do ciclo de vida. Leitura em `Thread` com limite de 20 MB. `ActivityNotFoundException` tratado. O cancelamento devolve erro ao HTML.
- **Biblioteca** ✅: `AlertDialog` com até 100 itens. `escolheu` + `OnDismissListener` tratam o cancelamento sem disparar duplicado. A leitura de bytes vai para `Thread`.
- `definirReferenciaDaGeracaoEMontarJson` só grava a referência depois de o preview decodificar. Uma imagem inválida não "envenena" a referência.

## Achados e severidades

| # | Sev. | Achado | Sugestão |
|---|---|---|---|
| M1 | **Medium** | O componente semeado **ressuscita** após o usuário apagá-lo. A semente só checa "id ausente", então apagar o item o recria no próximo start. É coerente com "idempotente" do brief, mas pode surpreender. | Registrar em `preferencias` um flag `componenteGeracaoMcpJaSemeado` e semear só uma vez. Alternativa: aceitar o comportamento e documentá-lo. |
| M2 | **Medium** | A referência é **única e compartilhada** no serviço (que é um só para o app), mas a flag de geração é **por painel**. Com duas janelas da coleção abertas, escolher imagem numa troca a referência da outra. Duas gerações simultâneas também são possíveis. Há snapshot (`val ref = referencia`) no início da geração, então não corrompe, mas o preview da outra janela fica desatualizado. | Mover o `AtomicBoolean` para o serviço (ou Activity), ou guardar a referência por janela. Já listado nas concerns do relatório. |
| M3 | **Medium** | `listarModelosGeracao` faz rede bloqueante (até 30 s de conexão + 120 s de leitura) no `DOMContentLoaded`/ao trocar o MCP. Não bloqueia a UI do Android, mas congela o JS da página. Sem timeout próprio, só o do OkHttp. | Tornar assíncrono (retornar `aguardando` + callback `__chromebrothersModelosGeracao`), ou reduzir o `callTimeout` para essa chamada. |
| L1 | Low | O nome do arquivo de referência vem de `nomeExibido`, que é texto livre sem extensão garantida. O cliente deduz o `content_type` pela extensão do nome (`.png`/`.webp`, senão jpeg). O `mime` passado a `definirReferencia` **não é usado** pelo cliente. Na galeria, um PNG ou HEIC sem extensão pode subir como `image/jpeg`. Na biblioteca os arquivos são sempre JPEG (`quadro-$id.jpg`), então não há problema real lá. | Para a biblioteca, usar `item.nomeDoArquivo`. Para a galeria, derivar a extensão do `mime` quando o nome não tiver uma. |
| L2 | Low | `mostrarListaDaBibliotecaParaReferenciaDaGeracao` lê o disco na thread de UI: `gravacaoDaBibliotecaDeImagens.carregar()` e `arquivo(...)` por item antes do `take(100)`. Com bibliotecas grandes pode dar jank. A `bibliotecaDeImagens` em memória já existe. | Usar a lista em memória e verificar o arquivo só para os 100 exibidos. |
| L3 | Low | Falha do seed (`Recusada`) é silenciosa. | Registrar um log (`Log.w`) no caso `Recusada`. |
| L4 | Low | `painelPedindoImagemDeReferenciaDaGeracao` se perde se o processo morrer enquanto o seletor de galeria está aberto. O resultado é descartado sem erro. | Aceitável. Anotar como limitação. |
| L5 | Low | Se a janela fechar durante a geração, as imagens são gravadas na biblioteca, mas o callback é descartado (`encerrada`). A biblioteca é sincronizada via `aoTerminar`, então nada se perde. | Nenhuma ação. |
| L6 | Info | Sem teste unitário novo. É razoável, já que o código depende de Android/WebView e a lógica pura (pedido, respostas JSON, HTML) está coberta por testes de tarefas anteriores. A validação manual no tablet com sessão Higgsfield real **ainda não foi feita**. | Fazer um smoke manual: galeria, biblioteca, gerar N=2, fechar janela no meio. |

## Conclusão
- **Spec ✅**: ponte completa, pickers, seed idempotente que não sobrescreve, wiring no Gestor (janelas existentes e novas) e callbacks na UI thread.
- **Quality: Approved**. Nenhum achado Critical ou High. Recomendo tratar **M1** e **M2** antes de uma entrega final multi-janela. M3 e os Low podem ficar para depois.
