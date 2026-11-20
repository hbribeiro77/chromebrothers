package com.chromebrothers.navegador

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.ArquivoDeMidiaSalvoNaGaveta

object LeituraDosArquivosDeMidiaBaixadosNaPastaDoChromeBrothers {
    fun listar(resolvedor: ContentResolver): List<ArquivoDeMidiaSalvoNaGaveta> {
        val resultado = mutableListOf<ArquivoDeMidiaSalvoNaGaveta>()
        val colecao = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val projecao = arrayOf(
            MediaStore.Downloads._ID,
            MediaStore.Downloads.DISPLAY_NAME,
            MediaStore.Downloads.DATE_MODIFIED,
            MediaStore.Downloads.RELATIVE_PATH,
            MediaStore.Downloads.IS_PENDING,
            MediaStore.Downloads.SIZE,
            MediaStore.Downloads.MIME_TYPE,
        )
        resolvedor.query(
            colecao,
            projecao,
            null,
            null,
            "${MediaStore.Downloads.DATE_MODIFIED} DESC",
        )?.use { cursor ->
            val colunaId = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            val colunaNome = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DISPLAY_NAME)
            val colunaData = cursor.getColumnIndexOrThrow(MediaStore.Downloads.DATE_MODIFIED)
            val colunaCaminho = cursor.getColumnIndex(MediaStore.Downloads.RELATIVE_PATH)
            val colunaPendente = cursor.getColumnIndex(MediaStore.Downloads.IS_PENDING)
            val colunaTamanho = cursor.getColumnIndex(MediaStore.Downloads.SIZE)
            val colunaMime = cursor.getColumnIndex(MediaStore.Downloads.MIME_TYPE)
            while (cursor.moveToNext()) {
                if (colunaPendente >= 0 && cursor.getInt(colunaPendente) == 1) continue
                val caminho = if (colunaCaminho >= 0) cursor.getString(colunaCaminho).orEmpty() else ""
                if (!caminho.replace('\\', '/').contains("ChromeBrothers")) continue
                val id = cursor.getLong(colunaId)
                resultado += ArquivoDeMidiaSalvoNaGaveta(
                    uri = ContentUris.withAppendedId(colecao, id).toString(),
                    nomeDoArquivo = cursor.getString(colunaNome).orEmpty(),
                    instante = cursor.getLong(colunaData) * 1000,
                    tamanho = if (colunaTamanho >= 0) cursor.getLong(colunaTamanho) else -1,
                    mime = if (colunaMime >= 0) cursor.getString(colunaMime).orEmpty() else "",
                )
            }
        }
        return resultado
    }
}
