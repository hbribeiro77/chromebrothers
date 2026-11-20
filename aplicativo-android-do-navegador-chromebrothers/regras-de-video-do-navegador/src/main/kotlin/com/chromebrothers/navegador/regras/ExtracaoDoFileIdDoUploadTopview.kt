package com.chromebrothers.navegador.regras

/** Extrai `fileId` de resposta de upload Topview (tool MCP ou REST). */
object ExtracaoDoFileIdDoUploadTopview {
    private val regexFileId =
        Regex(""""fileId"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)

    fun fileId(bruto: String): String? =
        regexFileId.find(bruto)?.groupValues?.getOrNull(1)?.trim()?.takeIf { it.isNotEmpty() }
}
