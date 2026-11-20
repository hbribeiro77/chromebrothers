# Task 7 — Fatia 2: arrastar `<img>` até o quadro

**Status:** DONE  
**Commits:** none  
**Módulo:** `app` (UI Android); reusa `resultadoDoGestoDeSoltar` (Task 1) + `enviarImagemDoComponenteParaCamadaDoEditor` / `enviarBitmapDoComponenteParaCamadaAtiva` (Tasks 2–3)

## O que foi entregue

- Script JS (`__chromebrothersEnvioImgArmado`) estendido:
  - long-press ~450ms **sem** movimento significativo → `enviarImagemParaCamadaDoEditor` (fatia 1 intacta);
  - movimento > ~24px (`576` px²) **antes** do long-press → modo arraste (não auto-envia);
  - durante arraste: `iniciarArraste…` + `atualizarArraste…(screenX,screenY)`;
  - `touchend`/`touchcancel`: `soltarArrasteDeImagemParaOEditor(uri, x, y)` via `changedTouches` + `screenX`/`screenY`.
- Bridges na `PonteJavascriptDaPaginaDaColecao` + `ligar*` no Painel e espelho no `Gestor` (`aplicarPontes`).
- `PainelDaGavetaDaBibliotecaDeVideos.retanguloDoQuadroDoEditorNaTela(): Rect?` — `getLocationOnScreen` + width/height do `EditorDeRecorteETraco…` só se `editandoImagem`.
- Activity `soltarArrasteDeImagemDoComponenteParaOEditor`: hit-test no rect → `EnvioDeImagemDoComponenteParaCamadaDoEditor.resultadoDoGestoDeSoltar` → `FUNDIR` chama o mesmo decode+fundir da fatia 1; `CANCELAR` → no-op (`mensagemOk("camada","cancelado")`, sem toast).
- Shadow de arraste: omitida (YAGNI); bridges `iniciar`/`atualizar` armadas sem feedback visual.

## Arquivos

- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`
- Modify: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt`

Sem commit.

## Compile / install

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin :app:assembleDebug
```

**BUILD SUCCESSFUL** (exit 0). Warnings pré-existentes de `statusBarColor`/`navigationBarColor`.

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Device `RX2T7007XMD`: **Success**.

## Desvios / YAGNI

- Sem ImageView overlay de sombra (brief: optional / YAGNI).
- Coords via `touch.screenX`/`screenY` do WebView (não offset nativo da WebView).
- CANCELAR retorna JSON ok com detalhe `"cancelado"` (sem toast), alinhado ao “no-op”.

## Preocupações

- Listeners `touchmove` estão `passive: true` — a página pode scrollar durante o arraste; hit-test no soltar ainda funciona.
- Se `screenX`/`screenY` divergirem do sistema de coords do `getLocationOnScreen` em algum OEM, o hit-test falha silenciosamente (CANCELAR). Smoke manual recomendado: soltar no quadro vs fora.
- Fatia 1 (long-press / contextmenu) preservada; arraste só se o dedo mover >24px antes dos 450ms.
