# Task 4 review package (after fix)


===== FILE: app\src\main\kotlin\com\chromebrothers\navegador\ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers.kt =====

package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

object ExportacaoDeVideoDaBibliotecaParaDownloadsDoChromeBrothers {
    /** @return content uri string ou null on failure */
    fun exportar(context: Context, arquivoLocal: File, nomeExibido: String): String? {
        var uriInserida: Uri? = null
        return try {
            if (!arquivoLocal.isFile || arquivoLocal.length() <= 0L) return null
            val valores = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, nomeComExtensaoMp4(nomeExibido))
                put(MediaStore.Downloads.MIME_TYPE, "video/mp4")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val uri = context.contentResolver.insert(colecao, valores) ?: return null
            uriInserida = uri
            context.contentResolver.openOutputStream(uri)?.use { saida ->
                arquivoLocal.inputStream().use { it.copyTo(saida) }
            } ?: error("sem saida")
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            if (context.contentResolver.update(uri, valores, null, null) == 0) {
                apagarUriSemDeixarExcecaoEscapar(context, uri)
                return null
            }
            uri.toString()
        } catch (_: Exception) {
            apagarUriSemDeixarExcecaoEscapar(context, uriInserida)
            null
        }
    }

    private fun apagarUriSemDeixarExcecaoEscapar(context: Context, uri: Uri?) {
        if (uri == null) return
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (_: Exception) {
        }
    }

    private fun nomeComExtensaoMp4(nomeExibido: String): String {
        return if (nomeExibido.endsWith(".mp4", ignoreCase = true)) nomeExibido else "$nomeExibido.mp4"
    }
}

