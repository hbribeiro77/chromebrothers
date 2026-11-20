package com.chromebrothers.navegador

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException

data class ImagemGravadaNaGaleriaDoNavegador(
    val uri: Uri,
    val nome: String,
)

object GravadorDeQuadroDeVideoNaGaleriaEAreaDeTransferencia {
    fun salvarNaGaleria(
        context: Context,
        png: ByteArray,
        mime: String = "image/png",
        prefixoDoNome: String = "chromebrothers-quadro",
    ): ImagemGravadaNaGaleriaDoNavegador {
        val extensao = when (mime) {
            "image/jpeg" -> "jpg"
            "image/webp" -> "webp"
            "image/gif" -> "gif"
            else -> "png"
        }
        val nome = "$prefixoDoNome-${System.currentTimeMillis()}.$extensao"
        val resolver = context.contentResolver
        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nome)
            put(MediaStore.Images.Media.MIME_TYPE, mime)
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}/ChromeBrothers",
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)
            ?: throw IOException("A galeria não aceitou o quadro.")
        resolver.openOutputStream(uri)?.use { saida -> saida.write(png) }
            ?: throw IOException("Não foi possível gravar o quadro.")
        valores.clear()
        valores.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, valores, null, null)
        return ImagemGravadaNaGaleriaDoNavegador(uri, nome)
    }

    fun salvarECopiar(context: Context, png: ByteArray): ImagemGravadaNaGaleriaDoNavegador {
        val publicada = salvarNaGaleria(context, png)
        copiarUri(context, publicada.uri, publicada.nome)
        return publicada
    }

    fun copiarUri(context: Context, uri: Uri, nome: String) {
        val mime = context.contentResolver.getType(uri)
            ?.takeIf { it.startsWith("image/") }
            ?: "image/png"
        publicarClipDeImagemParaGecko(context, uri, nome, mime)
    }

    /**
     * Copia bytes de imagem para o clipboard no formato que o **GeckoView** consegue colar
     * em contenteditable (ChatGPT etc.).
     *
     * Diferença vs Chrome: o Gecko (`Clipboard.getRawData`) só lê blob se
     * `ClipDescription.hasMimeType("image/png"|"image/jpeg"|"image/gif")` for true e o
     * item[0] tiver URI abrível. `ClipData.newUri(...)` às vezes declara só
     * `text/uri-list` — aí o Chrome ainda cola, o Gecko não.
     */
    fun copiarParaAreaDeTransferencia(context: Context, bytes: ByteArray, mime: String = "image/png") {
        val (bytesFinais, mimeFinal) = normalizarParaClipboardDoGecko(bytes, mime)
        val extensao = when (mimeFinal) {
            "image/jpeg" -> "jpg"
            "image/gif" -> "gif"
            else -> "png"
        }
        val nome = "chromebrothers-clipboard-${System.currentTimeMillis()}.$extensao"
        val resolver = context.contentResolver
        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nome)
            put(MediaStore.Images.Media.MIME_TYPE, mimeFinal)
            put(MediaStore.Images.Media.RELATIVE_PATH, "$PASTA_OCULTA_DA_COPIA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores)
            ?: throw IOException("Não foi possível copiar a imagem.")
        resolver.openOutputStream(uri)?.use { saida -> saida.write(bytesFinais) }
            ?: throw IOException("Não foi possível copiar a imagem.")
        // Sem IS_PENDING=0, outros apps não leem a URI do clipboard (Android 10+).
        valores.clear()
        valores.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, valores, null, null)
        publicarClipDeImagemParaGecko(context, uri, nome, mimeFinal)
        apagarCopiasAnteriores(context, uri)
    }

    /**
     * Monta ClipData com MIME de imagem **explícito** (não confiar no autodetection do
     * [ClipData.newUri]) — requisito do GeckoView para paste em contenteditable.
     */
    private fun publicarClipDeImagemParaGecko(
        context: Context,
        uri: Uri,
        nome: String,
        mime: String,
    ) {
        val mimeImagem = mime.takeIf { it.startsWith("image/") } ?: "image/png"
        // Só o MIME real dos bytes: se listarmos image/png com JPEG por baixo, o Gecko decodifica errado.
        val descricao = ClipDescription(nome, arrayOf(mimeImagem))
        val clip = ClipData(descricao, ClipData.Item(uri))
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(clip)
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            val tipos = (0 until (clip.description?.mimeTypeCount ?: 0)).joinToString { i ->
                clip.description.getMimeType(i)
            }
            Log.d(TAG, "clipboard imagem mime=[$tipos] uri=$uri")
        }
    }

    /** Gecko anuncia image/gif|jpeg|png no IME; preferimos PNG para o caminho de paste. */
    private fun normalizarParaClipboardDoGecko(bytes: ByteArray, mime: String): Pair<ByteArray, String> {
        if (mime.equals("image/gif", ignoreCase = true)) return bytes to "image/gif"
        if (mime.equals("image/png", ignoreCase = true)) return bytes to "image/png"
        val png = reencodeComoPng(bytes)
        return if (png != null) png to "image/png" else bytes to mime.ifBlank { "image/jpeg" }
    }

    private fun reencodeComoPng(bytes: ByteArray): ByteArray? {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        return try {
            val saida = ByteArrayOutputStream()
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, saida)) null else saida.toByteArray()
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }

    fun renomearNaGaleria(context: Context, uri: Uri, nomeAtual: String, nomeDigitado: String): String {
        val extensao = nomeAtual.substringAfterLast('.', "jpg").lowercase()
        val base = nomeDigitado
            .substringBeforeLast('.')
            .replace(Regex("""[\\/:*?"<>|]"""), "")
            .trim()
            .take(80)
        if (base.isEmpty()) throw IOException("A imagem precisa de um nome.")
        val nome = "$base.$extensao"
        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nome)
        }
        val alteradas = context.contentResolver.update(uri, valores, null, null)
        if (alteradas == 0) throw IOException("A galeria não aceitou o novo nome.")
        return nome
    }

    private fun apagarCopiasAnteriores(context: Context, manter: Uri) {
        val manterId = manter.lastPathSegment
        val resolver = context.contentResolver
        val projecao = arrayOf(MediaStore.Images.Media._ID)
        val selecao = "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ?"
        val argumentos = arrayOf("chromebrothers-clipboard-%")
        val cursor = if (Build.VERSION.SDK_INT >= 30) {
            val consulta = Bundle().apply {
                putInt(MediaStore.QUERY_ARG_MATCH_PENDING, MediaStore.MATCH_INCLUDE)
                putString(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION, selecao)
                putStringArray(android.content.ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, argumentos)
            }
            resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projecao, consulta, null)
        } else {
            @Suppress("DEPRECATION")
            resolver.query(
                MediaStore.setIncludePending(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                projecao,
                selecao,
                argumentos,
                null,
            )
        }
        cursor?.use { linhas ->
            val coluna = linhas.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (linhas.moveToNext()) {
                val id = linhas.getLong(coluna)
                if (id.toString() == manterId) continue
                resolver.delete(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), null, null)
            }
        }
    }

    private const val PASTA_OCULTA_DA_COPIA = "Pictures/ChromeBrothers/area-de-transferencia"
    private const val TAG = "CbClipboardImagem"
}
