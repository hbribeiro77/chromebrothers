# Plano de reimplementação do que saiu na troca para o Firefox

> **Para quem for implementar:** seguir uma tarefa por vez, na ordem. As tarefas editam os mesmos arquivos (`SuperficieDaPaginaNoMotorDoFirefox.kt`, a atividade principal e a extensão embutida). Não colocar duas tarefas em paralelo. Cada passo usa caixa `- [ ]`.

**Objetivo:** devolver seletor de arquivo, download pedido pela página, cópia de imagem por toque longo, detecção de vídeo e o bloqueio EasyList pedido a pedido, em cima do GeckoView 143 que já está instalado.

**Arquitetura:** o motor é `org.mozilla.geckoview:geckoview:143.0.20251003115653`. Não existe `evaluateJS` nem `shouldInterceptRequest`. Script roda pela extensão embutida, no canal que já existe (`connectNative("chromebrothers")` e `chromebrothers-executar` / `chromebrothers-resultado`). Aviso da página para o app entra no mesmo canal, com outro evento. Arquivo, download e toque longo usam os delegates nativos do Gecko, sem objeto JavaScript na página de terceiros.

**Tecnologias:** Kotlin 2.2.10, AGP 8.7.3, compileSdk 35, minSdk 29, módulo `:regras-de-video-do-navegador` (testes JVM) e `:app`. Tablet arm64 `RX2T7007XMD`. Gradle em `%LOCALAPPDATA%\gradle-8.11.1\bin\gradle.bat`. `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot`. adb em `%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe`.

## Restrições globais

- Não criar commit. A pasta do app não é um repositório git, e commit só quando for pedido.
- Não definir `window.ChromeBrothers`, `ChromeBrothersInicio`, `ChromeBrothersDownload` nem `ChromeBrothersCampo` em página de terceiros. A página inicial já tem `ChromeBrothersInicio` no HTML dela.
- Não chamar `CookieManager` para ler cookie de página carregada no Gecko. O pote de cookie do Firefox é outro. Download de resposta externa grava o `body` que o Gecko já entregou.
- O quadro principal nunca é bloqueado. `DecisaoDeBloqueioDeAnuncio.deveBloquear` continua com `ehQuadroPrincipal == true` devolvendo falso.
- A versão do script de vídeo só muda na tarefa 4, de `12` para `13`, nos dois pontos de `texto()`.
- Não alterar `temSom`, o comportamento do primeiro play mudo, o incremento 2/5/10/30, nem a aparência dos chips de velocidade.
- Não redesenhar diálogos, não colar imagem no compositor, não sugerir busca na rede.
- Quem testa no tablet é a pessoa. Instalar com `adb -s RX2T7007XMD install -r` e avisar que a atualização pode ter fechado o app. Não tocar na tela nem chutar PIN.
- Compilar o app sem `--offline` se o AAR do Gecko não estiver no cache do Gradle. Testes do módulo de regras podem usar `--offline`.

## O que já está no motor e não entra neste plano

- Página no `GeckoView`, modo computador com `USER_AGENT_MODE_DESKTOP`, modo tablet/celular com `USER_AGENT_MODE_MOBILE`.
- Proteção contra rastreamento ligada pelo interruptor Anúncios (`definirProtecaoContraRastreamento`). Ela permanece. A tarefa 5 soma o EasyList, não a substitui.
- Página inicial, janela de login (`onNewSession` + `mostrarSessaoDeLogin`) e a regra `DecisaoDeNavegacaoParaOutroSite`.
- Player nativo (ExoPlayer). O que falta é a página avisar que há um vídeo e um endereço de mídia.

## Ordem

1. Seletor de arquivo. Não injeta script.
2. Download pedido pela página, inclusive `blob:`.
3. Toque longo em imagem.
4. Avisos de vídeo e de mídia, pelo canal da extensão. Confirma o campo de texto, que já usa `evaluateJavascript`.
5. EasyList em `webRequest` bloqueante, no script de fundo da extensão.

Antes da tarefa 4, a pessoa testa o login do DeviantArt. Script a mais na página pode fazer o site barrar de novo. Se o login quebrar nessa tarefa, parar e não seguir para a 5.

Comando dos testes, na pasta `aplicativo-android-do-navegador-chromebrothers`:

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoPedidoDeArquivoVindoDoFirefox" --offline
```

Trocar o nome da classe em cada tarefa. No fim de cada tarefa que mexe no `:app`:

```powershell
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" -s RX2T7007XMD install -r "app\build\outputs\apk\debug\app-debug.apk"
```

---

### Tarefa 1: Seletor de arquivo

**Arquivos:**
- Criar: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/TraducaoDoPedidoDeArquivoDoFirefox.kt`
- Criar: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoPedidoDeArquivoVindoDoFirefox.kt`
- Modificar: `app/src/main/kotlin/com/chromebrothers/navegador/SuperficieDaPaginaNoMotorDoFirefox.kt` (o `promptDelegate` que hoje só implementa `onTextPrompt`)
- Modificar: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt` (`abrirSeletorDeArquivoDaPagina`, por volta da linha 681)

**Interfaces:**
- Consome: `PedidoDeArquivoDaPaginaDoNavegador.cameras(tiposAceitos, escolhePasta)` e `MODO_DE_ESCOLHER_PASTA`.
- Produz: `TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(tipo: Int): Boolean` e `TraducaoDoPedidoDeArquivoDoFirefox.tipos(mimeTypes: Array<String>?): List<String>`.
- No Gecko 143, `PromptDelegate.onFilePrompt(session, FilePrompt): GeckoResult<PromptResponse>`. `FilePrompt.type` usa `FilePrompt.Type.SINGLE`, `MULTIPLE` e `FOLDER`. `FilePrompt.mimeTypes` é `Array<String>?`. `confirm(context, uri)` e `confirm(context, uris)`.

O inteiro `FOLDER` do Gecko não é, por contrato, igual a `MODO_DE_ESCOLHER_PASTA` (2) do WebView. A tradução compara com a constante nomeada, não com o número 2.

- [ ] **Passo 1: Teste que falha**

```kotlin
package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoPedidoDeArquivoVindoDoFirefox {
    @Test
    fun pastaQuandoOTipoEDePasta() {
        assertTrue(TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(TraducaoDoPedidoDeArquivoDoFirefox.TIPO_PASTA))
        assertFalse(TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(TraducaoDoPedidoDeArquivoDoFirefox.TIPO_UM_ARQUIVO))
    }

    @Test
    fun tiposVaziosQuandoOFirefoxNaoMandouMime() {
        assertEquals(emptyList(), TraducaoDoPedidoDeArquivoDoFirefox.tipos(null))
    }

    @Test
    fun variosArquivosQuandoOTipoEMultiplo() {
        assertTrue(TraducaoDoPedidoDeArquivoDoFirefox.variosArquivos(TraducaoDoPedidoDeArquivoDoFirefox.TIPO_VARIOS_ARQUIVOS))
    }
}
```

`TIPO_UM_ARQUIVO`, `TIPO_VARIOS_ARQUIVOS` e `TIPO_PASTA` são os inteiros do AAR 143, lidos de `FilePrompt.Type`: `SINGLE = 1`, `MULTIPLE = 2`, `FOLDER = 3`. O módulo de regras não referencia o Gecko, então as constantes ficam nesses números.

- [ ] **Passo 2: Rodar o teste e ver falhar** porque a classe não existe.

- [ ] **Passo 3: Implementar a tradução e ligar o prompt**

```kotlin
object TraducaoDoPedidoDeArquivoDoFirefox {
    const val TIPO_UM_ARQUIVO = 1
    const val TIPO_VARIOS_ARQUIVOS = 2
    const val TIPO_PASTA = 3

    fun escolhePasta(tipo: Int): Boolean = tipo == TIPO_PASTA

    fun variosArquivos(tipo: Int): Boolean = tipo == TIPO_VARIOS_ARQUIVOS

    fun tipos(mimeTypes: Array<String>?): List<String> = mimeTypes?.toList().orEmpty()
}
```

O app chama assim, dentro do `promptDelegate` já existente:

```kotlin
override fun onFilePrompt(
    session: GeckoSession,
    prompt: PromptDelegate.FilePrompt,
): GeckoResult<PromptDelegate.PromptResponse> {
    val tipos = TraducaoDoPedidoDeArquivoDoFirefox.tipos(prompt.mimeTypes)
    val pasta = TraducaoDoPedidoDeArquivoDoFirefox.escolhePasta(prompt.type)
    val varios = TraducaoDoPedidoDeArquivoDoFirefox.variosArquivos(prompt.type)
    aoPedidoDeArquivo?.invoke(tipos, pasta, varios) { uris ->
        val resposta = when {
            uris.isEmpty() -> prompt.dismiss()
            varios || uris.size > 1 -> prompt.confirm(context, uris.toTypedArray())
            else -> prompt.confirm(context, uris.first())
        }
        // a atividade devolve a PromptResponse pelo GeckoResult criado abaixo
    }
    return resultadoPendente
}
```

`abrirSeletorDeArquivoDaPagina` deixa de receber `WebChromeClient.FileChooserParams`. Passa a receber `tipos: List<String>`, `escolhePasta: Boolean` e `varios: Boolean`, monta o `Intent.ACTION_OPEN_DOCUMENT` como já faz, e no retorno chama o confirm do prompt com os `Uri` escolhidos. `PedidoDeArquivoDaPaginaDoNavegador.cameras(tipos, escolhePasta)` continua decidindo foto e vídeo. Cancelar o seletor chama `prompt.dismiss()`.

Guardar o `FilePrompt` aberto num campo da atividade, porque o `ActivityResult` chega depois. Se chegar outro pedido, dar `dismiss()` no anterior.

- [ ] **Passo 4: Teste verde e `assembleDebug`.** No tablet, abrir uma página com `<input type="file">` e escolher um arquivo. A página precisa receber o arquivo.

---

### Tarefa 2: Download pedido pela página

**Arquivos:**
- Modificar: `SuperficieDaPaginaNoMotorDoFirefox.kt`, o `contentDelegate` que hoje implementa `onTitleChange` e `onCloseRequest`.
- Modificar: `AtividadePrincipalDoNavegadorChromebrothers.kt`, `receberDownloadPedidoPelaPagina` (por volta da linha 1887) e `lerBlobNaPagina` (por volta da linha 1935).

**Interfaces:**
- Consome: `GravacaoDoArquivoPedidoPelaPagina.gravarBytes` e `PedidoDeDownloadDaPagina.nome`.
- Produz: callback `aoRespostaExterna: ((WebResponse) -> Unit)?` na superfície.
- No Gecko 143, `ContentDelegate.onExternalResponse(session, WebResponse)`. `WebResponse.uri`, `headers`, `body: InputStream`, `statusCode`. `WebMessage` é a classe mãe.

Não baixar de novo com `HttpURLConnection` quando o corpo já veio. Não ler `CookieManager`.

- [ ] **Passo 1: Estender o contentDelegate sem apagar título e fechar**

```kotlin
override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
    post { aoRespostaExterna?.invoke(response) }
}
```

- [ ] **Passo 2: Gravar o corpo na atividade**

```kotlin
vista.aoRespostaExterna = { resposta ->
    val nome = PedidoDeDownloadDaPagina.nome(
        resposta.headers["content-disposition"].orEmpty(),
        resposta.uri,
        resposta.headers["content-type"].orEmpty(),
    )
    val mime = resposta.headers["content-type"].orEmpty()
    Thread {
        val bytes = resposta.body.readBytes()
        resposta.body.close()
        runOnUiThread { gravarBytesDaPagina(nome, mime, bytes) }
    }.start()
}
```

Se `body` for nulo, avisar `download_da_pagina_falhou`.

- [ ] **Passo 3: `blob:` volta pela ponte que já existe**

`lerBlobNaPagina` hoje chama `ChromeBrothersDownload.receber`, que não existe na página. Trocar o script para devolver o data URL no `evaluateJavascript`:

```kotlin
vista.evaluateJavascript(script) { bruto ->
    val dados = textoDoJavascript(bruto)
    if (dados.isNullOrBlank() || dados == "null") avisar(R.string.download_da_pagina_falhou)
    else gravarConteudoEmbutidoDaPagina(nome, dados)
}
```

O script passa a ser uma expressão, não uma chamada de ponte:

```javascript
fetch(ENDERECO).then(function (resposta) { return resposta.blob(); }).then(function (blob) {
  return new Promise(function (resolver) {
    var leitor = new FileReader();
    leitor.onloadend = function () { resolver(leitor.result || ""); };
    leitor.onerror = function () { resolver(""); };
    leitor.readAsDataURL(blob);
  });
}).catch(function () { return ""; })
```

O `eval` da extensão não espera promessa hoje (`execucao-na-pagina-do-chromebrothers.js` faz `JSON.stringify` na hora). Ajustar esse arquivo para: se o resultado tiver `.then`, esperar e só então disparar `chromebrothers-resultado`. Sem isso o `blob:` chega vazio.

- [ ] **Passo 4: `assembleDebug` e instalar.** No tablet, baixar um arquivo comum (um PDF ou uma imagem por link de download) e um `blob:` se aparecer. O arquivo vai para `Downloads/ChromeBrothers`.

---

### Tarefa 3: Toque longo em imagem

**Arquivos:**
- Modificar: o mesmo `contentDelegate` da tarefa 2.
- Modificar: `CopiaDeImagemDaPaginaParaABibliotecaDoNavegador.kt`. `ligar(vista: WebView)` deixa de ser o caminho. Criar `perguntarPorEndereco(endereco: String)` público, reusando `perguntar`, `baixar` e `pedirPelaPagina`.
- Modificar: a atividade, para chamar isso no callback novo.

**Interfaces:**
- No Gecko 143, `ContentDelegate.onContextMenu(session, screenX, screenY, ContextElement)`.
- `ContextElement.type` compara com `ContextElement.TYPE_IMAGE`. O endereço é `srcUri`, e se vier vazio usa `linkUri`.
- Produz: `aoMenuDeContexto: ((String) -> Unit)?` recebendo só o endereço da imagem. Coordenadas não entram na decisão.

Não chamar `salvarNaGaleria` a partir da cópia. A classe já separa `COPIAR`, `SALVAR`, `EDITAR` e `ACRESCENTAR`.

- [ ] **Passo 1: Callback no delegate**

```kotlin
override fun onContextMenu(
    session: GeckoSession,
    screenX: Int,
    screenY: Int,
    element: GeckoSession.ContentDelegate.ContextElement,
) {
    if (element.type != GeckoSession.ContentDelegate.ContextElement.TYPE_IMAGE) return
    val endereco = element.srcUri?.takeIf { it.isNotBlank() } ?: element.linkUri?.takeIf { it.isNotBlank() } ?: return
    post { aoMenuDeContexto?.invoke(endereco) }
}
```

- [ ] **Passo 2: A atividade abre o diálogo que já existe**

```kotlin
vista.aoMenuDeContexto = { endereco ->
    copiaDeImagem.perguntarPorEndereco(endereco)
}
```

`perguntarPorEndereco` chama o mesmo `perguntar` de hoje, sem `HitTestResult` e sem `requestFocusNodeHref`. O método `ligar(WebView)` pode ficar sem chamador. Não apagar o diálogo.

- [ ] **Passo 3: Instalar e, no tablet, segurar o dedo numa imagem http(s).** O menu de copiar, salvar, editar e acrescentar abre. Copiar não grava na galeria.

---

### Tarefa 4: Vídeo na página e endereço de mídia

**Arquivos:**
- Modificar: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/ScriptInjetadoParaDetectarVideoHtmlNaPagina.kt`
- Modificar: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDaClassificacaoEscolhaEDownloadDeMidiaDaPagina.kt` só se o teste deixar de achar `onMediaUrl`. O teste da linha 451 exige que o texto do script contenha `onMediaUrl`. Manter essa palavra no nome do aviso.
- Modificar: `app/src/main/assets/extensao-embutida-de-injecao-do-chromebrothers/ponte-de-javascript-da-pagina-no-motor-do-firefox.js`
- Modificar: `app/src/main/assets/extensao-embutida-de-injecao-do-chromebrothers/execucao-na-pagina-do-chromebrothers.js` (espera de promessa, se a tarefa 2 ainda não fez)
- Modificar: `SuperficieDaPaginaNoMotorDoFirefox.kt`, `entregarResultadoDeJavascript`
- Modificar: a atividade, `prepararNavegador`, para receber os avisos nos mesmos lambdas que `PonteJavascriptParaDetectarVideoNaPagina` já usa (`onVideoPlaying`, `onVideoStopped`, `onVideoBounds`, `onVideoLongPress`, `onMediaUrl`).

**Interfaces:**
- A página dispara `CustomEvent("chromebrothers-aviso", { detail: { nome, argumentos } })`.
- O content script isolado escuta e faz `porta.postMessage({ aviso: detalhe.nome, argumentos: detalhe.argumentos })`.
- Mensagem com `id` continua sendo resultado de `eval`. Mensagem com `aviso` é evento. Os dois não se misturam.
- Produz na superfície: `aoAvisoDaPagina: ((String, org.json.JSONArray) -> Unit)?`.

Não criar `window.ChromeBrothers`. O script deixa de chamar `ChromeBrothers.onMediaUrl(...)`. Passa a chamar uma função local:

```javascript
function avisarChromebrothers(nome, argumentos) {
  try {
    document.dispatchEvent(new CustomEvent("chromebrothers-aviso", {
      detail: { nome: nome, argumentos: argumentos }
    }));
  } catch (erro) {}
}
```

Trocar cada `ChromeBrothers.onMediaUrl(absoluto, tipo || "", tamanho || "")` por `avisarChromebrothers("onMediaUrl", [absoluto, tipo || "", tamanho || ""])`, e o mesmo para `onVideoPlaying`, `onVideoStopped`, `onVideoBounds` e `onVideoLongPress`. Os `if (window.ChromeBrothers && ...)` saem. A função local sempre existe.

Em `texto()`, trocar as duas ocorrências de `12` por `13`:

```javascript
if (window.__chromebrothersInstalado === 13) return;
window.__chromebrothersInstalado = 13;
```

`inicio()` não tem esse número. Não inventar versão lá.

- [ ] **Passo 1: Teste do canal, no módulo de regras**

O script é string. O teste garante o contrato novo e a ausência do objeto global:

```kotlin
@Test
fun oScriptAvisaSemCriarObjetoNaPagina() {
    val script = ScriptInjetadoParaDetectarVideoHtmlNaPagina.texto(true)
    assertTrue(script.contains("avisarChromebrothers(\"onMediaUrl\""))
    assertTrue(script.contains("__chromebrothersInstalado === 13"))
    assertFalse(script.contains("window.ChromeBrothers"))
}
```

- [ ] **Passo 2: Rodar e ver falhar.**

- [ ] **Passo 3: Alterar o script e o content script**

No `ponte-de-javascript-da-pagina-no-motor-do-firefox.js`, além do listener de resultado que já existe:

```javascript
document.addEventListener("chromebrothers-aviso", function (evento) {
  if (!porta || !evento.detail) return;
  porta.postMessage({ aviso: evento.detail.nome, argumentos: evento.detail.argumentos || [] });
});
```

Em `entregarResultadoDeJavascript`, se o JSON tiver a chave `aviso`, chamar `aoAvisoDaPagina` e voltar sem mexer em `retornosDeJavascript`.

Na atividade, o callback remonta a ponte antiga:

```kotlin
vista.aoAvisoDaPagina = { nome, argumentos ->
    val ponte = ponteDeVideoDaAba(id)
    when (nome) {
        "onVideoPlaying" -> ponte.onVideoPlaying(argumentos.optString(0), argumentos.optString(1), argumentos.optString(2))
        "onVideoStopped" -> ponte.onVideoStopped()
        "onVideoBounds" -> ponte.onVideoBounds(
            argumentos.optString(0), argumentos.optString(1), argumentos.optString(2),
            argumentos.optString(3), argumentos.optString(4), argumentos.optString(5), argumentos.optString(6),
        )
        "onVideoLongPress" -> ponte.onVideoLongPress(argumentos.optString(0))
        "onMediaUrl" -> ponte.onMediaUrl(argumentos.optString(0), argumentos.optString(1), argumentos.optString(2))
    }
}
```

`ponteDeVideoDaAba` é a `PonteJavascriptParaDetectarVideoNaPagina` que o `prepararNavegador` antigo construía e que hoje não é mais criada. Recriar com os mesmos lambdas de antes (player da página, lista de mídia, toque longo). Não registrar essa ponte com `addJavascriptInterface`.

- [ ] **Passo 4: Testes do módulo de regras verdes, inclusive `TesteDaClassificacaoEscolhaEDownloadDeMidiaDaPagina`.** Instalar. No tablet, tocar um vídeo HTML sem DRM: o player da página aparece. Com o interruptor Baixar ligado, um endereço de arquivo, HLS ou DASH entra na lista. O campo de texto da página continua: escrever nele e ver o texto ir para o campo focado. Se o login do DeviantArt passar a falhar depois desta tarefa, parar aqui.

---

### Tarefa 5: EasyList pedido a pedido

**Arquivos:**
- Criar: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/PacoteDaListaDeBloqueioParaOFirefox.kt`
- Criar: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoPacoteDaListaDeBloqueioParaOFirefox.kt`
- Criar: `app/src/main/assets/extensao-embutida-de-injecao-do-chromebrothers/fundo-de-bloqueio-de-anuncio-do-chromebrothers.js`
- Modificar: `app/src/main/assets/extensao-embutida-de-injecao-do-chromebrothers/manifest.json`
- Modificar: `MotorDoFirefoxDoNavegadorChromebrothers` para, com a extensão pronta, enviar o pacote quando a lista mudar.
- Modificar: o interruptor Anúncios, que já chama `definirProtecaoContraRastreamento`, para também reenviar o pacote. Desligado, o pacote vai vazio.

**Interfaces:**
- Consome: `ListaDeBloqueioDeAnuncio` e `DecisaoDeBloqueioDeAnuncio`. A decisão de um pedido continua no Kotlin, nos testes que já existem. O Firefox não chama Kotlin no meio de um `webRequest` bloqueante, então o script de fundo só aplica um pacote já calculado.
- Produz: `PacoteDaListaDeBloqueioParaOFirefox.json(lista: ListaDeBloqueioDeAnuncio): String` com quatro listas: `bloqueios`, `bloqueiosDeTerceiros`, `excecoes`, `excecoesDeTerceiros`.
- A lista hoje não expõe os conjuntos. Acrescentar quatro getters públicos, sem mudar `deveBloquear`.

`manifest.json` ganha, sem tirar os content scripts:

```json
"permissions": ["webRequest", "webRequestBlocking", "<all_urls>"],
"background": { "scripts": ["fundo-de-bloqueio-de-anuncio-do-chromebrothers.js"] }
```

O script de fundo:

```javascript
var pacote = { bloqueios: [], bloqueiosDeTerceiros: [], excecoes: [], excecoesDeTerceiros: [] };

function cobre(host, lista) {
  var atual = (host || "").toLowerCase();
  while (atual) {
    if (lista.indexOf(atual) >= 0) return true;
    var ponto = atual.indexOf(".");
    if (ponto <= 0) break;
    atual = atual.substring(ponto + 1);
  }
  return false;
}

browser.runtime.onMessage.addListener(function (mensagem) {
  if (mensagem && mensagem.pacote) pacote = mensagem.pacote;
});

browser.webRequest.onBeforeRequest.addListener(function (pedido) {
  if (pedido.type === "main_frame") return {};
  var host = "";
  try { host = new URL(pedido.url).host; } catch (erro) { return {}; }
  var hostPagina = "";
  try { hostPagina = new URL(pedido.originUrl || pedido.documentUrl || "").host; } catch (erro) {}
  var terceiro = hostPagina === "" || !mesmoSite(host, hostPagina);
  if (cobre(host, pacote.excecoes) || (terceiro && cobre(host, pacote.excecoesDeTerceiros))) return {};
  if (cobre(host, pacote.bloqueios) || (terceiro && cobre(host, pacote.bloqueiosDeTerceiros))) {
    return { cancel: true };
  }
  return {};
}, { urls: ["http://*/*", "https://*/*"] }, ["blocking"]);
```

`mesmoSite` no script repete a regra já testada: os dois últimos rótulos do host; IPv4 inteiro; host com `:` inteiro. Copiar essa regra de `DecisaoDeBloqueioDeAnuncio` para uma função pública `baseRegistravel(host: String): String` no módulo de regras, com teste, e o script segue a mesma definição. Não duplicar a regra só na cabeça.

O app envia o pacote para o script de fundo com `extensao.setMessageDelegate` no `WebExtension` (não na sessão) e uma mensagem nativa, ou o script de fundo faz `connectNative` e o app responde no `onConnect` com `port.postMessage`. Um dos dois, o que o `ensureBuiltIn` aceitar na primeira compilação. O pacote vazio significa anúncios desligados.

- [ ] **Passo 1: Teste do pacote**

```kotlin
@Test
fun oPacoteLevaOsQuatroConjuntos() {
    val lista = TextoDaListaDeBloqueioDeAnuncio.ler(
        "||anuncio.test^\n||so-terceiro.test^\$third-party\n@@||libera.test^\n",
    )
    val json = org.json.JSONObject(PacoteDaListaDeBloqueioParaOFirefox.json(lista))
    assertEquals("anuncio.test", json.getJSONArray("bloqueios").getString(0))
    assertEquals("so-terceiro.test", json.getJSONArray("bloqueiosDeTerceiros").getString(0))
    assertEquals("libera.test", json.getJSONArray("excecoes").getString(0))
}
```

O `$` no texto do teste Kotlin precisa ser escapado como `\$` dentro da string, senão o compilador interpola.

- [ ] **Passo 2: Teste falha, implementar getters e `json`, teste passa.**

- [ ] **Passo 3: Manifest, script de fundo e envio ao ligar a extensão e ao mudar o interruptor.**

- [ ] **Passo 4: Os testes antigos de `DecisaoDeBloqueioDeAnuncio` e de `DecisaoDeNavegacaoParaOutroSite` continuam verdes.** Instalar. Com Anúncios ligado, um subrecurso de domínio da lista não carrega, e a página em si carrega. Com Anúncios desligado, o subrecurso carrega. A proteção contra rastreamento continua acompanhando o mesmo interruptor.

---

## Fora deste plano

Login do Google e do DeviantArt já têm janela e exceção de navegação. Não reescrever isso aqui. Se a janela não abrir, o conserto é à parte: conferir se `onNewSession` pode abrir a sessão duas vezes (`setSession` e o próprio Gecko).

Não entra: colar imagem no compositor, régua, pincel, Boosty mux, PixelCopy, `pauseTimers`, Chrome Web Store, e qualquer disfarce de WebGL, canvas ou `webdriver` para passar de antibot.
