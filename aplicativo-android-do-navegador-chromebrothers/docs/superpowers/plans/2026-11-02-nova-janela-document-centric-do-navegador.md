# Nova janela document-centric — Implementation Plan

> Commits só se o usuário pedir.

**Goal:** Até 3 janelas do app no overview (document-centric), via overflow, gerenciador e “Abrir em nova janela” no link.

**Spec:** `docs/superpowers/specs/2026-11-02-nova-janela-document-centric-do-navegador-design.md`

**Architecture:** `documentLaunchMode="always"` + Intent `NEW_DOCUMENT | MULTIPLE_TASK`; regra pura de limite 3; cada Activity com suas abas.

## Global Constraints

- Máximo 3 janelas; 4ª recusa com aviso
- Botão = página inicial; link = EXTRA com URL
- Abas isoladas; disco compartilhado
- pt-BR; nomes longos; PowerShell; sem commit salvo pedido

### Task 1: Regra + testes JVM

`LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers` (MAXIMO=3, podeAbrir, mensagemRecusada) + testes.

### Task 2: Manifest + abrir janela na Activity

Manifest: `documentLaunchMode="always"`, `maxRecents="3"`.  
`abrirNovaJanela(endereco?)`, contar `appTasks`, EXTRA inicial no `onCreate`.

### Task 3: UI

Overflow “Nova janela”; botão no gerenciador; item no menu do link; strings.

### Task 4: Verificar

`:regras-de-video-do-navegador:test` + `:app:assembleDebug` + `adb install -r`.
