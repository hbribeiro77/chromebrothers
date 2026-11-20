# F2 Task 1 package


===== app\src\main\res\xml\caminhos_de_arquivo_do_quadro_copiado_chromebrothers.xml =====

<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="quadros_copiados" path="quadros/" />
    <cache-path name="capturas_da_camera_pedidas_pela_pagina" path="capturas-da-camera-pedidas-pela-pagina/" />
    <cache-path name="arquivos_enviados_pela_pagina" path="arquivos-enviados-pela-pagina/" />
    <cache-path name="capturas_nativas_de_tela" path="capturas-nativas-de-tela/" />
    <files-path name="imagens_capturadas" path="biblioteca-de-imagens-capturadas/" />
    <files-path name="biblioteca_de_videos_higgsfield" path="biblioteca-de-videos-higgsfield/" />
    <files-path name="refs_de_geracao_da_biblioteca" path="refs-de-geracao-da-biblioteca/" />
    <cache-path name="arraste_da_biblioteca" path="arraste-da-biblioteca/" />
</paths>


===== app\src\main\kotlin\com\chromebrothers\navegador\PreparacaoDeUriContentParaArrasteDaBiblioteca.kt =====

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

