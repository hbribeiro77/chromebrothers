# F2 Task 2 package


===== app\src\main\kotlin\com\chromebrothers\navegador\ExtracaoDeQuadroJpegDeVideoLocalParaArraste.kt =====

package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import java.io.File
import java.util.UUID

object ExtracaoDeQuadroJpegDeVideoLocalParaArraste {
    /** Grava JPEG em cacheDir/arraste-da-biblioteca/; null se falhar */
    fun extrairParaArquivo(context: Context, videoMp4: File): File? {
        if (!videoMp4.isFile || videoMp4.length() <= 0L) return null
        val retriever = MediaMetadataRetriever()
        var quadro: Bitmap? = null
        var destino: File? = null
        return try {
            retriever.setDataSource(videoMp4.absolutePath)
            val bitmap = retriever.getScaledFrameAtTime(
                1_000_000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                960,
                540,
            ) ?: return null
            quadro = bitmap
            if (bitmap.width <= 0 || bitmap.height <= 0) return null
            val pasta = File(context.cacheDir, "arraste-da-biblioteca")
            if (!pasta.mkdirs() && !pasta.isDirectory) return null
            val arquivo = File(pasta, "quadro-${UUID.randomUUID()}.jpg")
            destino = arquivo
            val gravou = arquivo.outputStream().use { saida ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 86, saida)
            }
            if (!gravou || !arquivo.isFile || arquivo.length() <= 0L) {
                arquivo.delete()
                destino = null
                return null
            }
            arquivo
        } catch (_: Exception) {
            destino?.delete()
            null
        } finally {
            quadro?.let { if (!it.isRecycled) it.recycle() }
            runCatching { retriever.release() }
        }
    }
}

