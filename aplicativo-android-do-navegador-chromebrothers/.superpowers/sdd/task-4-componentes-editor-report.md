# Task 4 — Botão Componentes no chrome do editor

**Status:** DONE  
**Commits:** none  
**Módulo:** `app` (UI Android)

## O que foi entregue

- `ImageButton` `@+id/botao_componentes_no_chrome_do_editor_de_imagem` na `barra_de_acoes_da_imagem_ampliada`, ao lado de `botao_concluir_edicao_da_imagem` / antes de `botao_mostrar_gaveta_na_imagem_ampliada`; `contentDescription` `@string/componentes_no_chrome_do_editor_de_imagem`; ícone reusado `icone_de_guardar_componente_da_carcaca_da_colecao`; `visibility=gone` no XML.
- `PainelDaGavetaDaBibliotecaDeVideos`: `var aoAbrirComponentesDoEditor: (() -> Unit)?`; click → invoke; `VISIBLE` em `mostrarChromeDeEdicao`, `GONE` em `mostrarChromeDeVisita` (espelha `botao_concluir_edicao`).
- Activity: `painelDaBibliotecaDeVideos.aoAbrirComponentesDoEditor = { abrirComponentesPeloChromeDoEditorDeImagem() }`.
- Seletor: `mostrarCaixaDeEscolhaSimplesDoNavegador` (Dialog custom, não AlertDialog) com `bibliotecaDeComponentesDaColecao.listar()` ordenado por `atualizadoEm`; vazio → toast curto `paginas_da_biblioteca_vazias`.
- Ao escolher: `painelDaColecao.carregarComponenteDaBiblioteca(item)` **sem** `painelDaBibliotecaDeVideos.esconder()`.

## Arquivos

- Modify: `app/src/main/res/layout/layout_gaveta_da_biblioteca_de_videos_do_navegador.xml`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/AtividadePrincipalDoNavegadorChromebrothers.kt`

Sem commit.

## Compile check

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Saída: **BUILD SUCCESSFUL in 10s** (exit 0). Warnings pré-existentes de `statusBarColor`/`navigationBarColor` deprecated — não relacionados a esta task.

## Desvios em relação ao brief

- Ícone: reuso de `icone_de_guardar_componente_da_carcaca_da_colecao` (não há drawable dedicado “abrir componentes”); contentDescription = string “Componentes”.
- Lista do seletor mostra **todos** os itens de `listar()` (ordenados), não só o `LIMITE=8` do overflow — o brief pede `listar()`, não `ItensDoMenuOverflow…montar`.

## Preocupações

- Lista muito grande no Dialog linear pode ficar longa (sem scroll dedicado na caixa simples); overflow já limita a 8 — se virar problema, reutilizar `montar` ou ScrollView.
- Fechar janelas ao sair do editor permanece na Task 5.
