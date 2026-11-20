package com.chromebrothers.navegador.regras

/**
 * Extrai URL de saída e status de respostas da Tool API Sjinn.
 *
 * Importante: o envelope sempre traz `"success":true` mesmo com `status:0` (pendente)
 * e o `input` pode repetir a URL da referência — não usar isso como resultado.
 */
object ExtracaoDaUrlDoResultadoDaTarefaSjinn {
    private val regexOutputUrlsArray =
        Regex("""output_urls"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
    private val regexUrlEmAspas =
        Regex(""""(https?://[^"\s]+)"""")
    private val regexStatus =
        Regex(""""status"\s*:\s*(-?\d+)""")

    fun statusNumerico(bruto: String): Int? =
        regexStatus.find(bruto)?.groupValues?.getOrNull(1)?.toIntOrNull()

    fun tarefaFalhou(bruto: String): Boolean {
        val st = statusNumerico(bruto) ?: return false
        return st == -1 || st == 2
    }

    /** Só considera concluída com status numérico 1 (completed). */
    fun tarefaConcluida(bruto: String): Boolean {
        if (tarefaFalhou(bruto)) return false
        return statusNumerico(bruto) == 1
    }

    fun urlDoResultado(
        bruto: String,
        urlsDeEntrada: Set<String> = emptySet(),
    ): String? {
        if (bruto.isBlank() || tarefaFalhou(bruto)) return null
        if (!tarefaConcluida(bruto)) return null
        val texto = bruto.replace("\\/", "/")
        val entradas = urlsDeEntrada.map { normalizar(it) }.toSet()
        val doArray = urlsDeOutputUrls(texto)
            .map { limpar(it) }
            .firstOrNull { candidata ->
                candidata.isNotBlank() && normalizar(candidata) !in entradas
            }
        return doArray
    }

    private fun urlsDeOutputUrls(texto: String): List<String> {
        val bloco = regexOutputUrlsArray.find(texto)?.groupValues?.getOrNull(1) ?: return emptyList()
        return regexUrlEmAspas.findAll(bloco).map { it.groupValues[1] }.toList()
    }

    private fun limpar(url: String): String =
        url.trimEnd('.', ',', ')', ']', '"', '\'', '\\')

    private fun normalizar(url: String): String =
        limpar(url).trim().lowercase().substringBefore('?')
}
