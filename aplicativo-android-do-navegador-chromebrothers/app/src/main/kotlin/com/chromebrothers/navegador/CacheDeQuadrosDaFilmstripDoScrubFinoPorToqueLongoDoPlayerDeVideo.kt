package com.chromebrothers.navegador

import android.graphics.Bitmap
import android.util.LruCache
import com.chromebrothers.navegador.regras.ScrubFinoPorToqueLongoNoPlayerDeVideo

/**
 * Cache em memória dos quadros da filmstrip do scrub fino.
 * Sobrevive a sair/entrar no modo no mesmo processo; separado por vídeo+tempo.
 */
object CacheDeQuadrosDaFilmstripDoScrubFinoPorToqueLongoDoPlayerDeVideo {
    private const val MAXIMO_DE_BYTES = 12 * 1024 * 1024

    private val cache = object : LruCache<String, Bitmap>(MAXIMO_DE_BYTES) {
        override fun sizeOf(chave: String, bitmap: Bitmap): Int {
            return bitmap.byteCount.coerceAtLeast(1)
        }
    }

    @Synchronized
    fun obter(chaveDoVideo: String, tempoMs: Long, passoMs: Long): Bitmap? {
        val chave = ScrubFinoPorToqueLongoNoPlayerDeVideo.chaveDoQuadroEmCache(chaveDoVideo, tempoMs, passoMs)
        if (chave.isEmpty()) return null
        val bitmap = cache.get(chave) ?: return null
        if (bitmap.isRecycled) {
            cache.remove(chave)
            return null
        }
        return bitmap
    }

    @Synchronized
    fun guardar(chaveDoVideo: String, tempoMs: Long, passoMs: Long, bitmap: Bitmap) {
        if (bitmap.isRecycled || bitmapQuasePreto(bitmap)) return
        val chave = ScrubFinoPorToqueLongoNoPlayerDeVideo.chaveDoQuadroEmCache(chaveDoVideo, tempoMs, passoMs)
        if (chave.isEmpty()) return
        val existente = cache.get(chave)
        if (existente != null && !existente.isRecycled) return
        cache.put(chave, bitmap)
    }

    @Synchronized
    fun limparVideo(chaveDoVideo: String) {
        val prefixo = chaveDoVideo.trim()
        if (prefixo.isEmpty()) return
        val remover = cache.snapshot().keys.filter { it.startsWith("$prefixo|") }
        remover.forEach { cache.remove(it) }
    }

    private fun bitmapQuasePreto(bitmap: Bitmap): Boolean {
        val largura = bitmap.width.coerceAtLeast(1)
        val altura = bitmap.height.coerceAtLeast(1)
        val passoX = (largura / 8).coerceAtLeast(1)
        val passoY = (altura / 8).coerceAtLeast(1)
        var amostras = 0
        var soma = 0L
        var y = 0
        while (y < altura) {
            var x = 0
            while (x < largura) {
                val cor = bitmap.getPixel(x, y)
                soma += ((cor shr 16) and 0xFF) + ((cor shr 8) and 0xFF) + (cor and 0xFF)
                amostras++
                x += passoX
            }
            y += passoY
        }
        return amostras == 0 || (soma / amostras) < 18
    }
}
