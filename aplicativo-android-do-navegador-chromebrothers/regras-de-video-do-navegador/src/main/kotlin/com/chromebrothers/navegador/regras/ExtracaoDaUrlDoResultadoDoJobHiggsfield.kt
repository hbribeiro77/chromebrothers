package com.chromebrothers.navegador.regras

/**
 * Extrai a URL do **resultado** de um job Higgsfield, evitando a URL da imagem de
 * referência que aparece em `job_status` enquanto o job ainda está `in_progress`
 * (`type: media_input`, `*_resize.jpg`, `upload.higgsfield.ai`).
 */
object ExtracaoDaUrlDoResultadoDoJobHiggsfield {
    private val regexUrl = Regex("""https?://[^\s"'<>\\]+""")
    private val regexResultUrl =
        Regex("""result_url["\s:=]+"?\s*(https?://[^\s"'<>\\]+)""", RegexOption.IGNORE_CASE)

    fun jobFalhou(bruto: String): Boolean {
        val t = bruto.lowercase()
        // NÃO use contains("failed")/"fail": o summary do jobs_wait traz `"failed":0`
        // e isso abortava o poll assim que o primeiro wait expirava (~15–30s).
        if (Regex(""""status"\s*:\s*"failed"""").containsMatchIn(t)) return true
        if (Regex("""—\s*failed\b""").containsMatchIn(t)) return true
        if (Regex(""",failed,(?:image|video|audio)\b""").containsMatchIn(t)) return true
        // Contador real de falhas no summary (failed: N com N>0).
        if (Regex(""""failed"\s*:\s*([1-9]\d*)""").containsMatchIn(t)) return true
        if (Regex("""\bfailed:\s*([1-9]\d*)\b""").containsMatchIn(t)) return true
        return false
    }

    fun jobConcluido(bruto: String): Boolean {
        if (jobFalhou(bruto)) return false
        val t = bruto.lowercase()
        if (t.contains("in_progress") || t.contains("\"pending\"")) {
            // jobs_wait lista "completed" no mesmo blob às vezes; exige completed explícito.
            if (!t.contains("completed") && !t.contains("\"success\"") && !t.contains("\"done\"")) {
                return false
            }
        }
        return t.contains("completed") || t.contains("\"success\"") || t.contains("\"done\"") ||
            t.contains("result_url")
    }

    fun urlDoResultado(bruto: String): String? {
        if (bruto.isBlank() || jobFalhou(bruto)) return null
        // job_status in_progress traz só a URL da referência — não baixar.
        if (!jobConcluido(bruto)) return null
        val texto = bruto.replace("\\/", "/")
        regexResultUrl.find(texto)?.groupValues?.getOrNull(1)?.let { candidata ->
            val limpa = limpar(candidata)
            if (!ehUrlDeEntrada(limpa)) return limpa
        }
        return regexUrl.findAll(texto)
            .map { limpar(it.value) }
            .firstOrNull { url -> pareceUrlDeMidia(url) && !ehUrlDeEntrada(url) }
    }

    private fun limpar(url: String): String =
        url.trimEnd('.', ',', ')', ']', '"', '\'', '\\')

    private fun pareceUrlDeMidia(url: String): Boolean {
        val l = url.lowercase()
        return l.contains(".png") || l.contains(".jpg") || l.contains(".jpeg") ||
            l.contains(".webp") || l.contains(".mp4") || l.contains(".webm") ||
            l.contains("cdn") || l.contains("cloudfront") ||
            l.contains("storage") || l.contains("higgsfield")
    }

    /** URLs da referência / upload, não do arquivo gerado. */
    fun ehUrlDeEntrada(url: String): Boolean {
        val l = url.lowercase()
        return l.contains("upload.higgsfield.ai") ||
            l.contains("_resize.") ||
            l.contains("media_input")
    }
}
