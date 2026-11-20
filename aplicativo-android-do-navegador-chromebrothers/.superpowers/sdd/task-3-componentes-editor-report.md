# Task 3 — Bridge + toque longo na `<img>`

**Status:** DONE  
**Commits:** none  
**Módulo:** `app` (UI Android); consome `enviarBitmapDoComponenteParaCamadaAtiva` da Task 2

## O que foi entregue

- Bridge `chromebrothers.enviarImagemParaCamadaDoEditor(uri)` na `PonteJavascriptDaPaginaDaColecao`, via `chamarAberturaDeMidia` + callback `aoEnviarImagemParaCamadaDoEditor`.
- `PainelDaColecaoFlutuanteDoChatDoNavegador.ligarEnviarImagemParaCamadaDoEditor` e espelho no `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador` (ligar em todas as janelas + `aplicarPontes` em janela nova).
- Activity: `enviarImagemDoComponenteParaCamadaDoEditor` — decode URI→bytes→Bitmap (mesmo caminho de `abrirImagemDoComponenteNoVisorOuEditor`), toast em falha de leitura / `RECUSAR_SEM_DESTINO`, chamada a `painelDaBibliotecaDeVideos.enviarBitmapDoComponenteParaCamadaAtiva`, retorno JSON `mensagemOk("camada","ok")` / `mensagemErro`.
- Script injetado em `onPageFinished` (quando há HTML): `contextmenu` + toque longo ~450ms em cada `<img>`; idempotente com `window.__chromebrothersEnvioImgArmado`; MutationObserver para imgs dinâmicas. Fatia 1 only (sem drag).

## Arquivos

- Modify: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Modify: `GestorDasJanelasFlutuantesDaColecaoDoChatDoNavegador.kt`
- Modify: `AtividadePrincipalDoNavegadorChromebrothers.kt`

Sem commit.

## Compile check

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Saída: **BUILD SUCCESSFUL in 4s** (exit 0). Warnings pré-existentes de `statusBarColor`/`navigationBarColor` deprecated — não relacionados a esta task.

Primeira tentativa falhou: `const val` + `trimIndent()` (não é constante em tempo de compilação). Corrigido para `private val`.

## Desvios em relação ao brief

- Retorno ao JS usa o JSON já padronizado de `AberturaDeMidiaLocalDoComponenteDaColecao.mensagemOk/Erro` (com `"ok":true` e destino `"camada"`), não a string literal `"ok"` sozinha — alinhado às outras bridges de mídia.
- Bitmap reciclado só em falha/`RECUSAR_SEM_DESTINO`; em sucesso o editor pode ter usado `origem` se `copy` falhar (mesmo padrão cauteloso de `abrirImagemDoComponenteNoVisorOuEditor`).

## Preocupações

- Em sucesso de fusão o bitmap origem fica para o GC (editor faz `copy`; fallback raro usa `origem`).
- Script não documenta a API em `FerramentasDeLeituraDoChatDaOpenRouter` (brief não pediu; YAGNI).
- Sem teste unitário do script (smoke compile apenas, como o brief).
