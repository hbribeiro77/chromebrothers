# Task 2 — Editor: `fundirBitmapNaCamadaAtiva`

**Status:** DONE  
**Commits:** none  
**Módulo:** `app` (UI Android) + strings; consome regras da Task 1

## O que foi entregue

- `EditorDeRecorteETracoDaImagemDaBiblioteca.fundirBitmapNaCamadaAtiva(origem)` — decide destino via `EnvioDeImagemDoComponenteParaCamadaDoEditor.decidirDestino`, promove folha se necessário (`promoverFolhaParaImagem`), cria `Peca.Imagem` temporária centrada/dimensionada (aspect) e funde com `fundirPecaTransformadaNaOrigem`. Sucesso sempre retorna `FUNDIR_NA_IMAGEM` (como no brief). Falha de promote → `RECUSAR_SEM_DESTINO`.
- `PainelDaGavetaDaBibliotecaDeVideos.enviarBitmapDoComponenteParaCamadaAtiva(origem)` — só encaminha se `editandoImagem`; senão `RECUSAR_SEM_DESTINO`.
- Quatro strings novas em `strings_do_navegador_chromebrothers.xml` (nomes e textos literais do brief).

## Arquivos

- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/EditorDeRecorteETracoDaImagemDaBiblioteca.kt`
- Modify: `app/src/main/kotlin/com/chromebrothers/navegador/PainelDaGavetaDaBibliotecaDeVideos.kt`
- Modify: `app/src/main/res/values/strings_do_navegador_chromebrothers.xml`

Sem commit.

## Dimensionamento / centro

Sobre a destino (`Peca.Imagem`):

1. Escala px→coords = `destino.largura|altura / bitmap.width|height`.
2. Tamanho natural da origem nessa escala.
3. Se estourar a destino, fator `min(1, caber)` mantendo aspect.
4. Centrar: `esquerda/topo = destino + (destino.size - temp.size) / 2`.

## Compile check

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot"
& "$env:LOCALAPPDATA\gradle-8.11.1\bin\gradle.bat" :app:compileDebugKotlin
```

Saída: **BUILD SUCCESSFUL in 34s** (exit 0). Warnings pré-existentes de `statusBarColor`/`navigationBarColor` deprecated — não relacionados a esta task.

## Desvios em relação ao brief

Nenhum na API. Toast/`RECUSAR` na Activity fica para o caller da Task 3 (brief: “Toast/mensagem curta no caller da Activity”).

## Auto-revisão

- Reusa `promoverFolhaParaImagem` e `fundirPecaTransformadaNaOrigem` (undo já empilhado).
- Após promote bem-sucedido, retorno é `FUNDIR_NA_IMAGEM`, não o enum de promote.
- Wrapper exige `editandoImagem` antes de falar com o editor.
- Strings: `enviar_imagem_do_componente_para_a_camada_do_editor`, `recusa_envio_de_imagem_do_componente_sem_camada_no_editor`, `componentes_no_chrome_do_editor_de_imagem`, `falha_ao_ler_imagem_do_componente_para_o_editor`.

## Preocupações

- Peça temporária não verifica `PilhaDeCamadasDaComposicao.cabeMaisUma` (brief não pede; a peça some na fusão).
- Destino rotacionado: temp fica axis-aligned no AABB da destino (brief não pede herdar `angulo`).
