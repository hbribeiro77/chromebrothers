### Tasks 5+6: WebView drop check + assemble/install/aceite

**Workspace:** `D:\claude\defensoria\chromebrothers\aplicativo-android-do-navegador-chromebrothers`

## Task 5
1. Confirm `startDragAndDrop` uses `DRAG_FLAG_GLOBAL | DRAG_FLAG_GLOBAL_URI_READ` in PainelDaGaveta (already Task 3).
2. Search if page WebView/Gecko needs anything for external file drop — if a small grant/flag fix is obvious, apply it; else document limitation in report (ChatGPT/file input = best-effort).
3. Do not invent a large Gecko patch.

## Task 6
```
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

Checklist in report (manual items marked for user):
1. Long-press image → drop on MCP image gen → ref
2. Long-press video → drop on MCP video gen → frame+Toast
3. Long-press → ChatGPT/file input (best effort)
4. ⓘ and short tap intact
5. Coleção→editor lift-drag still works

NO git commits. NO Fase extras.

Report: `.superpowers/sdd/task-f2-5-6-verify-report.md`
