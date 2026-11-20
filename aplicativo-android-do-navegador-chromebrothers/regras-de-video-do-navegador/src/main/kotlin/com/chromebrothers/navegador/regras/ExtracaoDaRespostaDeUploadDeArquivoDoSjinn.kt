package com.chromebrothers.navegador.regras

/**
 * Parse da resposta de `POST /api/un-api/upload_file` (presign).
 * Espelha o cliente web: `data.signed_url`, `data.cdn_url`, `data.file_name`.
 */
object ExtracaoDaRespostaDeUploadDeArquivoDoSjinn {
    fun signedUrl(bruto: String): String? = valorEmData(bruto, "signed_url")
        ?: valorEmData(bruto, "upload_url")

    fun cdnUrl(bruto: String): String? = valorEmData(bruto, "cdn_url")
        ?: valorEmData(bruto, "file_url")
        ?: valorEmData(bruto, "url")

    fun fileName(bruto: String): String? = valorEmData(bruto, "file_name")
        ?: valorEmData(bruto, "filename")

    fun sucesso(bruto: String): Boolean =
        bruto.contains("\"success\":true") || bruto.contains("\"success\": true")

    private fun valorEmData(bruto: String, chave: String): String? {
        // Preferência: dentro de "data":{...}
        val dataIdx = bruto.indexOf("\"data\"")
        val escopo = if (dataIdx >= 0) bruto.substring(dataIdx) else bruto
        return TextoJsonDoChat.valorDaChave(escopo, chave)?.trim()?.takeIf { it.isNotEmpty() }
    }
}
