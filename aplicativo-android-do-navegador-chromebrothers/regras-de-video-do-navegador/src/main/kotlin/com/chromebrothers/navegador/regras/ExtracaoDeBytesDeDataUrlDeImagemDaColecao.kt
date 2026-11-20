package com.chromebrothers.navegador.regras

import java.util.Base64

/** Extrai bytes de um `data:image/...;base64,...` gerado por canvas (quadro de vídeo). */
object ExtracaoDeBytesDeDataUrlDeImagemDaColecao {
    private val regex =
        Regex("""^data:image/(?:jpeg|jpg|png|webp);base64,([A-Za-z0-9+/=\s]+)$""", RegexOption.IGNORE_CASE)

    fun bytes(dataUrl: String): ByteArray? {
        val bruto = dataUrl.trim()
        if (bruto.isEmpty()) return null
        val grupo = regex.find(bruto)?.groupValues?.getOrNull(1) ?: return null
        return try {
            Base64.getDecoder().decode(grupo.replace("\\s".toRegex(), ""))
        } catch (_: Exception) {
            null
        }
    }

    fun extensao(dataUrl: String): String {
        val baixo = dataUrl.trim().lowercase()
        return when {
            baixo.startsWith("data:image/png") -> "png"
            baixo.startsWith("data:image/webp") -> "webp"
            else -> "jpg"
        }
    }
}
