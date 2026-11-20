package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria
import java.io.File
import java.net.URI

/**
 * Copia a mídia gerada (armazenamento interno) para Movies/ChromeBrothers ou Pictures/ChromeBrothers,
 * confere o tamanho da cópia e só então apaga o original pela rotina das bibliotecas.
 * Chamado fora da thread principal.
 */
class MovimentoDaMidiaGeradaParaAGaleriaDoAparelho(private val context: Context) {
    /** [novos] = uri original → `content://` da cópia na Galeria (para os cards acompanharem). */
    data class Resultado(val movidos: List<String>, val falhas: List<String>, val novos: Map<String, String> = emptyMap())

    /** [apagarOriginais] recebe original → novo antes de apagar (o original ainda existe nessa hora). */
    fun mover(uris: List<String>, apagarOriginais: (Map<String, String>) -> Unit): Resultado {
        val novos = linkedMapOf<String, String>()
        val falhas = mutableListOf<String>()
        uris.forEach { uri ->
            val destino = copiarParaAGaleriaDevolvendoUri(uri)
            if (destino != null) novos[uri] = destino else falhas += uri
        }
        if (novos.isNotEmpty()) apagarOriginais(novos)
        return Resultado(novos.keys.toList(), falhas, novos)
    }

    /** Só a cópia (o original fica): usado pelo player, que ainda está tocando o arquivo. */
    fun copiarParaAGaleria(uriTexto: String): Boolean = copiarParaAGaleriaDevolvendoUri(uriTexto) != null

    /** Copia para a Galeria e devolve o `content://` da cópia, ou null se falhou. */
    fun copiarParaAGaleriaDevolvendoUri(uriTexto: String): String? {
        val regra = MovimentoDaMidiaGeradaParaAGaleria
        if (!regra.podeMover(uriTexto)) return null
        val arquivo = runCatching { File(URI(uriTexto.substringBefore('?').trim())) }.getOrNull() ?: return null
        if (!arquivo.isFile) return null
        val nome = arquivo.name
        val video = regra.ehVideo(nome)
        val colecao = if (video) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        }
        val resolvedor = context.contentResolver
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nome)
            put(MediaStore.MediaColumns.MIME_TYPE, regra.mime(nome))
            put(MediaStore.MediaColumns.RELATIVE_PATH, regra.pastaDeDestino(nome))
            // Mantém a data da geração (a grade do Visualizador ordena por ela).
            put(MediaStore.MediaColumns.DATE_TAKEN, arquivo.lastModified())
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val destino: Uri = runCatching { resolvedor.insert(colecao, valores) }.getOrNull() ?: return null
        val copiou = runCatching {
            resolvedor.openOutputStream(destino)?.use { saida ->
                arquivo.inputStream().use { entrada -> entrada.copyTo(saida, 256 * 1024) }
            } ?: error("sem saída")
            val tamanhoDaCopia = resolvedor.openFileDescriptor(destino, "r")?.use { it.statSize } ?: -1L
            regra.copiaConfere(arquivo.length(), tamanhoDaCopia)
        }.getOrDefault(false)
        if (!copiou) {
            runCatching { resolvedor.delete(destino, null, null) }
            return null
        }
        valores.clear()
        valores.put(MediaStore.MediaColumns.IS_PENDING, 0)
        return if (runCatching { resolvedor.update(destino, valores, null, null) }.isSuccess) destino.toString() else null
    }
}
