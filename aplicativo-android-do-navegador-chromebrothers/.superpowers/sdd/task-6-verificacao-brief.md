### Task 6: Verificação final Fase 1

1. Run:
```
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
```
Expected: BUILD SUCCESSFUL

2. Confirm APK still installed (optional reinstall if assemble produced new apk):
```
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

3. Confirm Fase 2 NOT implemented (grep startDragAndDrop / long-press drag from gaveta cards — should be absent for this feature).

4. Write checklist status against Fase 1 accept criteria (manual items marked as "precisa exercício no aparelho pelo usuário" if you cannot generate MCP videos):
- Generate video/image per MCP → origem + chips
- ⓘ prompt + refs
- Baixar video → Downloads/ChromeBrothers / Baixados
- Baixar image → Pictures/ChromeBrothers
- No new AlertDialog
- Old JSON → Desconhecido
- Card tap still opens media

5. Do NOT implement Fase 2.

NO git commits.

Report: D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers\.superpowers\sdd\task-6-verificacao-report.md
