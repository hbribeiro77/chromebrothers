# Histórico de conversas do chat (OpenRouter) — design

**Data:** 2026-11-02  
**Status:** aprovado

## Objetivo

Persistir conversas do chat da OpenRouter no aparelho, com lista global, retomar, nova conversa, excluir, resumir/compactar para liberar espaço, e medidor circular de uso do histórico. Sem referenciar outra conversa no prompt (fica para versão futura).

## Contexto atual

- Uma conversa por janela, só em memória (`historico` em `PainelDoChatDaOpenRouterNoNavegador`).
- Fechar a Activity apaga as mensagens.
- Disco hoje: chave, modelo, notas de site, sites liberados — não as mensagens.
- `MensagemDeChat`: `papel`, `conteudo`, `partes` (anexos/base64), `chamadas`, `idDaChamada`.

## Decisões de produto (v1)

| Tema | Decisão |
|------|---------|
| Escopo | Salvar / listar / retomar / nova conversa / excluir / resumir |
| Lista | Global no app (qualquer janela vê as mesmas conversas) |
| Limpar | Substitui por **Nova conversa** (arquiva se houver conteúdo) |
| Mesma conversa em 2+ janelas | Permitido; última gravação vence |
| Anexos | Persistir com a conversa |
| Limpeza automática | Não; só exclusão/resumo manual |
| Limite | Alerta + medidor; não apaga sozinho |
| Resumir | Compacta o arquivo (resumo + últimas trocas; remove anexos antigos) |

## Fora de escopo (v1)

- Referenciar / citar outra conversa no contexto do envio.
- Limpeza automática por idade ou LRU.
- Busca full-text nas conversas.
- Sync na nuvem.
- Histórico por aba (continua por janela na UI ativa; lista é global).

## Armazenamento

Pasta interna: `filesDir/conversas_do_chat/`

- `indice.json` — entradas leves: `id`, `titulo`, `criadaEm`, `atualizadaEm`, `bytesAproximados` (e flag opcional `resumida`).
- `conversa_<id>.json` — lista completa de `MensagemDeChat` no formato já serializado para a API (`papel`, `conteudo`, `partes`, `chamadas`, `idDaChamada`).

**Sessão ativa (memória):** `idDaConversaAtual` + `historico`.  
Se `id` for nulo, o primeiro envio com conteúdo cria a conversa, gera título pela 1ª mensagem do usuário (texto cortado) e grava no índice.

**Quando gravar:** após cada turno completo (resposta do assistente / fim do ciclo de tools) e ao tocar Nova conversa (se houver mensagens). Conversa vazia não entra no índice.

**Limites (constantes nas regras):** teto de **quantidade** de conversas **e** teto de **bytes totais** (o medidor usa os bytes); teto duro por arquivo (ex. 50 MB) só para bloquear save extremo. Valores numéricos ficam no plano/implementação.

## UI e fluxos

### Nova conversa

Substitui o botão Limpar. Se a atual tem mensagens: garante persistência e limpa o painel (`id` nulo, `historico` vazio, bolhas zeradas). Se já está vazia: no-op.

### Lista de histórico

Entrada principal: ícone no header do chat (toque no medidor também abre a lista). Lista: título + data da última atualização.  
Toque → carrega o JSON no painel e remonta bolhas (`user` / `assistant` visíveis; mensagens `tool`/`system` de ferramenta ficam no `historico` enviado à API, sem bolha dedicada, alinhado ao comportamento atual).  
Excluir → remove arquivo + índice; se era a ativa nessa janela, painel vai para sessão nova; **outros painéis** com o mesmo `id` também passam a sessão nova (listener in-process do repositório).

### Medidor circular

Sempre visível no header do chat (perto de Nova conversa / histórico). Arco 0–100% = bytes totais do índice ÷ teto. Estados visuais: normal → âmbar (atenção) → vermelho (cheio). Toque abre a lista. O aviso no save permanece como reforço.

### Resumir conversa

Ação na lista (alternativa à exclusão):

1. Confirmação explícita (compacta e remove anexos antigos).
2. Chamada OpenRouter (modelo atual do chat, sem tools) com prompt fixo pedindo resumo fiel em pt-BR do histórico.
3. Substitui o arquivo por: uma mensagem de resumo + as **últimas 2 trocas** (até 2 pares user→assistant, ou seja no máx. 4 mensagens visíveis recentes) intactas; `partes` antigas removidas em todo o arquivo.
4. Atualiza `bytesAproximados`, flag `resumida` e o medidor. Título base permanece (UI pode indicar “resumida”).
5. **Todos** os painéis com aquele `id` recarregam a versão compacta (mesmo listener), para uma janela aberta não sobrescrever o resumo no próximo save.

Requer rede e chave, como o chat. Falha → não altera o arquivo; Toast de erro.

### Alerta de limite

Ao salvar: se quantidade ou bytes ≥ teto → aviso claro pedindo exclusão ou resumo. Não apaga automaticamente.  
Se uma conversa individual ultrapassar teto duro por anexos → avisar; bloquear só nesse caso extremo (não truncar em silêncio).

## Arquitetura de código

### Regras puras (`regras-de-video-do-navegador`) — TDD

- Serializar / deserializar conversa ↔ texto JSON.
- Montar / atualizar índice (criar, tocar, excluir, recalcular bytes).
- Gerar título a partir da 1ª mensagem do usuário.
- Calcular percentual e faixa do medidor (ok / atenção / cheio).
- Compactar: resumo da API + últimas 2 trocas → novo histórico sem `partes` antigas.
- Prompt fixo de resumo.
- Constantes de teto e limiares do medidor.

### App

- Repositório em disco (`filesDir/conversas_do_chat/`): ler/gravar índice e arquivos.
- `PainelDoChatDaOpenRouterNoNavegador`: sessão, save pós-turno, Nova conversa, lista, excluir, resumir, remontar bolhas, medidor.
- Reutilizar `ClienteDeChatDaOpenRouter` para a chamada de resumo (sem tools).

Padrão próximo ao de notas de site: regras testáveis + fachada Android só de I/O.

## Erros

| Situação | Comportamento |
|----------|----------------|
| Falha de disco no save | Toast; mantém estado em memória |
| Falha ao resumir | Toast; arquivo intacto |
| JSON corrompido | Mostra na lista como ilegível; só permite excluir |
| Sem rede no resumir | Toast; não altera arquivo |

## Critérios de sucesso

1. Nova conversa arquiva a atual (com conteúdo) e deixa o painel em branco.
2. Lista global mostra conversas; retomar restaura mensagens e anexos após fechar o app.
3. Excluir remove do disco e do índice.
4. Resumir compacta, reduz bytes e permite continuar na mesma conversa.
5. Medidor reflete uso; alerta aparece ao atingir limite; nada é apagado sozinho.
6. Duas janelas na mesma conversa: ambas operam; última gravação prevalece.
7. `:regras-de-video-do-navegador:test` + `:app:assembleDebug` passam.

## Testes manuais sugeridos

- Nova → várias mensagens com anexo → matar app → retomar.
- Medidor sobe com anexos; resumir baixa o uso.
- Excluir e nova conversa vazia não poluem o índice.
- Duas janelas, mesma conversa, saves intercalados.
