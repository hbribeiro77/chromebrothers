# Task 5 — Tools do chat da biblioteca de páginas

**Status:** DONE  
**Commits:** none

## O que foi entregue

Leitura (sem confirmação), no mesmo caminho de `listar_midia_da_pagina`:

- `listar_paginas_da_biblioteca` em `permitidas`, `json()` e rótulos. O painel chama o callback e devolve id, título e data UTC (`yyyy-MM-dd HH:mm`). Lista vazia: `Nenhuma página na biblioteca.`

Ações confirmadas (site liberado na aba pula a pergunta, como `publicar_pagina_da_colecao`):

- `guardar_pagina_da_colecao` — argumentos vazios. Executa `painelDaColecao.guardarNaBiblioteca(...)`.
- `abrir_pagina_da_biblioteca` — `id` ou `titulo` (os dois podem vir; a execução usa o id se não estiver em branco). `buscar` / `buscarPorTitulo`, depois `carregarPaginaDaBiblioteca` (já mostra a flutuante).
- `apagar_pagina_da_biblioteca` — `id` obrigatório. `apagar`, `apagarArquivos`, `gravar`. Se o vínculo da coleção for esse id, `limparVinculoBiblioteca()`. A página ao vivo permanece.

A instrução do sistema cita as quatro ferramentas e diz que `publicar_pagina_da_colecao` não grava na biblioteca até `guardar_pagina_da_colecao`.

## Testes

`:regras-de-video-do-navegador:test` — **440 testes, 0 falhas**.

Novos, em `TesteDasFerramentasDeListarGuardarAbrirEApagarPaginasDaBibliotecaNoChat` (5): nomes no json/`permitidas`/`nomes` e na instrução; lista id/título/data; interpretar guardar/abrir/apagar; liberação no site da aba.

## Build e instalação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :regras-de-video-do-navegador:test :app:assembleDebug
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
```

**BUILD SUCCESSFUL.** `adb install -r` → **Success**.

## Preocupações

- Abrir com id inexistente não cai no título, mesmo se os dois vierem juntos.
- Id acima de 80 caracteres é recusado (limite do nome de arquivo no disco).
- O fluxo do chat no aparelho (confirmação, toast da coleção, gaveta aberta) não foi exercido na UI; só regras, assemble e install.
- Se a gaveta Páginas já estiver aberta, guardar/apagar pelo chat chama `atualizar()`.

## Arquivos

- `AcoesConfirmadasDoChatDaOpenRouter.kt`
- `LiberacaoDeNavegacaoDosSitesParaOChat.kt`
- `FerramentasDeLeituraDoChatDaOpenRouter.kt`
- `PainelDoChatDaOpenRouterNoNavegador.kt`
- `AtividadePrincipalDoNavegadorChromebrothers.kt`
- `TesteDasFerramentasDeListarGuardarAbrirEApagarPaginasDaBibliotecaNoChat.kt`

## Continuação do pedido (abrir página da biblioteca)

`ContinuacaoDoPedidoDoChat.acoes()` deixou de tratar todo “abre/abrir” como `abrir_endereco`.

- “abre/abrir” com biblioteca + página, ou o nome `abrir_pagina`, pede `abrir_pagina_da_biblioteca` e não pede endereço.
- “guardar/salvar” com biblioteca, `guardar_pagina`, ou página + coleção, pede `guardar_pagina_da_colecao`.
- “apagar/excluir” com biblioteca + página pede `apagar_pagina_da_biblioteca` e não vira `tirar_da_colecao`.
- `mensagem()` cobre as três ações.

`TesteDaContinuacaoDoPedidoDoChatDaOpenRouter`: “abre a página X da biblioteca” falta só abrir a página e, depois dessa ação, não continua; “guarda esta página na biblioteca” pede guardar.

`:regras-de-video-do-navegador:test --tests TesteDaContinuacaoDoPedidoDoChatDaOpenRouter` e `:app:assembleDebug` — **BUILD SUCCESSFUL**. `adb install -r` — **Success**. Sem commit.
