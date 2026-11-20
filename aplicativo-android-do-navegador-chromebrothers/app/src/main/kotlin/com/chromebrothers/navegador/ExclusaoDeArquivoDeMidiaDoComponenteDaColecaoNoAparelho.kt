package com.chromebrothers.navegador

import android.content.ContentResolver
import android.net.Uri
import com.chromebrothers.navegador.regras.ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador
import java.io.File
import java.net.URI

/**
 * Apaga mídia pedida pelo Visualizador (`content://` MediaStore ou `file://` local).
 */
object ExclusaoDeArquivoDeMidiaDoComponenteDaColecaoNoAparelho {
    fun excluirVarios(resolvedor: ContentResolver, urisJson: String?): String {
        val uris = ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.parseUris(urisJson)
        if (uris.isEmpty()) {
            return ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.mensagemSemUris()
        }
        val excluidos = mutableListOf<String>()
        val falhas = mutableListOf<String>()
        for (uri in uris) {
            if (excluirUma(resolvedor, uri)) excluidos += uri else falhas += uri
        }
        return ExclusaoDeArquivosDaColecaoFlutuanteDoVisualizador.mensagemResultado(excluidos, falhas)
    }

    fun excluirUma(resolvedor: ContentResolver, uriTexto: String): Boolean {
        val limpo = uriTexto.trim()
        if (limpo.isEmpty()) return false
        return try {
            when {
                limpo.startsWith("content:", ignoreCase = true) ->
                    resolvedor.delete(Uri.parse(limpo), null, null) > 0
                limpo.startsWith("file:", ignoreCase = true) -> {
                    val arquivo = File(URI(limpo))
                    arquivo.isFile && arquivo.delete()
                }
                else -> false
            }
        } catch (_: Exception) {
            false
        }
    }
}
