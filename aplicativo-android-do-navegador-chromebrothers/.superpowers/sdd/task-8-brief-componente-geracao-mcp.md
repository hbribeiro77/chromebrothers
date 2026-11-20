### Task 8: Verificação no tablet

Checklist do plano (não é implementação de código — verificação E2E):

- [ ] Abrir coleção → componente “Geração MCP — imagem” na biblioteca
- [ ] Sem Higgsfield ligado → erro claro ao listar/gerar
- [ ] Com sessão: listar modelos preenche select
- [ ] Gerar 1 sem ref → aparece na grade + biblioteca
- [ ] Gerar com ref (galeria) e (biblioteca) → OK
- [ ] Quantidade 2 → dois itens (ou fallback de 2 jobs)

**Ambiente:**
- Device serial conhecido: `RX2T7007XMD`
- adb: `$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe`
- gradle: `$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat`
- Workspace: `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`
- Sem git commits
- APK debug já foi instalado na Task 7; reinstalar se necessário após rebuild

**Como verificar (adb + UI):**
1. Confirmar device online; se offline, reportar BLOCKED.
2. Garantir APK debug instalado (`assembleDebug` + `adb install -r` se preciso).
3. Abrir o app; abrir coleção flutuante; localizar seed `geracao-mcp-imagem` / título “Geração MCP — imagem”.
4. Exercitar checklist; capturar logcat (`Higgsfield|GeracaoMcp|colecao|SEM_SESSAO`) e evidências.
5. Se sessão Higgsfield não estiver disponível no tablet, documentar o que passou sem sessão e o que ficou bloqueado.

**Entrega:**
- Report em `.superpowers/sdd/task-8-report-componente-geracao-mcp.md` com status DONE / DONE_WITH_CONCERNS / BLOCKED e checklist marcado.
- Sem commits.
