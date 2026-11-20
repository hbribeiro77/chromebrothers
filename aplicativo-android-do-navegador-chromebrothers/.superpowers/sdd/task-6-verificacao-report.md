# Task 6 — Verificação final Fase 1

**Status:** DONE

## Build
`& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug`
→ BUILD SUCCESSFUL (test UP-TO-DATE, assembleDebug UP-TO-DATE)

## Install
`adb install -r app-debug.apk` → Success (device RX2T7007XMD from Task 5; reinstall Task 6)

## Fase 2
- `startDragAndDrop` ausente na gaveta para este feature
- Modal ⓘ / chips presentes; sem long-press drag novo

## AlertDialog
- Import + 3 `AlertDialog.Builder` pré-existentes (não no caminho `mostrarModalDoDetalhe*`)
- Modal novo = `Dialog` Chromebrothers

## Checklist aceite Fase 1

| Critério | Status |
|----------|--------|
| Origem MCP no save + chips | Código pronto — **exercício manual** gerar Topview/Sjinn/Higgsfield |
| ⓘ prompt + refs | Código pronto — **exercício manual** |
| Baixar vídeo → Downloads/ChromeBrothers / Baixados | Código pronto — **exercício manual** |
| Baixar imagem → Pictures/ChromeBrothers | Reusa `baixar` — **exercício manual** |
| Sem AlertDialog novo no fluxo ⓘ | OK |
| JSON antigo → desconhecido | OK (parse) |
| Toque no cartão abre mídia | OK (ⓘ separado) |
| Fase 2 não implementada | OK |

## Minors acumulados (não bloqueantes)
- Imagem sem prompt no modal pode mostrar nome do arquivo
- Reexport cria cópia duplicada
- Filtro origem compartilhado entre abas Vídeos/Imagens
