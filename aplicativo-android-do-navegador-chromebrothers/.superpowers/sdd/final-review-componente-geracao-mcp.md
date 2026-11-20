# Revisão final — Componente da coleção: geração de imagem via MCP

Escopo: revisão somente leitura, sem git e sem editar código-fonte.
Plano: `docs/superpowers/plans/2026-11-05-componente-da-colecao-para-geracao-de-imagem-via-mcp.md`
Spec: `docs/superpowers/specs/2026-11-05-componente-da-colecao-para-geracao-de-imagem-via-mcp-design.md`

Limite da verificação: não há `gradlew` na raiz do projeto (nem em `..`). Não consegui rodar os testes de `regras-de-video-do-navegador` nem compilar. A revisão é só de leitura de código.

## Strengths

- A divisão em camadas segue a spec. A lógica pura (`PedidoDeGeracaoDeImagemViaMcpNaColecao`, `ListagemDeModelosDoHiggsfieldNoChat.jsonDosItensParaPonte`, HTML empacotado) fica no módulo JVM `regras`, com testes. Rede, sessão e gravação ficam no nativo (`ServicoDeGeracaoDeImagemViaMcp...`).
- `PedidoDeGeracaoDeImagemViaMcpNaColecao.ler` valida o que a spec pede: prompt obrigatório, limite de 4000 reaproveitado do chat, MCP desconhecido recusado e quantidade de 1 a 4. O `valorDaChave` não é enganado por aspas dentro do prompt, porque o `JSON.stringify` do HTML escapa as aspas.
- O contrato da ponte é estável: sempre devolve JSON com `ok`/`erro`. `gerarImagensMcp` devolve na hora `{ok, andamento}` e roda o trabalho em thread própria, então não trava a WebView. Há trava `AtomicBoolean` contra geração dupla na mesma janela.
- `listarModelosGeracao` roda na thread da ponte do WebView, não na principal, e captura exceções.
- O progresso é traduzido corretamente. O serviço emite `{andamento, atual, total}`, o painel converte para `{progresso:"Gerando imagem n de N…"}` em `progressoParaHtml`, e o HTML trata `r.progresso`.
- A entrega ao HTML usa `JSONObject.quote` + `evaluateJavascript` com guarda `encerrada`/`isDestroyed`, então não há injeção de JS.
- A referência é capturada em `val ref = referencia` no início da geração, então `limpar` durante o job é seguro. O `temReferencia` sem imagem escolhida dá erro claro.
- Os pickers têm tratamento completo:
  - galeria com `GetContent`, limite de 20 MB e leitura fora da thread principal;
  - biblioteca com `AlertDialog`;
  - `ActivityNotFoundException` tratada;
  - `OnDismissListener` devolve "Nenhuma imagem escolhida";
  - a referência só é guardada depois de o preview decodificar (`definirReferenciaDaGeracaoEMontarJson`).
- O seed é idempotente (`buscar(id)` antes de `guardar`) e usa id fixo e tags da spec.
- O resultado volta como `previewDataUrl` JPEG de 320px. Isso evita depender de `file://`/CORS na WebView carregada com `loadDataWithBaseURL`.
- A biblioteca em memória é recarregada ao terminar (`sincronizarBibliotecaDeVideos` via `ligarAoTerminarGeracaoDeImagemViaMcp`).
- O `count` foi adicionado ao cliente (`quantidade.coerceIn(1,4)`, só quando maior que 1 e só para imagem). O fluxo de referência não foi tocado.

## Issues

### Critical

Nenhum.

### Important

1. **Risco de perder imagens geradas (gravação fora da memória).** `gravacao.guardarImagemComMetadados` carrega e grava a biblioteca direto no disco, em thread de fundo. Em paralelo, a Activity e `PainelDaGavetaDaBibliotecaDeVideos` mantêm `bibliotecaDeImagens` em memória e fazem `gravacaoDeImagens.gravar(bibliotecaDeImagens)` em várias ações (linhas 251, 1315, 2646, 2690 e 2705).
   - O `sincronizarBibliotecaDeVideos()` só roda no fim do job.
   - Com 4 gerações em série (minutos), qualquer captura, favorito ou exclusão feitos pelo usuário nesse intervalo sobrescreve o arquivo da biblioteca. As imagens já gravadas ficam órfãs no disco, sem item na lista.
   - O mesmo padrão já existia no chat, mas aqui a janela é maior.
   - Mitigação sugerida: sincronizar a memória a cada item gravado (chamar `aoTerminar`/sync por imagem) ou gravar via a instância em memória.

2. **O fluxo difere da spec: `count` não é usado.** O serviço faz N chamadas com `quantidade = 1` (o `count` do cliente fica sem uso). A spec manda enviar `params.count` e, se a resposta trouxer só um job, repetir como fallback.
   - Efeito: N uploads da mesma referência (`media_upload` + PUT + `media_confirm` a cada iteração), geração estritamente sequencial e N vezes o timeout em caso de lentidão.
   - Funcionalmente correto e previsível, e a falha parcial devolve os itens já gravados.
   - Ou se atualiza a spec/plano para registrar a decisão, ou se reaproveita o `mediaId` entre iterações, ou se usa `count`.

### Minor

1. **MIME ignorado na referência.** `definirReferencia(bytes, nome, mime)` guarda o `mime`, mas `enviarReferenciaDeImagem` decide o `content_type` só pela extensão do nome.
   - No picker da biblioteca o nome costuma ser `nomeExibido` (sem extensão) e o mime vem fixo como `image/jpeg`.
   - Isso pode provocar o `media_confirm` falho já visto com PNG ("PNG media_confirm once"), que já está registrado.
   - Convém passar o mime ao cliente, ou normalizar a referência para JPEG, que é o formato que a biblioteca grava.

2. **Spec: bloquear Gerar sem modelo.** Se a listagem de modelos falha ou volta vazia, o HTML envia `modelo:""` e o cliente usa o default silenciosamente. A spec pedia o Gerar bloqueado até haver modelo ou um default explícito. `jsonDosItensParaPonte` também devolve `ok:true` com `itens:[]` quando o texto não é o JSON esperado (fallback em linhas), sem erro visível.

3. **Toque na grade não faz nada.** O serviço devolve só `idBiblioteca` e `previewDataUrl`, sem `fileUrl`. O HTML só liga `abrirImagemNoVisor` quando há `fileUrl`, então o clique nunca dispara. O item 7 da UI da spec é opcional ("pode usar"), mas há código morto.

4. **Resultados acumulam na grade.** `grade` nunca é limpa entre gerações. Se o WebView recarregar, `temRef` volta a `false` mas a referência continua no serviço; é inofensivo, porque `temReferencia:false` a ignora, só a UI fica sem indicação.

5. **`__chromebrothersGeracaoMcp` e JSON `null`.** Se o JSON chegar como `null`, `r.itens` lança exceção e o botão fica desabilitado. Cenário improvável. Vale `if (!r) r = {...}`.

6. **Qualidade/formato.** `guardarQuadro` reduz para 1920 px e recodifica em JPEG 90 (comportamento já existente do chat). Registrar, porque o PNG original do Higgsfield não é preservado.

7. **Seed por janela.** `semearComponenteDeGeracaoDeImagemViaMcpSeAusente()` roda em todo `onCreate`. Duas janelas abrindo juntas podem ler a biblioteca antes de gravar e se sobrescrever, mas o resultado é o mesmo componente. Os itens "seed reaparece" e "ref multi-janela" já estão registrados.

## Já registrado (não reavaliado)

seed reaparece, ref multi-janela, `listarModelos` bloqueante (na thread da ponte), PNG `media_confirm` uma vez, janela corta.

## Assessment

**Ready (com ressalvas)** para uso/validação manual no tablet. Não há bloqueadores críticos, e o fluxo principal (listar modelos → gerar com e sem referência → grade → biblioteca) está coerente com a spec.

Antes de dar como encerrado:
- Tratar o Important 1 (sincronizar a biblioteca a cada imagem gravada) ou aceitá-lo conscientemente.
- Registrar a decisão sobre `count` (Important 2).
- Rodar a suíte `regras-de-video-do-navegador` e o build do app, que eu não consegui executar por falta do wrapper do Gradle.
