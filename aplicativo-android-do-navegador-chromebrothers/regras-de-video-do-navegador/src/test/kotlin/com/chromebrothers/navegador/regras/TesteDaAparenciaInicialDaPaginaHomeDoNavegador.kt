package com.chromebrothers.navegador.regras

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TesteDaAparenciaInicialDaPaginaHomeDoNavegador {
    @Test
    fun homeEscuraJaNasceComClasseEFundoEscuros() {
        val aparencia = AparenciaInicialDaPaginaHomeDoNavegador.aPartirDoEstado(
            """{"escura":true,"motor":"google","modo":"computador","atalhos":[]}""",
        )
        assertEquals("escura", aparencia.classeDoCorpo)
        assertEquals("#202124", aparencia.fundoCss)
        assertEquals("dark", aparencia.esquemaDeCor)
    }

    @Test
    fun homeClaraMantemFundoClaro() {
        val aparencia = AparenciaInicialDaPaginaHomeDoNavegador.aPartirDoEstado(
            """{"escura":false,"motor":"google","modo":"computador","atalhos":[]}""",
        )
        assertEquals("", aparencia.classeDoCorpo)
        assertEquals("#ffffff", aparencia.fundoCss)
        assertEquals("light", aparencia.esquemaDeCor)
    }

    @Test
    fun aplicaMarcadoresNoDocumentoAntesDoJavascript() {
        val bruto = """
            <html style="color-scheme:%%ESQUEMA%%;background:%%FUNDO%%">
            <body id="pagina-inicial-chromebrothers" class="%%CLASSE_CORPO%%">
            </body></html>
        """.trimIndent()
        val html = AparenciaInicialDaPaginaHomeDoNavegador.aplicarNoDocumento(
            bruto,
            AparenciaInicialDaPaginaHomeDoNavegador.aPartirDoEstado("""{"escura":true}"""),
        )
        assertTrue(html.contains("class=\"escura\""))
        assertTrue(html.contains("background:#202124"))
        assertTrue(html.contains("color-scheme:dark"))
        assertEquals(-1, html.indexOf("%%"))
    }
}
