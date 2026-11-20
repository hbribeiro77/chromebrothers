# Fase 2 Tasks 5+6 — WebView/Gecko drop + assemble/install

**Status:** concluído sem patch. Flags da Task 3 confirmadas. Limitação do Gecko documentada (ChatGPT / `<input type="file">` = best-effort). Sem commits.

## Task 5 — arraste externo para a página

`PainelDaGavetaDaBibliotecaDeVideos.iniciarArrasteDeArquivoLocalDaBiblioteca` (por volta da linha 2851):

```kotlin
val flags = View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ
cartao.startDragAndDrop(clip, sombra, estado, flags)
```

Clip é `content://` do FileProvider (`${applicationId}.arquivos`, `grantUriPermissions="true"`), MIME de imagem/vídeo, `localState` = `EstadoDoArrasteDaBiblioteca`.

### O que o Gecko faz com o drop

Motor: `org.mozilla.geckoview:geckoview:143.0.20251003115653`. A página (ChatGPT) está no `GeckoView` de `SuperficieDaPaginaNoMotorDoFirefox`, não no WebView da coleção.

Bytecode do AAR:

- `GeckoView.onDragEvent` → `PanZoomController.onDragEvent` → `GeckoDragAndDrop.createDropData`.
- No `ACTION_DROP` só há caminho de arquivo interno para `application/x-moz-draganddrop`.
- `text/html` vira HTML. Qualquer outro clip (inclusive `image/*` / `video/*` com URI) cai em `ClipData.Item.coerceToText` e, se houver texto, segue como `text/plain`.
- Não há leitura de `Item.getUri()` nem entrega de `DataTransfer.files` para `<input type="file">`.

Não existe flag pequena no app que mude isso. Preencher o input exigiria patch no Gecko (ou um ponteiro de prompt de arquivo inteiro). Não aplicado.

O `OnDragListener` da coleção (`PainelDaColecaoFlutuanteDoChatDoNavegador`) só consome o arraste com gerador MCP aberto. Fora isso devolve `false`. Isso não alcança o ChatGPT na aba Gecko. O WebView do sistema também não ganha um grant extra que force file input; o caminho MCP já usa o arquivo local / `requestDragAndDropPermissions`.

**Best-effort:** soltar imagem ou vídeo da gaveta no ChatGPT pode, no máximo, inserir texto (`content://…` via `coerceToText`). Não anexa arquivo. O seletor de arquivo da página (prompt nativo do Gecko) continua o caminho confiável.

## Task 6 — build e install

Diretório: `aplicativo-android-do-navegador-chromebrothers`.

```
gradle :regras-de-video-do-navegador:test :app:assembleDebug
→ BUILD SUCCESSFUL in 13s (39 tasks: 6 executed, 33 up-to-date), exit 0
```

```
adb install -r app\build\outputs\apk\debug\app-debug.apk
→ Performing Streamed Install / Success
```

Aparelho: `RX2T7007XMD` `SM_X906B` (`gts8u`), estado `device`.

## Checklist manual (usuário, neste APK)

- [ ] Long-press numa imagem da gaveta → soltar no gerador MCP de imagem → vira referência
- [ ] Long-press num vídeo da gaveta → soltar no gerador MCP de vídeo → frame JPEG + Toast "Usei um frame do vídeo como referência"
- [ ] Long-press → ChatGPT / file input — **best-effort; esperado não anexar arquivo** (ver Task 5)
- [ ] ⓘ e toque curto na gaveta continuam iguais
- [ ] Arraste da coleção para o editor (lift-drag por toque) continua funcionando
