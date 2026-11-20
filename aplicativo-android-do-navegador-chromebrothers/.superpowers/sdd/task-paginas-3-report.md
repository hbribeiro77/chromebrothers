# Task 3 — Coleção: idOrigem + guardar + carregar

**Status:** DONE  
**Commits:** none

## O que foi entregue

No `PainelDaColecaoFlutuanteDoChatDoNavegador`:

- `idOrigemDaBiblioteca` privado; `limparVinculoBiblioteca()` e `idOrigemDaBibliotecaAtual()`.
- `carregarPaginaDaBiblioteca`: título, HTML sanitizado, modo Página, `idOrigem = item.id`, mostra a flutuante e carrega o HTML. Retorno: `Abri "…" na coleção.`
- `guardarNaBiblioteca`: HTML em branco devolve `Não há página para guardar.`; senão sanitiza, chama `biblioteca.guardar`, e no `Ok` grava o `.html`, regrava o índice e fixa `idOrigem`. Mensagens: `Guardei "…" na biblioteca.` / `Atualizei "…" na biblioteca.` Recusa repassa a mensagem da biblioteca.
- `publicarPagina` não zera `idOrigem`. `limparConteudo` (Limpar e Fechar) zera.

UI: botão **Guardar** na faixa Mídia/Página (`botao_guardar_pagina_da_colecao_na_biblioteca`). Habilitado só com HTML. Clique via `ligarGuardarNaBiblioteca`, chamado na `AtividadePrincipal` depois de carregar a biblioteca, com `System.currentTimeMillis()` e `UUID`. Toast com a mensagem retornada.

## Compilação

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

**BUILD SUCCESSFUL in 10s.** Avisos de `statusBarColor` / `navigationBarColor` já existiam.

## Arquivos

- Alterado: `PainelDaColecaoFlutuanteDoChatDoNavegador.kt`
- Alterado: `AtividadePrincipalDoNavegadorChromebrothers.kt`
- Alterado: `layout_atividade_principal_do_navegador_chromebrothers.xml`
- Alterado: `strings_do_navegador_chromebrothers.xml`

## Auto-revisão

- Guardar com vínculo atualiza o item; sem vínculo cria e passa a ter `idOrigem`.
- Publicar de novo não persiste até guardar outra vez.
- Nenhum commit.
