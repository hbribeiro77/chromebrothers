# Modo Página HTML estático na coleção — plano

> **For agentic workers:** implementar task-by-task com TDD. Commits só se o usuário pedir.

**Goal:** Dois modos na coleção flutuante (Mídia | Página); IA publica HTML estático sanitizado e troca o modo.

**Architecture:** Regras puras para sanitizar/interpretar; painel nativo com Android `WebView` para a página; ferramentas de chat espelhando montar coleção + liberação.

**Tech Stack:** Kotlin, JUnit/kotlin.test no módulo regras, Android WebView + layout da coleção, OpenRouter tools.

## Global Constraints

- Sem JS; remover script/on*/javascript:/iframe/object/embed
- Imagens http/https/file; limite HTML 300_000 chars
- Uma página ativa (republicar substitui); sessão em memória
- `publicar_pagina` confirma (exceto site liberado); `abrir_modo` sem confirmação
- pt-BR; nomes de arquivo longos e descritivos

### Task 1: Sanitizer + publicação (regras)

**Files:**
- Create: `regras-de-video-do-navegador/src/main/kotlin/.../SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.kt`
- Create: `regras-de-video-do-navegador/src/main/kotlin/.../PublicacaoDaPaginaDaColecaoFlutuanteDoChat.kt`
- Create: testes correspondentes

**Produces:** `SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(html)`, `PublicacaoDaPaginaDaColecaoFlutuanteDoChat.ler(argumentos)`, `ModoDaColecaoFlutuante`, `lerModo`

### Task 2: Ações do chat + liberação

Wire `publicar_pagina_da_colecao` / `abrir_modo_da_colecao` em `AcoesConfirmadasDoChatDaOpenRouter`, `PedidoDeAcaoConfirmadaDoChat`, `LiberacaoDeNavegacaoDosSitesParaOChat`, instrução do sistema.

### Task 3: UI do painel

Segmento Mídia|Página, container WebView, carregar HTML sanitizado, interceptar navegação http(s) para callback abrir na aba.

### Task 4: Wire PainelDoChat + Activity

Callbacks publicar/abrir modo; confirmação só em publicar; testes + assembleDebug + adb install.
