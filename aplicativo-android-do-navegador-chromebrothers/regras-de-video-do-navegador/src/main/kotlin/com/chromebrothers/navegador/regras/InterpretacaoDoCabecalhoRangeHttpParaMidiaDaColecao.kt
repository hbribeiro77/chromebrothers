package com.chromebrothers.navegador.regras

/**
 * Interpreta `Range: bytes=…` para servir 206 em mídia local/content da coleção,
 * e extrai `cb_thumb` da query (miniatura da grade do visualizador).
 */
object InterpretacaoDoCabecalhoRangeHttpParaMidiaDaColecao {
    data class Faixa(
        val inicio: Long,
        val fim: Long,
        val total: Long,
    ) {
        val tamanhoDaFaixa: Long get() = fim - inicio + 1
        val cabecalhoContentRange: String get() = "bytes $inicio-$fim/$total"
    }

    private val padrao = Regex("""(?i)bytes\s*=\s*(?:(\d+)-(\d*)|(-\d+))""")

    fun interpretar(cabecalho: String?, tamanhoTotal: Long): Faixa? {
        if (tamanhoTotal <= 0L) return null
        val bruto = cabecalho?.trim().orEmpty()
        if (bruto.isEmpty()) return null
        val m = padrao.find(bruto) ?: return null
        val (inicio, fim) = when {
            m.groupValues[3].isNotEmpty() -> {
                val sufixo = m.groupValues[3].toLongOrNull()?.unaryMinus() ?: return null
                if (sufixo <= 0L) return null
                val ini = (tamanhoTotal - sufixo).coerceAtLeast(0L)
                ini to (tamanhoTotal - 1)
            }
            else -> {
                val ini = m.groupValues[1].toLongOrNull() ?: return null
                val fimTxt = m.groupValues[2]
                val fimVal = if (fimTxt.isEmpty()) tamanhoTotal - 1
                else fimTxt.toLongOrNull() ?: return null
                ini to fimVal
            }
        }
        if (inicio < 0L || fim < inicio || inicio >= tamanhoTotal) return null
        return Faixa(inicio = inicio, fim = fim.coerceAtMost(tamanhoTotal - 1), total = tamanhoTotal)
    }

    fun ladoDaMiniatura(uriTexto: String?): Int? {
        if (uriTexto.isNullOrBlank()) return null
        val m = Regex("""[?&]cb_thumb=(\d+)""").find(uriTexto) ?: return null
        val lado = m.groupValues[1].toIntOrNull() ?: return null
        return lado.coerceIn(32, 2048)
    }

    /**
     * A grade do Visualizador usa `?cb_thumb=` no `src` da carta; arraste/fundir/colar
     * devem ler a mídia cheia, sem esse parâmetro.
     */
    fun uriSemParametroDeMiniatura(uriTexto: String?): String {
        val bruto = uriTexto?.trim().orEmpty()
        if (bruto.isEmpty()) return ""
        if (!bruto.contains("cb_thumb=", ignoreCase = true)) return bruto
        val semFragmento = bruto.substringBefore('#')
        val fragmento = if ('#' in bruto) "#" + bruto.substringAfter('#') else ""
        val base = semFragmento.substringBefore('?', missingDelimiterValue = semFragmento)
        val query = semFragmento.substringAfter('?', missingDelimiterValue = "")
        if (query.isEmpty()) return bruto
        val restante = query.split('&')
            .filter { it.isNotEmpty() && !it.startsWith("cb_thumb=", ignoreCase = true) }
        return if (restante.isEmpty()) {
            base + fragmento
        } else {
            base + "?" + restante.joinToString("&") + fragmento
        }
    }
}
