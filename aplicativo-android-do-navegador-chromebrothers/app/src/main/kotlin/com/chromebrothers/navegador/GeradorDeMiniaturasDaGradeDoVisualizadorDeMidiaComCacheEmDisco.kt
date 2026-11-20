package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import java.io.File
import java.io.FileInputStream

/**
 * Gera JPEGs leves (imagem ou 1º quadro de vídeo) para a grade do Visualizador e guarda em disco.
 * Usado pelo intercept da WebView (`?cb_thumb=`) e pela pré-geração na abertura do app,
 * com a mesma chave — o que a pré-geração grava a WebView só lê.
 */
class GeradorDeMiniaturasDaGradeDoVisualizadorDeMidiaComCacheEmDisco(
    private val contexto: Context,
) {
    private val pasta: File by lazy {
        File(contexto.cacheDir, "miniaturas-da-grade-da-colecao").apply { mkdirs() }
    }

    fun arquivoEmCacheDeConteudo(uriLimpa: Uri, ehVideo: Boolean, ladoMax: Int): File? =
        obterOuGerar(chaveDeConteudo(uriLimpa, ladoMax)) {
            if (ehVideo) quadroDeVideoDeConteudo(uriLimpa, ladoMax)
            else contexto.contentResolver.openInputStream(uriLimpa)?.use { decodificarImagem(it, ladoMax) }
        }

    fun arquivoEmCacheDeArquivo(arquivo: File, ehVideo: Boolean, ladoMax: Int): File? =
        obterOuGerar("f:${arquivo.absolutePath}:$ladoMax:${arquivo.lastModified()}") {
            if (ehVideo) quadroDeVideoDeArquivo(arquivo, ladoMax)
            else FileInputStream(arquivo).use { decodificarImagem(it, ladoMax) }
        }

    /** Pré-gera a miniatura de uma uri da listagem (content:// ou file://); ignora falhas. */
    fun pregerar(uriTexto: String, mime: String, ladoMax: Int) {
        runCatching {
            val uri = Uri.parse(uriTexto)
            val ehVideo = mime.lowercase().startsWith("video/") ||
                Regex("""\.(mp4|webm|mkv|mov|m4v|avi)$""", RegexOption.IGNORE_CASE)
                    .containsMatchIn(uri.lastPathSegment.orEmpty())
            when (uri.scheme) {
                "content" -> arquivoEmCacheDeConteudo(uri, ehVideo, ladoMax)
                "file" -> arquivoEmCacheDeArquivo(File(uri.path.orEmpty()), ehVideo, ladoMax)
                else -> null
            }
        }
    }

    private fun chaveDeConteudo(uriLimpa: Uri, ladoMax: Int): String = "c:${uriLimpa}:$ladoMax"

    private fun obterOuGerar(chave: String, gerador: () -> ByteArray?): File? {
        val nome = Integer.toHexString(chave.hashCode()) + ".jpg"
        val cache = File(pasta, nome)
        if (cache.isFile && cache.length() > 0L) return cache
        val bytes = gerador() ?: return null
        val temporario = File(pasta, "$nome.${Thread.currentThread().id}.tmp")
        temporario.writeBytes(bytes)
        if (!temporario.renameTo(cache)) {
            temporario.delete()
        }
        return cache.takeIf { it.isFile && it.length() > 0L }
    }

    private fun quadroDeVideoDeConteudo(uri: Uri, ladoMax: Int): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(contexto, uri)
            quadroDoRetriever(retriever, ladoMax)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun quadroDeVideoDeArquivo(arquivo: File, ladoMax: Int): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(arquivo.absolutePath)
            quadroDoRetriever(retriever, ladoMax)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun quadroDoRetriever(retriever: MediaMetadataRetriever, ladoMax: Int): ByteArray? {
        val quadro = if (android.os.Build.VERSION.SDK_INT >= 27) {
            retriever.getScaledFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, ladoMax, ladoMax)
        } else {
            retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } ?: return null
        return comprimir(reduzir(quadro, ladoMax))
    }

    private fun decodificarImagem(entrada: java.io.InputStream, ladoMax: Int): ByteArray? {
        val marcado = if (entrada.markSupported()) entrada else java.io.BufferedInputStream(entrada, 64 * 1024)
        marcado.mark(64 * 1024)
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(marcado, null, limites)
        runCatching { marcado.reset() }
        var amostra = 1
        val w = limites.outWidth.coerceAtLeast(1)
        val h = limites.outHeight.coerceAtLeast(1)
        while (w / amostra > ladoMax * 2 || h / amostra > ladoMax * 2) amostra *= 2
        val opts = BitmapFactory.Options().apply {
            inSampleSize = amostra
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bruto = BitmapFactory.decodeStream(marcado, null, opts) ?: return null
        return comprimir(reduzir(bruto, ladoMax))
    }

    private fun reduzir(bruto: Bitmap, ladoMax: Int): Bitmap {
        val escala = minOf(ladoMax.toFloat() / bruto.width, ladoMax.toFloat() / bruto.height, 1f)
        if (escala >= 0.999f) return bruto
        return Bitmap.createScaledBitmap(
            bruto,
            (bruto.width * escala).toInt().coerceAtLeast(1),
            (bruto.height * escala).toInt().coerceAtLeast(1),
            true,
        ).also { if (it !== bruto) bruto.recycle() }
    }

    private fun comprimir(bitmap: Bitmap): ByteArray {
        val saida = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 72, saida)
        if (!bitmap.isRecycled) bitmap.recycle()
        return saida.toByteArray()
    }
}
