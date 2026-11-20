# Relatório - correções finais da feature de OCR

Status: concluído (sem commit, sem adb/smoke).

## Fix 1 - mensagemDeFalha
Arquivo: ExtracaoDeTextoDeImagemViaIaDoNavegador.kt. Palavras-chave: vision, multimodal, image_url, image input, support image, supports image; "does not support"/"não suport" só com image ou vision. Nunca devolve texto bruto da API.
Testes novos: "No endpoints found that support image input" -> SEM_VISAO; "model does not support streaming" -> GENERICA; null -> GENERICA.

## Fix 2 - Modal landscape
layout_modal_da_extracao_de_texto_de_imagem_via_ia_do_navegador.xml: ScrollView com layout_height=0dp e layout_weight=1.

## Fix 3 - OOM
OrquestradorDaExtracaoDeTextoDeImagemViaIaDoNavegador.kt: decodeByteArray e paraParte com catch de OutOfMemoryError e Exception -> mostrarFalhaImediata (modal de falha).

## Verificação
gradle :regras-de-video-do-navegador:test (TesteDaExtracaoDeTextoDeImagemViaIaDoNavegador) + :app:assembleDebug: BUILD SUCCESSFUL.
