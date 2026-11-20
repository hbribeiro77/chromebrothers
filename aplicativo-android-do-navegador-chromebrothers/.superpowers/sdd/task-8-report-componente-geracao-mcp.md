# Task 8 — Verificação no tablet (Geração MCP — imagem)

**Status: DONE_WITH_CONCERNS**

Este relatório substitui o anterior (BLOCKED). Agora o tablet estava online e havia sessão Higgsfield, então os 6 itens foram exercitados.

## Ambiente

- Dispositivo: `RX2T7007XMD` (SM-X906B), `adb devices` = `device`.
- APK debug (`assembleDebug` + `adb install -r`) instalado pelo controller antes desta verificação.
- App: `com.chromebrothers.navegador`.
- Modelo usado em todas as gerações: "Higgsfield Soul 2.0" (`soul_2`), prompt `a red apple on a white table`.
- Nenhum código foi alterado e nenhum commit foi feito.

## Como abrir o componente

Ícone "Abrir a biblioteca" na barra → aba **Componentes** → item "Geração MCP — imagem". A coleção flutuante abre com o componente carregado ("Da biblioteca").

A janela abre parcialmente fora da tela, então precisei arrastá-la pelo título e redimensioná-la.

## Checklist

| # | Item | Resultado | Evidência |
|---|------|-----------|-----------|
| 1 | Abrir coleção → componente "Geração MCP — imagem" na biblioteca | **PASSOU** | Aba Componentes lista "Geração MCP — imagem" (tags mcp/higgsfield/imagem, 5 nov 23:24). Ao tocar, abre a janela flutuante com MCP, Prompt, Imagem de referência (Galeria/Biblioteca/Limpar), Modelo, Gerações (1–4) e Gerar. `t8_2.jpg`, `t8_6.jpg` |
| 2 | Sem Higgsfield ligado → erro claro ao listar/gerar | **PASSOU (ressalva)** | Removi temporariamente `sessao_da_conta_higgsfield.xml` (com backup), reiniciei o app e abri o componente. O select de modelo mostra "Erro: Ligue o Higgsfield na engrenagem do chat." e o status mostra, em vermelho, "Ligue o Higgsfield na engrenagem do chat.". Depois de preencher o prompt e tocar em Gerar, a mensagem continuou a mesma. Como ela é igual à da listagem, não consegui distinguir qual das duas chamadas a produziu. `t8_28.jpg`. A sessão foi restaurada (`cmp` idêntico ao backup) e o backup foi apagado |
| 3 | Com sessão: listar modelos preenche select | **PASSOU** | O select abre com o catálogo real: Higgsfield Soul 2.0, Soul Cinema, GPT Image 2, Cinema Studio Image 2.5, Marketing Studio Image, DTC Ads, Auto, AutoSprite Animation, Soul Cast, Soul Location… Depois de restaurar a sessão, o select voltou a ser preenchido (`t8_31.jpg`) |
| 4 | Gerar 1 sem ref → aparece na grade + biblioteca | **PASSOU** | Progresso "Gerando imagem 1 de 1…", depois "Pronto: 1 imagem(ns) salva(s) na biblioteca." com a miniatura na grade (`t8_10.jpg`). Biblioteca > Imagens tem a entrada de 23:34 (`t8_26.jpg`). Log: `generate_image {model:soul_2, prompt, aspect_ratio:1:1}` |
| 5a | Gerar com ref (biblioteca) | **PASSOU** | Ref escolhida em "Biblioteca" (JPEG). Log: `media_upload`, `PUT referencia HTTP 200`, `media_confirm` "Confirmed 1 upload", `generate_image` com `medias:[{value:<id>,role:image}]`. UI mostrou "Pronto: 1 imagem(ns)…" e a imagem foi salva às 23:40 (`t8_16.jpg`, `t8_18.jpg`, `t8_26.jpg`) |
| 5b | Gerar com ref (galeria) | **PASSOU com JPEG; FALHOU com PNG (concern)** | O seletor de fotos abre e a miniatura aparece. Com um **JPEG** (`1000043785.jpg`) o fluxo completo funcionou (ver item 6). Com um **PNG** (`1000043798.png`, screenshot) o `media_confirm` do Higgsfield falhou: "Confirm failed: Something went wrong. Please try again. Request ID 7ab78613-…". O erro apareceu em vermelho na UI (`t8_14.jpg`), e o `PUT` do arquivo tinha dado HTTP 200. Tentei o PNG uma única vez, então não sei se foi falha transitória do servidor ou problema com PNG. Nenhum item foi salvo nessa tentativa |
| 6 | Quantidade 2 → dois itens (ou fallback de 2 jobs) | **PASSOU (via fallback de 2 jobs)** | Gerações = 2, com ref de galeria (JPEG). O app roda **2 jobs sequenciais de `generate_image`** (cada um com seu `media_upload`/`confirm`), não 1 job com `count=2`. UI: "Pronto: 2 imagem(ns) salva(s) na biblioteca." (`t8_22.jpg`). Biblioteca > Imagens tem 2 entradas às 23:43 (`t8_26.jpg`) |

## Concerns

1. **PNG da galeria falhou no `media_confirm`** (item 5b). A causa mais provável é o servidor, mas não foi investigada. O app tratou bem: mostrou o erro do Higgsfield em vermelho e não quebrou. Vale uma segunda tentativa com outro PNG, ou converter PNG para JPEG antes do upload, se o problema se repetir.
2. **Item 2** mostra a mesma mensagem na listagem e no Gerar, sem como separar as duas no teste. O caminho `SEM_SESSAO` do Gerar existe no código (`gerarEAgregar`) e não faz chamada de rede.
3. **Janela flutuante abre parcialmente fora da tela** (cortada embaixo) e pequena; foi preciso arrastar e redimensionar para usar o formulário.
4. **A grade acumula miniaturas** entre gerações, o que é aceitável.
5. **O adb ficou instável** durante a verificação. Há outro adb (v40) brigando com o do SDK (v41), que reinicia o servidor e derruba o device por alguns segundos.
6. A tela do tablet girou para retrato no final (autorrotação). Não afeta o app.
7. A geração real consumiu créditos do Higgsfield: 4 jobs de imagem (1 sem ref, 1 com ref da biblioteca, 2 com ref da galeria JPEG). A tentativa com PNG falhou antes de criar o job.

## Dados e limpeza

- Sessão Higgsfield: removida temporariamente só para o item 2, **restaurada** logo depois (arquivo idêntico ao backup, listagem voltou a funcionar). O backup em `files/` do app foi apagado.
- Evidências (screenshots reduzidos) em `.superpowers/sdd/t8_*.jpg`: `t8_2`, `t8_6`, `t8_10`, `t8_13`, `t8_14`, `t8_16`, `t8_18`, `t8_22`, `t8_26`, `t8_28`, `t8_31`.
- Os tokens e o conteúdo do arquivo de sessão não foram copiados para este relatório.
