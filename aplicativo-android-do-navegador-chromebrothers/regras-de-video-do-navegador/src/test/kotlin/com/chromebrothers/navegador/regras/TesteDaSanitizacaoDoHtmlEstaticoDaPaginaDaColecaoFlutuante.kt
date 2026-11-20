package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TesteDaSanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante {
    @Test
    fun permiteScriptLocalEOnclickMasRemoveScriptExternoEIframe() {
        val bruto = """
            <html><head><style>h1{color:red}</style></head>
            <body>
            <h1>Aula</h1>
            <img src="https://cdn.exemplo/a.png">
            <button onclick="chromebrothers.gerarPdf('aula')">PDF</button>
            <script>function x(){ chromebrothers.gerarPdf('aula'); }</script>
            <script src="https://evil.test/x.js"></script>
            <iframe src="https://evil.test"></iframe>
            <a href="javascript:alert(1)">x</a>
            <a href="https://sjinn.ai/aula">abrir</a>
            </body></html>
        """.trimIndent()
        val limpo = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(bruto)
        assertTrue(limpo.contains("<h1>Aula</h1>"))
        assertTrue(limpo.contains("chromebrothers.gerarPdf"))
        assertTrue(limpo.contains("onclick="))
        assertTrue(limpo.contains("<script>function x()"))
        assertTrue(limpo.contains("Content-Security-Policy"))
        assertTrue(limpo.contains("connect-src 'none'"))
        assertFalse(limpo.contains("evil.test/x.js"))
        assertFalse(limpo.contains("<iframe", ignoreCase = true))
        assertFalse(limpo.contains("javascript:", ignoreCase = true))
    }

    @Test
    fun oCspLiberaMidiaHttpEHttpsComoAsImagens() {
        val limpo = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(
            """<html><body><video src="http://cdn.exemplo/a.mp4"></video></body></html>""",
        )
        assertTrue(limpo.contains("media-src"))
        assertTrue(limpo.contains("img-src https: http: file: content: data: blob:"))
        assertTrue(limpo.contains("media-src https: http: file: content: blob:"))
        assertTrue(limpo.contains("connect-src 'none'"))
    }

    @Test
    fun removeObjectEmbedEMetaRefresh() {
        val bruto = """
            <meta http-equiv="refresh" content="0;url=https://evil.test">
            <object data="https://evil.test"></object>
            <embed src="https://evil.test">
            <p>ok</p>
        """.trimIndent()
        val limpo = SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante.aplicar(bruto)
        assertTrue(limpo.contains("<p>ok</p>"))
        assertFalse(limpo.contains("<object", ignoreCase = true))
        assertFalse(limpo.contains("<embed", ignoreCase = true))
        assertFalse(limpo.contains("refresh", ignoreCase = true))
    }
}
