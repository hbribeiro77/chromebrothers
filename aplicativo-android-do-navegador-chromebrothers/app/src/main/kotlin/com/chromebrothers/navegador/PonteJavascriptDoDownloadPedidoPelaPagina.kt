package com.chromebrothers.navegador

import android.webkit.JavascriptInterface

class PonteJavascriptDoDownloadPedidoPelaPagina(
    private val aoReceber: (String, String) -> Unit,
    private val aoFalhar: () -> Unit,
) {
    @JavascriptInterface
    fun receber(nome: String, dados: String) {
        aoReceber(nome, dados)
    }

    @JavascriptInterface
    fun falhou() {
        aoFalhar()
    }
}
