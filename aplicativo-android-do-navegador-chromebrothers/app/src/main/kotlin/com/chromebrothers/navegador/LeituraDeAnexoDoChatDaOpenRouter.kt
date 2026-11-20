package com.chromebrothers.navegador

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream

data class AnexoDoChatDaOpenRouter(
    val nome: String,
    val mime: String,
    val tipo: String,
    val dados: String = "",
    val texto: String = "",
)

class LeituraDeAnexoDoChatDaOpenRouter(private val contexto: Context) {
    fun ler(uri: Uri, aoReceber: (AnexoDoChatDaOpenRouter?, String?) -> Unit) {
        Thread {
            val resultado = runCatching { montar(uri) }.getOrElse { erro ->
                null to (erro.message ?: "Não consegui ler o anexo.")
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                aoReceber(resultado.first, resultado.second)
            }
        }.start()
    }

    private fun montar(uri: Uri): Pair<AnexoDoChatDaOpenRouter?, String?> {
        val nome = nomeDe(uri)
        val mime = contexto.contentResolver.getType(uri).orEmpty()
        val extensao = nome.substringAfterLast('.', "").lowercase()
        val bytes = lerBytes(uri) ?: return null to "O anexo passou de 8 MB."
        return when {
            mime.startsWith("image/") || extensao in EXTENSOES_DE_IMAGEM -> imagem(bytes, nome)
            mime == "application/pdf" || extensao == "pdf" -> {
                val dados = Base64.encodeToString(bytes, Base64.NO_WRAP)
                AnexoDoChatDaOpenRouter(nome.ifBlank { "arquivo.pdf" }, "application/pdf", "arquivo", dados) to null
            }
            mime.startsWith("text/") || extensao in EXTENSOES_DE_TEXTO -> {
                val texto = bytes.toString(Charsets.UTF_8).take(LIMITE_DE_TEXTO)
                AnexoDoChatDaOpenRouter(nome.ifBlank { "arquivo.txt" }, "text/plain", "texto", texto = texto) to null
            }
            else -> null to "Use uma imagem, um PDF ou um texto."
        }
    }

    private fun imagem(bytes: ByteArray, nome: String): Pair<AnexoDoChatDaOpenRouter?, String?> {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, limites)
        if (limites.outWidth <= 0 || limites.outHeight <= 0) return null to "Não consegui ler a imagem."
        val amostra = amostra(limites.outWidth, limites.outHeight)
        val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opcoes)
            ?: return null to "Não consegui ler a imagem."
        val lado = maxOf(bitmap.width, bitmap.height).coerceAtLeast(1)
        val escala = if (lado > LADO_MAXIMO) LADO_MAXIMO.toFloat() / lado else 1f
        val reduzido = if (escala < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * escala).toInt().coerceAtLeast(1),
                (bitmap.height * escala).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            bitmap
        }
        val saida = ByteArrayOutputStream()
        reduzido.compress(Bitmap.CompressFormat.JPEG, 80, saida)
        if (reduzido !== bitmap) bitmap.recycle()
        reduzido.recycle()
        val dados = Base64.encodeToString(saida.toByteArray(), Base64.NO_WRAP)
        val nomeJpeg = if (nome.endsWith(".jpg", true) || nome.endsWith(".jpeg", true)) nome else "imagem.jpg"
        return AnexoDoChatDaOpenRouter(nomeJpeg, "image/jpeg", "imagem", dados) to null
    }

    private fun lerBytes(uri: Uri): ByteArray? {
        val entrada = contexto.contentResolver.openInputStream(uri) ?: return ByteArray(0)
        return entrada.use { fluxo ->
            val saida = ByteArrayOutputStream()
            val bloco = ByteArray(8192)
            var total = 0
            while (true) {
                val lido = fluxo.read(bloco)
                if (lido < 0) break
                total += lido
                if (total > LIMITE_DE_BYTES) return null
                saida.write(bloco, 0, lido)
            }
            saida.toByteArray()
        }
    }

    private fun nomeDe(uri: Uri): String {
        val cursor = contexto.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        cursor?.use {
            if (it.moveToFirst()) return it.getString(0).orEmpty()
        }
        return uri.lastPathSegment.orEmpty()
    }

    private fun amostra(largura: Int, altura: Int): Int {
        var valor = 1
        while (largura / valor > LADO_MAXIMO * 2 || altura / valor > LADO_MAXIMO * 2) valor *= 2
        return valor
    }

    private companion object {
        const val LIMITE_DE_BYTES = 8 * 1024 * 1024
        const val LIMITE_DE_TEXTO = 100_000
        const val LADO_MAXIMO = 1600
        val EXTENSOES_DE_IMAGEM = setOf("jpg", "jpeg", "png", "webp", "gif")
        val EXTENSOES_DE_TEXTO = setOf("txt", "md", "markdown")
    }
}
