package com.chromebrothers.navegador.regras

/**
 * Parse e JSON de resultado da exclusão em lote pedida pelo Visualizador de mídia.
 */
object ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador {
    fun parseUris(urisJson: String?): List<String> {
        val bruto = urisJson?.trim().orEmpty()
        if (bruto.length < 2 || bruto.first() != '[' || bruto.last() != ']') return emptyList()
        val corpo = bruto.substring(1, bruto.length - 1).trim()
        if (corpo.isEmpty()) return emptyList()
        val resultado = mutableListOf<String>()
        var i = 0
        while (i < corpo.length) {
            while (i < corpo.length && (corpo[i].isWhitespace() || corpo[i] == ',')) i++
            if (i >= corpo.length) break
            if (corpo[i] != '"') return emptyList()
            i++
            val uri = StringBuilder()
            while (i < corpo.length) {
                val c = corpo[i]
                if (c == '\\' && i + 1 < corpo.length) {
                    uri.append(corpo[i + 1])
                    i += 2
                    continue
                }
                if (c == '"') {
                    i++
                    break
                }
                uri.append(c)
                i++
            }
            val limpo = uri.toString().trim()
            if (limpo.isNotEmpty()) resultado += limpo
        }
        return resultado
    }

    fun mensagemSemUris(): String =
        """{"ok":false,"motivo":"Nenhum arquivo selecionado.","excluidos":0,"falhas":0,"urisExcluidas":[],"urisComFalha":[]}"""

    fun mensagemResultado(
        excluidos: List<String>,
        falhas: List<String>,
    ): String {
        val ok = excluidos.isNotEmpty()
        return """{"ok":$ok,"excluidos":${excluidos.size},"falhas":${falhas.size},"urisExcluidas":${arrayJson(excluidos)},"urisComFalha":${arrayJson(falhas)}}"""
    }

    private fun arrayJson(uris: List<String>): String =
        uris.joinToString(prefix = "[", postfix = "]") { "\"${escapar(it)}\"" }

    private fun escapar(texto: String): String = buildString(texto.length + 8) {
        for (c in texto) {
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}
