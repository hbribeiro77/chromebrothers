package com.chromebrothers.navegador

import android.content.Context
import android.net.Uri
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class LeituraDeBytesDeMidiaLocalOuRemotaParaOComponenteDaColecao(
    private val contexto: Context,
    private val clienteHttp: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build(),
) {
    fun ler(endereco: String): ByteArray? {
        val bruto = endereco.trim()
        if (bruto.isEmpty()) return null
        val uri = runCatching { Uri.parse(bruto) }.getOrNull() ?: return null
        return when (uri.scheme?.lowercase()) {
            "content" -> lerConteudo(uri)
            "file" -> lerArquivo(uri)
            "http", "https" -> lerRemoto(bruto)
            else -> null
        }
    }

    fun copiarParaArquivoLocal(endereco: String, pasta: File, nomeBase: String): File? {
        val bytes = ler(endereco) ?: return null
        pasta.mkdirs()
        val extensao = extensaoProvavel(endereco, bytes)
        val arquivo = File(pasta, "$nomeBase.$extensao")
        return try {
            arquivo.outputStream().use { it.write(bytes) }
            if (arquivo.length() <= 0L) {
                arquivo.delete()
                null
            } else {
                arquivo
            }
        } catch (_: Exception) {
            arquivo.delete()
            null
        }
    }

    private fun lerConteudo(uri: Uri): ByteArray? =
        try {
            contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (_: Exception) {
            null
        }

    private fun lerArquivo(uri: Uri): ByteArray? {
        val caminho = uri.path ?: return null
        val arquivo = File(caminho)
        if (!arquivo.isFile) return null
        return try {
            arquivo.readBytes()
        } catch (_: Exception) {
            null
        }
    }

    private fun lerRemoto(endereco: String): ByteArray? =
        try {
            val pedido = Request.Builder().url(endereco).get().build()
            clienteHttp.newCall(pedido).execute().use { resposta ->
                if (!resposta.isSuccessful) return null
                resposta.body?.bytes()
            }
        } catch (_: Exception) {
            null
        }

    private fun extensaoProvavel(endereco: String, bytes: ByteArray): String {
        val mime = runCatching {
            val uri = Uri.parse(endereco)
            if (uri.scheme.equals("content", ignoreCase = true)) {
                contexto.contentResolver.getType(uri)
            } else {
                null
            }
        }.getOrNull().orEmpty().lowercase()
        when {
            mime.startsWith("image/png") || (bytes.size >= 8 && bytes[0] == 0x89.toByte()) -> return "png"
            mime.startsWith("image/webp") -> return "webp"
            mime.startsWith("image/gif") -> return "gif"
            mime.startsWith("image/") -> return "jpg"
            mime.startsWith("video/mp4") -> return "mp4"
            mime.startsWith("video/webm") -> return "webm"
            mime.startsWith("video/") -> return "mp4"
        }
        val caminho = endereco.substringBefore('?').substringAfterLast('/').lowercase()
        val ponto = caminho.substringAfterLast('.', "")
        if (ponto.length in 2..5 && ponto.all { it.isLetterOrDigit() }) return ponto
        return "bin"
    }
}
