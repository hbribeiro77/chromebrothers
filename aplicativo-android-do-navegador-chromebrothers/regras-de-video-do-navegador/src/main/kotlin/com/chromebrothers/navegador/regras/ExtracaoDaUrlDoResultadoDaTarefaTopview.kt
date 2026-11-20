package com.chromebrothers.navegador.regras

/**
 * Extrai taskId / status / URL de vídeo de respostas Topview (REST ou tool MCP que ecoa o mesmo JSON).
 *
 * Status finais típicos: `success` | `fail` | `failed` | `error`.
 * Pendentes: `pending` | `running` | `processing` | `submitted` | vazio.
 */
object ExtracaoDaUrlDoResultadoDaTarefaTopview {
    private val STATUS_OK = setOf("success", "succeeded", "completed", "done")
    private val STATUS_FAIL = setOf("fail", "failed", "error", "cancelled", "canceled")

    private val regexTaskId =
        Regex(""""taskId"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
    private val regexStatus =
        Regex(""""status"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
    private val regexFilePath =
        Regex(""""filePath"\s*:\s*"(https?://[^"]+)"""", RegexOption.IGNORE_CASE)
    private val regexErrorMsg =
        Regex(""""errorMsg"\s*:\s*"(.*?)"""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val regexUrlVideo =
        Regex("""(https?://[^\s"']+\.mp4[^\s"']*)""", RegexOption.IGNORE_CASE)

    fun taskId(bruto: String): String? =
        regexTaskId.find(bruto)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }

    fun status(bruto: String): String =
        regexStatus.find(bruto)?.groupValues?.getOrNull(1)?.trim()?.lowercase().orEmpty()

    fun mensagemDeErro(bruto: String): String? {
        val msg = regexErrorMsg.find(bruto)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        if (msg.isEmpty() || msg.equals("null", true)) return null
        return msg.replace("\\n", "\n").replace("\\\"", "\"")
    }

    fun tarefaFalhou(bruto: String): Boolean {
        val st = status(bruto)
        if (st in STATUS_FAIL) return true
        val erro = mensagemDeErro(bruto)
        return erro != null && st.isNotEmpty() && st !in STATUS_OK
    }

    fun tarefaConcluida(bruto: String): Boolean {
        if (tarefaFalhou(bruto)) return false
        val st = status(bruto)
        if (st in STATUS_OK) return true
        // Sem status explícito, mas já veio filePath/mp4 (resposta sync com waitForCompletion).
        return st.isEmpty() && extrairUrlBruta(bruto) != null
    }

    fun urlDoVideo(bruto: String): String? {
        if (bruto.isBlank() || tarefaFalhou(bruto)) return null
        if (!tarefaConcluida(bruto)) return null
        return extrairUrlBruta(bruto)
    }

    private fun extrairUrlBruta(bruto: String): String? {
        val texto = bruto.replace("\\/", "/")
        regexFilePath.find(texto)?.groupValues?.getOrNull(1)?.let { return limpar(it) }
        return regexUrlVideo.find(texto)?.groupValues?.getOrNull(1)?.let { limpar(it) }
    }

    private fun limpar(url: String): String =
        url.trimEnd('.', ',', ')', ']', '"', '\'', '\\')
}
