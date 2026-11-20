# Chat player / visor / floating button — Implementation Plan

> Commits só se o usuário pedir. Prefer Grok subagents.

**Goal:** Chat lista/abre vídeos e imagens da biblioteca nativa; lê vídeo detectado na aba; mostra floating button ou abre player direto.

**Spec:** `docs/superpowers/specs/2026-11-01-chat-player-visor-e-botao-flutuante-do-video-design.md`

## Global Constraints

- Leitura sem confirmação; ações com confirmação salvo site liberado
- Reusar `abrirPlayerNativo` / `abrirItemDaBiblioteca` / visor da gaveta
- Sem favoritar/download/controles via chat (v1)
- pt-BR; nomes longos; PowerShell; sem commit salvo pedido

### Task 1: Schema + interpretação + liberação + instrução (regras)

Tools leitura: `ler_video_detectado_na_aba`, `listar_videos_da_biblioteca`, `listar_imagens_da_biblioteca`  
Tools ação: `mostrar_botao_flutuante_do_video`, `abrir_player_do_video_detectado`, `abrir_video_da_biblioteca` (chave), `abrir_imagem_da_biblioteca` (id)

Wire AcoesConfirmadas + Pedido + Liberacao + Ferramentas json/instrucao/rótulos + testes JVM.

### Task 2: Wire PainelDoChat + AtividadePrincipal

Callbacks: descrever vídeo detectado; mostrar botão (repor posição como UI atual); abrir player detectado; listar/abrir vídeo/imagem biblioteca.

### Task 3: Testes + assembleDebug + adb install
