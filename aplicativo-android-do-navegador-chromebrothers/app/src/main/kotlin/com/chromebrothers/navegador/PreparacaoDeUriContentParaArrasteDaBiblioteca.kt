package com.chromebrothers.navegador

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Monta `content://` + MIME para arraste da biblioteca.
 * Quem inicia o drag concede FLAG_GRANT_READ_URI_PERMISSION na URI.
 */
object PreparacaoDeUriContentParaArrasteDaBiblioteca {
    data class Payload(val uri: Uri, val mime: String, val nome: String)

    fun deArquivoLocal(context: Context, arquivo: File, mimeForcado: String? = null): Payload? {
        if (!arquivo.isFile || arquivo.length() == 0L) return null
        val uri = try {
            FileProvider.getUriForFile(context, "${context.packageName}.arquivos", arquivo)
        } catch (_: Exception) {
            return null
        }
        return Payload(uri, mimeForcado ?: mimePelaExtensao(arquivo.name), arquivo.name)
    }

    private fun mimePelaExtensao(nome: String): String {
        return when {
            nome.endsWith(".mp4", ignoreCase = true) -> "video/mp4"
            nome.endsWith(".jpg", ignoreCase = true) ||
                nome.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            nome.endsWith(".png", ignoreCase = true) -> "image/png"
            nome.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "application/octet-stream"
        }
    }
}
