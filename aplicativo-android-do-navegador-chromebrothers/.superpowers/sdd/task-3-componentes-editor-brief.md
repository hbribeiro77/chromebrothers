### Task 3: Bridge + toque longo na `<img>`

**Files:**
- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt`

**Interfaces:**
- Consumes: decode URI → Bitmap (reusar caminho de `abrirImagemDoComponenteNoVisorOuEditor` / `chamarAberturaDeMidia`)
- Produces: `@JavascriptInterface fun enviarImagemParaCamadaDoEditor(uri: String?): String`
- Produces: script injetado após load da página do componente

- [ ] **Step 1: Callback nativo**

No Painel da coleção (mesmo padrão dos `ligarAbrirImagemNoEditor`):

```kotlin
private var aoEnviarImagemParaCamadaDoEditor: ((String) -> String)? = null

fun ligarEnviarImagemParaCamadaDoEditor(enviar: (String) -> String) {
    aoEnviarImagemParaCamadaDoEditor = enviar
}
```

Na `PonteJavascriptDaPaginaDaColecao`:

```kotlin
@JavascriptInterface
fun enviarImagemParaCamadaDoEditor(uri: String?): String =
    chamarAberturaDeMidia(uri, aoEnviarImagemParaCamadaDoEditor)
```

No Gestor: espelhar `ligar…` para todas as janelas (como os outros `ligar*`).

Na Activity: ligar callback que:
1. Decodifica URI → Bitmap (mesmo helper usado em `abrirImagemDoComponenteNoVisorOuEditor`).
2. Se decode falhar → toast `falha_ao_ler_imagem…` e retorna mensagem de erro curta.
3. Chama `painelDaBibliotecaDeVideos.enviarBitmapDoComponenteParaCamadaAtiva(bitmap)`.
4. Se `RECUSAR_SEM_DESTINO` → toast `recusa_envio…`.
5. Retorna `"ok"` / erro para o JS.

- [ ] **Step 2: Injetar listener de toque longo**

Após `carregarComponenteDaBiblioteca` / load HTML da página, `evaluateJavascript` com script que:
- Em cada `img`, `contextmenu` (e/ou touch long ~450ms) previne default e chama  
  `chromebrothers.enviarImagemParaCamadaDoEditor(img.currentSrc || img.src)`.
- Não interfere em inputs/links.

Manter o script idempotente (`window.__chromebrothersEnvioImgArmado`).

- [ ] **Step 3: Smoke compile** (`:app:compileDebugKotlin`)

---

