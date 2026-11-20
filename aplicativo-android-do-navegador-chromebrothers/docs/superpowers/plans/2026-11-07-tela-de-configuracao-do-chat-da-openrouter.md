# Tela de configuração do chat — Implementation Plan

> Executar task a task. Sem commit automático.

**Goal:** ⚙️ abre overlay de config organizado (seções + linhas-resumo); chat fica só conversa.

**Architecture:** FrameLayout no corpo da gaveta: `corpo_do_chat` + `painel_da_configuracao` (ScrollView com acordeões). Kotlin só alterna visibilidade e expande seções.

**Tech Stack:** XML layouts, LinearLayout/ScrollView, PainelDoChatDaOpenRouterNoNavegador.kt

## Tasks

1. Extrair conteúdo atual de `painel_da_chave` para `layout_painel_da_configuracao_do_chat_da_openrouter.xml` com seções colapsáveis (cabeçalho clicável + detalhe).
2. Reestruturar gaveta: FrameLayout weight=1 com corpo do chat vs overlay config; strings título/voltar.
3. Wire: gear/abrir-sem-chave → config; voltar/gear → chat; atualizar resumos de estado; `prepararNotas` ao abrir.
4. Build/install.
