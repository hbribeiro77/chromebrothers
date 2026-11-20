# Biblioteca de páginas HTML da coleção flutuante

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

Permitir **guardar** a página HTML publicada na coleção flutuante, **reabrir** depois na mesma janela flutuante (minimizar / bolha), **apagar** na gaveta, e deixar o **chat editar** a página aberta — sem gravar automaticamente cada republicação na biblioteca.

## Decisões

- Guardar: botão na coleção **e** ferramenta no chat.
- Abrir na gaveta → carrega na coleção modo Página e mostra a flutuante (não é um viewer separado na gaveta).
- Edição ao vivo (`publicar_pagina_da_colecao`) **não** atualiza o item salvo; só “guardar” de novo (botão ou chat).
- Persistência só do HTML + metadados; **não** inclui itens de mídia da coleção no v1.

## Modelo de dados

`ItemDaBibliotecaDePaginasDaColecao`:

| Campo | Notas |
|-------|--------|
| `id` | estável (string/uuid) |
| `titulo` | máx. 120 |
| `html` | sanitizado como na publicação; máx. 300_000 |
| `atualizadoEm` | epoch ms |

- Limite de itens: **40**.
- Guardar com a sessão “vinda” de um `id` → **substitui** esse item.
- Guardar sem vínculo → **cria** item novo.
- HTML gravado já passa por `SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante` (ou reutiliza o HTML já sanitizado em memória).

## Persistência

Espelhar o padrão de imagens/vídeos:

- Módulo `regras`: biblioteca em memória + regras de acrescentar / atualizar / apagar / listar / buscar.
- App: gravação em pasta do armazenamento interno do app (índice + um arquivo HTML por id, ou equivalente simples e testável).

## UI

### Gaveta da biblioteca

- Nova seção **Páginas** (ao lado de Vídeos / Imagens / etc.).
- Lista: título + data relativa ou curta.
- Toque / Abrir: `carregarPaginaNaColecao(id)` → define HTML + título na coleção, modo Página, marca `idOrigemDaBiblioteca`, mostra flutuante.
- Apagar: remove da biblioteca e do disco; se a coleção aberta for esse id, limpa o vínculo (a página ao vivo pode permanecer até o usuário fechar/limpar — ou limpar a página; preferência v1: **mantém a página ao vivo**, só some o vínculo de “atualiza este id”).

### Coleção flutuante

- Botão **Guardar na biblioteca** visível quando há HTML publicado.
- Sem HTML: botão desabilitado ou oculto.
- Após guardar: feedback curto (toast); se criou, passa a ter `idOrigemDaBiblioteca`.

## Chat

| Ferramenta | Tipo | Notas |
|------------|------|--------|
| `listar_paginas_da_biblioteca` | leitura | id, título, data |
| `guardar_pagina_da_colecao` | ação confirmada* | grava HTML atual; atualiza se houver origem |
| `abrir_pagina_da_biblioteca` | ação confirmada* | args: `id` (preferencial) ou `titulo` |
| `apagar_pagina_da_biblioteca` | ação confirmada* | args: `id` |

\*Confirmação do usuário, **exceto** site liberado (mesma família da coleção / publicar página).

`publicar_pagina_da_colecao` inalterado quanto à biblioteca: só sessão.

Instrução do sistema: mencionar guardar / listar / abrir / apagar páginas da biblioteca e que editar com publicar não persiste até guardar de novo.

## Fluxos

1. **Criar e guardar:** chat publica HTML → usuário ou chat guarda → item na gaveta.
2. **Reabrir:** gaveta Páginas → Abrir → flutuante com a página → chat pode `publicar_pagina` de novo → guardar de novo para persistir.
3. **Apagar:** gaveta ou chat → some da biblioteca.

## Fora de escopo (v1)

- Favoritos / recentes específicos de páginas
- Versionamento
- Salvar mídia da coleção junto
- Indicador “não salvo” obrigatório (opcional depois)
- Viewer embutido só na gaveta (sem flutuante)

## Critério de sucesso

Usuário guarda uma página da coleção, fecha o app, reabre pela gaveta na flutuante, pede ao chat uma alteração, vê a mudança na flutuante, e só atualiza o item da biblioteca ao guardar de novo.
