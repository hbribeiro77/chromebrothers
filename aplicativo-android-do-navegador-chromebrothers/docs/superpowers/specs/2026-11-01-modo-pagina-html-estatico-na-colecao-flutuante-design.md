# Modo Página (HTML estático) na coleção flutuante

Data: 2026-11-01  
Status: implementado (2026-11-01)

## Objetivo

A coleção flutuante passa a ter **dois modos** na mesma janela:

- **Mídia** — comportamento atual (vídeo/imagem, ordem, tamanho, baixar).
- **Página** — WebView com HTML/CSS **estático** publicado pela IA.

Os dois estados **convivem**: trocar de modo não apaga o outro. A IA escolhe qual modo abrir; publicar HTML abre em Página.

## Contexto

Hoje a “webview” da IA (texto de instrução do chat) é só a coleção de miniaturas: tipos `video` e `imagem`. Isso é pouco para um quadro que a IA monta. Markdown/ZIP já existem como export; a página é o **rascunho visual** interativo (leitura) na bolha.

## Decisão de produto

| Tema | Escolha |
|------|---------|
| Relação com a coleção | Modo C: dois modos na mesma janela |
| Poder do HTML (corte 1) | A: HTML + CSS estático; imagens `http`/`https`/`file`; **sem JS** |
| JS local / rede | Fora de escopo (fase B futura) |
| Páginas múltiplas | Uma página ativa; republicar substitui |

## UI

- Mesma bolha/painel (`painel_da_colecao_flutuante_do_chat`).
- Controle visível **Mídia | Página** (segmento ou dois botões) quando houver conteúdo em pelo menos um dos lados, ou sempre que o painel estiver aberto.
- Modo Mídia: layout atual (lista, marcar, baixar, etc.).
- Modo Página: área de WebView (preferir o motor já usado no app para conteúdo local — GeckoView ou WebView Android, o que for mais simples para `loadDataWithBaseURL` / arquivo local sem scripts).
- Fechar/minimizar/arrastar/redimensionar continuam iguais e valem para os dois modos.
- Se a página estiver vazia e o usuário for para Página: estado vazio curto (“Nenhuma página publicada.”).

## Ferramentas do chat

### `publicar_pagina_da_colecao`

- Args: `titulo` (string), `html` (string; pode incluir `<style>`).
- Opcional futuro: `css` separado — **não** no corte 1 (CSS vai no HTML).
- Efeito: sanitiza → grava página ativa → mostra painel no modo Página.
- Confirmação: como montar coleção; **site liberado** segue sem perguntar.

### `abrir_modo_da_colecao`

- Args: `modo` = `midia` | `pagina`.
- Efeito: mostra o painel nesse modo (cria/abre a bolha se estiver oculta).
- Confirmação: **não** necessária (só UI), ou leve — preferência: sem confirmação.

### Instrução do sistema

Atualizar o texto de `FerramentasDeLeituraDoChatDaOpenRouter.instrucao` para explicar:

- a coleção tem modo mídia e modo página;
- página = HTML estático sem script;
- imagens só http(s)/file;
- republicar substitui a página.

## Segurança e sanitização

Antes de carregar:

1. Remover / rejeitar `<script>`, `<iframe>`, `<object>`, `<embed>`, `<form action=...>`, meta refresh.
2. Remover atributos `on*` (onclick, onerror, …).
3. Remover URLs `javascript:` e `data:text/html`.
4. Em `src`/`href` de imagens e links: permitir só `http:`, `https:`, `file:` (file só caminhos do app, se aplicável).
5. Links `http(s)` na página: **não** navegar dentro da mini-WebView — interceptar e abrir na aba do navegador (ou ignorar no corte 1 e só bloquear navegação interna; preferência: abrir na aba atual/nova conforme padrão do app).
6. Limite de tamanho do HTML (texto): **300_000** caracteres após trim; acima disso a ação é recusada com mensagem clara.

Sem JS ⇒ sem `fetch`/XHR. Imagens remotas carregam pelo motor da WebView (aceitável no corte A).

## Modelo de dados

- Lista de mídia: inalterada (`ItemDaColecaoFlutuante` vídeo/imagem).
- Página ativa: `titulo` + `htmlSanitizado` (em memória no painel; persistência em disco **opcional** no corte 1 — preferência: só memória da sessão da Activity; se o processo morrer, some).
- Modo atual da UI: `MIDIA` | `PAGINA`.

## Liberação

Site liberado na aba: `publicar_pagina_da_colecao` sem confirmação (igual coleção / markdown).  
`abrir_modo_da_colecao` nunca pede confirmação.

## Testes (regras)

- Sanitizer: remove script/on*/javascript:; preserva HTML+CSS e img https.
- Interpretar args de `publicar_pagina` / `abrir_modo` (limites, modo inválido).
- Liberação inclui publicar página.
- Definições JSON das ferramentas expostas no catálogo.

## Fora de escopo (fase B+)

- JS local limitado.
- Vários documentos / histórico de páginas.
- Editar a página pelo toque.
- Export automático HTML → markdown.
- Tipos texto/link/arquivo no board de mídia (caminho “quadro”, separado).

## Critério de pronto

- Usuário/IA alterna Mídia ↔ Página sem perder a lista nem o HTML.
- IA publica HTML sanitizado e a janela mostra o resultado.
- Tentativa com `<script>` não executa script.
- Build + testes das regras passam; APK instalável no Fold.
