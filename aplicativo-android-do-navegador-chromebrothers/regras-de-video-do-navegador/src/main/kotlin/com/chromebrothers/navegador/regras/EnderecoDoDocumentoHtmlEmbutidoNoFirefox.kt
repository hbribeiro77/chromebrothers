package com.chromebrothers.navegador.regras

import java.util.Base64

object EnderecoDoDocumentoHtmlEmbutidoNoFirefox {
    fun montar(html: String, mime: String): String {
        val tipo = mime.ifBlank { "text/html" }
        val corpo = Base64.getEncoder().encodeToString(html.toByteArray(Charsets.UTF_8))
        return "data:$tipo;charset=utf-8;base64,$corpo"
    }
}
