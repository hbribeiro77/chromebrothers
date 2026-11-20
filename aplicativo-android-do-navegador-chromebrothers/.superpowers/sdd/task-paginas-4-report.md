# Task 4 — Seção Páginas na gaveta

**Status:** DONE  
**Commits:** none

## O que foi entregue

- Botão `botao_secao_paginas_da_biblioteca` na faixa de seções (rolagem horizontal para caber Vídeos, Imagens, Baixados e Páginas). Rótulo **Páginas**.
- `SecaoDaGaveta.PAGINAS` lista `bibliotecaDePaginas.listar()`: título e data curta (`d MMM, HH:mm`). Recentes/Favoritos ficam ocultos nessa seção.
- Clique chama `aoAbrirPagina`: a atividade esconde a gaveta e `carregarPaginaDaBiblioteca(item)` abre a flutuante.
- Toque longo ou lixeira: `apagar` + `apagarArquivos` + `gravar`. Se `idOrigemDaBibliotecaAtual() == id`, só `limparVinculoBiblioteca()` — o HTML ao vivo permanece.
- Biblioteca, gravação e callbacks entram no construtor da gaveta (mesmo caminho das imagens), com padrão vazio. O player nativo não recebe páginas e esconde o botão.

## Compilação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

**BUILD SUCCESSFUL in 9s.** Avisos de `statusBarColor` / `navigationBarColor` já existiam.

## Arquivos

- Alterado: `layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`
- Alterado: `PainelDaGavetaDaBibliotecaDeVideos.kt`
- Alterado: `AtividadePrincipalDoNavegadorChromebrothers.kt`
- Alterado: `strings_do_navegador_chromebrothers.xml`

## Auto-revisão

- Apagar o item aberto só solta o vínculo; a página na flutuante continua.
- Reabrir a gaveta recarrega o índice do disco.
- Nenhum commit.
