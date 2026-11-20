# Task 6 review package (focused excerpts)

## ContextoDaPaginaParaOChat.kt (full)
See file — scriptDoTextoCompletoNormalizado without slice; scriptDoTextoVisivel delegates; mensagem still take(LIMITE).

## Painel ctor + when
- Callbacks lerTextoPaginado, consultarElementos, rolarPagina added
- when: TEXTO_PAGINADO / ELEMENTOS / ROLAR pass chamada.argumentos

## AtividadePrincipal wiring (lines ~471-536)
- lerTextoPaginado: lerPedido + lerTextoCompletoDaAba + normalizar + fatiar + mensagem
- consultarElementos: Erro early; Ok → evaluateJavascript(script) → descrever(textoDoJavascript)
- rolarPagina: same with Rolagem
- lerTextoVisivelDaAba → lerTextoCompletoDaAba + take(LIMITE)
- lerTextoCompletoDaAba uses scriptDoTextoCompletoNormalizado

## Verify (implementer report)
- :regras-de-video-do-navegador:test + :app:assembleDebug SUCCESS
- adb install Success
