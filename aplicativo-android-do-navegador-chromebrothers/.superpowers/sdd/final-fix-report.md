# Final fix report — histórico de conversas do chat

Status: DONE (sem commit)

## I1 — Título usa contexto da página
- `TituloDaConversaDoChatDaOpenRouter.gerar` agora usa o texto após `\n\nPergunta:\n` (se existir) antes de colapsar espaços e cortar.
- Novo teste `tituloUsaApenasAPerguntaQuandoMensagemTemContextoDaPagina`.

## I2 — Entrada de histórico além do medidor
- Novo `TextView` `botao_historico_de_conversas_do_chat` ("Histórico") entre o medidor e "Nova conversa" no layout.
- String `historico_de_conversas_do_chat` = "Histórico".
- `Painel.ligar()` liga o clique a `abrirListaDeConversas()` (continua `private`).

## Verificação
`:regras-de-video-do-navegador:test --tests TesteDoTituloDaConversaDoChatDaOpenRouter` + `:app:assembleDebug` → BUILD SUCCESSFUL.

## Arquivos alterados
- regras-de-video-do-navegador/src/main/kotlin/.../TituloDaConversaDoChatDaOpenRouter.kt
- regras-de-video-do-navegador/src/test/kotlin/.../TesteDoTituloDaConversaDoChatDaOpenRouter.kt
- app/src/main/kotlin/.../PainelDoChatDaOpenRouterNoNavegador.kt
- app/src/main/res/layout/layout_atividade_principal_do_navegador_chromebrothers.xml
- app/src/main/res/values/strings_do_navegador_chromebrothers.xml
