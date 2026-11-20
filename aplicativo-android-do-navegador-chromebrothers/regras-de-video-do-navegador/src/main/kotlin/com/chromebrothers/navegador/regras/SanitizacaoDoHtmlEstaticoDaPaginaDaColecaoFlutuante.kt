package com.chromebrothers.navegador.regras

/**
 * HTML da mini-página da coleção: JS local ok; sem script remoto nem rede via fetch.
 */
object SanitizacaoDoHtmlEstaticoDaPaginaDaColecaoFlutuante {
    private const val CSP =
        """<meta http-equiv="Content-Security-Policy" content="default-src 'none'; img-src https: http: file: content: data: blob:; media-src https: http: file: content: blob:; style-src 'unsafe-inline'; script-src 'unsafe-inline'; font-src data: https: http:; connect-src 'none'; frame-src 'none'; object-src 'none'; base-uri 'none'; form-action 'none';">"""

    private val tagsProibidas = Regex(
        """</?\s*(iframe|object|embed|link)\b[^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val scriptComSrc = Regex(
        """<\s*script\b[^>]*\bsrc\s*=[^>]*>\s*(</\s*script\s*>)?""",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val metaRefresh = Regex(
        """<\s*meta\b[^>]*http-equiv\s*=\s*['\"]?\s*refresh\b[^>]*>""",
        RegexOption.IGNORE_CASE,
    )
    private val javascriptUrl = Regex(
        """(href|src)\s*=\s*(['"])\s*javascript:[^'"]*\2""",
        RegexOption.IGNORE_CASE,
    )
    private val dataHtml = Regex(
        """(href|src)\s*=\s*(['"])\s*data:text/html[^'"]*\2""",
        RegexOption.IGNORE_CASE,
    )
    private val cspExistente = Regex(
        """<\s*meta\b[^>]*http-equiv\s*=\s*['\"]?\s*Content-Security-Policy\b[^>]*>""",
        RegexOption.IGNORE_CASE,
    )

    fun aplicar(html: String): String {
        var texto = html
        texto = tagsProibidas.replace(texto, "")
        texto = scriptComSrc.replace(texto, "")
        texto = metaRefresh.replace(texto, "")
        texto = javascriptUrl.replace(texto, "")
        texto = dataHtml.replace(texto, "")
        texto = cspExistente.replace(texto, "")
        return injetarCsp(texto)
    }

    private fun injetarCsp(html: String): String {
        val head = Regex("""<\s*head\b[^>]*>""", RegexOption.IGNORE_CASE).find(html)
        if (head != null) {
            val fim = head.range.last + 1
            return html.substring(0, fim) + CSP + html.substring(fim)
        }
        val htmlTag = Regex("""<\s*html\b[^>]*>""", RegexOption.IGNORE_CASE).find(html)
        if (htmlTag != null) {
            val fim = htmlTag.range.last + 1
            return html.substring(0, fim) + "<head>$CSP</head>" + html.substring(fim)
        }
        return "<head>$CSP</head>$html"
    }
}
