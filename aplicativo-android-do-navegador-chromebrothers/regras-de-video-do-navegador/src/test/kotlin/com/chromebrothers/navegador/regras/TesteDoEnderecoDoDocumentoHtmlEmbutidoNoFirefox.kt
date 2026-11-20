package com.chromebrothers.navegador.regras

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDoEnderecoDoDocumentoHtmlEmbutidoNoFirefox {
    @Test
    fun oDocumentoEmbutidoNaoDeixaOCerquilhaCortarOEndereco() {
        val html = "<style>body{background:#ffffff}</style><h1>ChromeBrothers</h1>"
        val endereco = EnderecoDoDocumentoHtmlEmbutidoNoFirefox.montar(html, "text/html")
        assertTrue(endereco.startsWith("data:text/html;charset=utf-8;base64,"))
        val corpo = endereco.substringAfter(",")
        assertFalse(corpo.contains("#"))
        assertEquals(html, String(Base64.getDecoder().decode(corpo), Charsets.UTF_8))
    }
}
