package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

class GravacaoDoArquivoPedidoPelaPagina(private val contexto: Context) {
    fun baixarDaRede(
        endereco: String,
        nome: String,
        mime: String,
        referer: String,
        agente: String,
        cookie: String?,
    ): Boolean {
        val temporario = File(contexto.cacheDir, "download-pedido-pela-pagina-${System.nanoTime()}")
        return try {
            val conexao = URI(endereco).toURL().openConnection() as HttpURLConnection
            conexao.instanceFollowRedirects = true
            conexao.connectTimeout = 20_000
            conexao.readTimeout = 30_000
            conexao.setRequestProperty("User-Agent", agente.ifBlank { "Mozilla/5.0" })
            conexao.setRequestProperty("Accept", "*/*")
            if (referer.isNotBlank()) conexao.setRequestProperty("Referer", referer)
            if (!cookie.isNullOrBlank()) conexao.setRequestProperty("Cookie", cookie)
            conexao.connect()
            if (conexao.responseCode !in 200..299) return false
            conexao.inputStream.use { entrada ->
                temporario.outputStream().use { saida -> entrada.copyTo(saida) }
            }
            if (temporario.length() == 0L) return false
            publicar(temporario, nome, mime.ifBlank { "application/octet-stream" })
            true
        } catch (_: Exception) {
            false
        } finally {
            temporario.delete()
        }
    }

    fun gravarBytes(nome: String, mime: String, bytes: ByteArray): Boolean {
        if (bytes.isEmpty() || bytes.size > LIMITE_NA_MEMORIA) return false
        val temporario = File(contexto.cacheDir, "download-pedido-pela-pagina-${System.nanoTime()}")
        return try {
            temporario.writeBytes(bytes)
            publicar(temporario, nome, mime.ifBlank { "application/octet-stream" })
            true
        } catch (_: Exception) {
            false
        } finally {
            temporario.delete()
        }
    }

    private fun publicar(arquivo: File, nome: String, mime: String) {
        val valores = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nome)
            put(MediaStore.Downloads.MIME_TYPE, mime)
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ChromeBrothers")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = contexto.contentResolver.insert(colecao, valores) ?: error("sem destino")
        try {
            contexto.contentResolver.openOutputStream(uri)?.use { saida ->
                arquivo.inputStream().use { it.copyTo(saida) }
            } ?: error("sem saida")
            valores.clear()
            valores.put(MediaStore.Downloads.IS_PENDING, 0)
            contexto.contentResolver.update(uri, valores, null, null)
        } catch (erro: Exception) {
            contexto.contentResolver.delete(uri, null, null)
            throw erro
        }
    }

    private companion object {
        const val LIMITE_NA_MEMORIA = 40 * 1024 * 1024
    }
}
