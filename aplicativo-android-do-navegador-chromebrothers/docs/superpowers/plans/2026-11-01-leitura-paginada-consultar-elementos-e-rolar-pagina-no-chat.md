# Leitura paginada + consultar elementos + rolar página — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans. Steps use checkbox (`- [ ]`) syntax. Commits só se o usuário pedir.

**Goal:** Três tools de leitura sem confirmação — `ler_texto_da_pagina`, `consultar_elementos`, `rolar_pagina` — para o agente explorar páginas grandes e extrair estrutura para a coleção/webview.

**Architecture:** Regras puras (parse args, scripts JS, formatar resposta) no módulo `regras-de-video-do-navegador`; registro no schema/instrução de `FerramentasDeLeituraDoChatDaOpenRouter`; dispatch no `PainelDoChat` como leitura; `evaluateJavascript` na aba ativa via callbacks novos na `AtividadePrincipal`.

**Tech Stack:** Kotlin, kotlin.test/JUnit no módulo regras, GeckoView/WebView `evaluateJavascript`, OpenRouter function tools.

**Spec:** `docs/superpowers/specs/2026-11-01-leitura-paginada-consultar-elementos-e-rolar-pagina-no-chat-design.md`

## Global Constraints

- Sem confirmação; não entram em `AcoesConfirmadasDoChatDaOpenRouter`
- Sem JS arbitrário — só CSS `querySelectorAll` / `scrollTo` / `scrollIntoView`
- URLs extraídas só `http`/`https`
- Texto bloco máx 6000; seletor máx 300; elementos máx 40 por chamada
- Scroll prioridade de args: `seletor` > `y` > `paginas` > `pixels`
- pt-BR; nomes de arquivo longos e descritivos
- Windows PowerShell; Gradle local; commits só a pedido

## File map

| Arquivo | Papel |
|---------|--------|
| `TextoJsonDoChat.kt` | + `inteiroDaChave` / `numeroDaChave` (números JSON sem aspas) |
| `LeituraPaginadaDoTextoDaPaginaParaOChat.kt` | offset/limite, fatiar texto, metadados, script opcional |
| `ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.kt` | validar seletor, script, parse TSV/linhas, formatar |
| `RolagemDaPaginaDaAbaParaOChat.kt` | interpretar modo, script de rolar+medir, formatar |
| `FerramentasDeLeituraDoChatDaOpenRouter.kt` | constantes, schema com properties, permitidas, rótulos, instrução |
| `PainelDoChatDaOpenRouterNoNavegador.kt` | novos callbacks + `when` |
| `AtividadePrincipalDoNavegadorChromebrothers.kt` | wiring `evaluateJavascript` |
| Testes `TesteDa*.kt` correspondentes |

---

### Task 1: `TextoJsonDoChat.inteiroDaChave` (+ número com decimal para `paginas`)

**Files:**
- Modify: `regras-de-video-do-navegador/src/main/kotlin/com/chromebrothers/navegador/regras/TextoJsonDoChat.kt`
- Create: `regras-de-video-do-navegador/src/test/kotlin/com/chromebrothers/navegador/regras/TesteDoInteiroENumeroDaChaveNoTextoJsonDoChat.kt`

**Produces:**
- `fun inteiroDaChave(json: String, chave: String): Int?` — aceita `"10"` ou `10`
- `fun numeroDaChave(json: String, chave: String): Double?` — aceita `"1.5"` ou `1.5`

- [ ] **Step 1: Teste que falha**

```kotlin
@Test
fun leInteiroComESemAspas() {
    assertEquals(3, TextoJsonDoChat.inteiroDaChave("""{"offset":3}""", "offset"))
    assertEquals(3, TextoJsonDoChat.inteiroDaChave("""{"offset":"3"}""", "offset"))
    assertEquals(null, TextoJsonDoChat.inteiroDaChave("""{}""", "offset"))
}

@Test
fun leNumeroComDecimal() {
    assertEquals(1.5, TextoJsonDoChat.numeroDaChave("""{"paginas":1.5}""", "paginas"))
}
```

- [ ] **Step 2: Rodar teste (espera FAIL)**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test --tests "com.chromebrothers.navegador.regras.TesteDoInteiroENumeroDaChaveNoTextoJsonDoChat"
```

- [ ] **Step 3: Implementar** — após achar `"chave"`, pular `:`, se `"` usar `textoNaAspa` + `toIntOrNull`/`toDoubleOrNull`; senão ler token `-?\d+(\.\d+)?` até não-dígito.

- [ ] **Step 4: Rodar teste (PASS)**

---

### Task 2: `LeituraPaginadaDoTextoDaPaginaParaOChat`

**Files:**
- Create: `.../LeituraPaginadaDoTextoDaPaginaParaOChat.kt`
- Create: `.../TesteDaLeituraPaginadaDoTextoDaPaginaParaOChat.kt`

**Produces:**
```kotlin
object LeituraPaginadaDoTextoDaPaginaParaOChat {
    const val LIMITE_PADRAO = 6000
    fun lerPedido(argumentos: String): PedidoDaLeituraPaginada // offset, limite (clamp)
    fun fatiar(textoNormalizado: String, offset: Int, limite: Int): FatiaDoTextoDaPagina
    // Fatia: corpo, total, offset, proximoOffset, temMais
    fun mensagem(titulo: String, endereco: String, fatia: FatiaDoTextoDaPagina): String
    fun normalizar(bruto: String): String // mesmos replaces do ContextoDaPagina
}
```

- [ ] **Step 1: Testes** — texto de 10 chars, limite 4, offset 0 → temMais; offset 8 → parcial; offset 99 → vazio temMais=false; args `{"offset":4,"limite":99999}` → limite clamp 6000; mensagem contém `total=` / `tem_mais=`.

- [ ] **Step 2: FAIL → implementar → PASS**

Formato sugerido da mensagem (além do trecho):

```
Página aberta: …
Endereço: …

total=N offset=O proximo_offset=P tem_mais=sim|nao

<corpo>
```

---

### Task 3: `ConsultaDeElementosDaPaginaPorSeletorCssParaOChat`

**Files:**
- Create: `.../ConsultaDeElementosDaPaginaPorSeletorCssParaOChat.kt`
- Create: `.../TesteDaConsultaDeElementosDaPaginaPorSeletorCssParaOChat.kt`

**Produces:**
```kotlin
object ConsultaDeElementosDaPaginaPorSeletorCssParaOChat {
    const val LIMITE_DO_SELETOR = 300
    const val LIMITE_DE_ITENS = 40
    fun lerPedido(argumentos: String): ResultadoDoPedidoDeConsulta // Ok(seletor, offset, limite) | Erro(msg)
    fun script(seletor: String, offset: Int, limite: Int): String
    fun descrever(brutoDoScript: String): String // ou mensagem de erro embutida no bruto
}
```

**Script (contrato):** retorna texto com:
- primeira linha meta: `ok\ttotal\tN` ou `erro\tmensagem`
- linhas: `tag\ttexto\thref\tsrc\talt\ttitle\taria\tfilhos` onde `filhos` é `img=url|a=url|video=url` unidos por `|`
- escapar tabs/newlines no JS com replace
- try/catch em `querySelectorAll`; filtrar URLs com `^https?:`

- [ ] **Step 1: Testes** — seletor vazio → erro; seletor > 300 → erro; parse de bruto ok com 2 itens + total; `descrever` lista `- article …`; script contém `querySelectorAll` e não contém `eval(`.

- [ ] **Step 2: FAIL → implementar → PASS**

---

### Task 4: `RolagemDaPaginaDaAbaParaOChat`

**Files:**
- Create: `.../RolagemDaPaginaDaAbaParaOChat.kt`
- Create: `.../TesteDaRolagemDaPaginaDaAbaParaOChat.kt`

**Produces:**
```kotlin
sealed class PedidoDeRolagemDaPagina {
    data class PorSeletor(val seletor: String) : …
    data class Absoluto(val y: Int) : …
    data class PorPaginas(val paginas: Double) : …
    data class Relativo(val pixels: Int) : …
}
object RolagemDaPaginaDaAbaParaOChat {
    fun lerPedido(argumentos: String): Resultado // Ok(pedido) | Erro
    fun script(pedido: PedidoDeRolagemDaPagina): String
    fun mensagem(bruto: String): String // y=, altura_pagina=, altura_viewport=, no_fim=
}
```

Prioridade ao ler args: se `seletor` não vazio → PorSeletor (validar ≤300); senão se chave `y` presente → Absoluto; senão `paginas`; senão `pixels`; senão Erro.

Script: aplica scroll; depois mede `scrollY`, `scrollHeight`, `innerHeight`; retorna `ok\ty\taltura\tviewport` ou `erro\t…`.

- [ ] **Step 1: Testes** de prioridade, erro sem args, formatar `no_fim=sim` quando y+viewport >= altura-2, script com `scrollTo` / `scrollIntoView`.

- [ ] **Step 2: FAIL → implementar → PASS**

---

### Task 5: Registrar tools + instrução

**Files:**
- Modify: `FerramentasDeLeituraDoChatDaOpenRouter.kt`
- Modify/Create teste: estender `TesteDaLeituraDeMidia…` ou `TesteDasFerramentasDeExploracaoDaPaginaNoChatDaOpenRouter.kt`

**Changes:**
- Constantes: `TEXTO_PAGINADO = "ler_texto_da_pagina"`, `ELEMENTOS = "consultar_elementos"`, `ROLAR = "rolar_pagina"`
- Incluir em `permitidas`
- `definicaoComProps` (como ações) com properties/required
- Atualizar descrição de `TEXTO` se quiser apontar a paginada
- `instrucao`: bloco sobre página grande / lazy-load / não desistir
- `rotuloVisivel` / `rotuloEmAndamento`

- [ ] **Step 1: Teste** — `json()` contém os três nomes e `"offset"` / `"seletor"` / `"pixels"`; `instrucao` menciona `consultar_elementos` e `rolar_pagina`; `permitidas` contém os três.

- [ ] **Step 2: Implementar → PASS**

---

### Task 6: Wire Painel + Activity

**Files:**
- Modify: `PainelDoChatDaOpenRouterNoNavegador.kt` — ctor + `when`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt` — callbacks

**Callbacks sugeridos:**
```kotlin
lerTextoPaginado: (argumentos: String, aoReceber: (String) -> Unit) -> Unit
consultarElementos: (argumentos: String, aoReceber: (String) -> Unit) -> Unit
rolarPagina: (argumentos: String, aoReceber: (String) -> Unit) -> Unit
```

**Activity:**
- Sem página → `"Não há página aberta."`
- Texto paginado: `evaluateJavascript(Contexto…scriptDoTextoVisivel() ou script full sem slice)` — preferir script que devolve **texto completo normalizado no JS sem truncar**, fatiar no Kotlin com `LeituraPaginada…` (senão offset > 6000 nunca funciona). **Importante:** alterar `scriptDoTextoVisivel` ou adicionar `scriptDoTextoCompletoNormalizado()` sem `.slice(0, 6000)`; manter o truncamento só no fatiador Kotlin / tool antiga.
- Elementos / rolar: `lerPedido` no Kotlin; se Erro, `aoReceber(msg)` sem JS; se Ok, `evaluateJavascript(script)` → `descrever`/`mensagem`.
- Após rolar: pequeno delay opcional **não** no v1 (YAGNI); lazy-load fica a cargo de nova chamada do modelo.

- [ ] **Step 1:** Ajustar `ContextoDaPaginaParaOChat`:
  - `scriptDoTextoCompletoNormalizado()` sem slice
  - `ler_texto_visivel` / contexto atual: continuar truncando em 6000 no Kotlin após o script completo **ou** manter script antigo só para a tool velha e usar o completo só na paginada (preferível: script completo + `take(LIMITE)` na tool antiga para um único caminho)

- [ ] **Step 2:** Wire painel/activity

- [ ] **Step 3: Verificar compile + testes**

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

Expected: BUILD SUCCESSFUL, adb Success

---

## Spec coverage checklist

| Spec | Task |
|------|------|
| `ler_texto_da_pagina` + metadados | 2, 5, 6 |
| `consultar_elementos` CSS only | 3, 5, 6 |
| `rolar_pagina` sem confirmação + prioridade | 4, 5, 6 |
| limites / erros pt | 2–4 |
| instrução sistema | 5 |
| testes JVM | 1–5 |
| fora de escopo midia offset / full capture | não implementar |

## Self-review

- Sem TBD; nomes alinhados à spec
- Script completo sem slice é requisito implícito da paginação (documentado na Task 6)
- `inteiroDaChave` necessário porque `valorDaChave` só lê strings entre aspas
