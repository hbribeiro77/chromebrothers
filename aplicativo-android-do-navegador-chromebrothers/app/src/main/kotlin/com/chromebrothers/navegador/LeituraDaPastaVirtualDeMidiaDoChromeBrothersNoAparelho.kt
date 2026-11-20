package com.chromebrothers.navegador

import android.content.ContentResolver
import android.content.ContentUris
import android.provider.MediaStore
import com.chromebrothers.navegador.regras.ArquivoDeMidiaSalvoNaGaveta
import com.chromebrothers.navegador.regras.ItemDaPastaEscolhidaPeloComponente
import com.chromebrothers.navegador.regras.LimiteDeItensExibidosNoVisualizadorDeMidiaNaColecao
import com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria
import com.chromebrothers.navegador.regras.PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao
import com.chromebrothers.navegador.regras.RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao

/**
 * Junta Downloads/ChromeBrothers, Pictures/ChromeBrothers (exceto área de transferência) e as mídias
 * geradas via MCP no formato JSON que o Visualizador já entende via escolherPasta/listarPasta.
 */
class LeituraDaPastaVirtualDeMidiaDoChromeBrothersNoAparelho(
    private val resolvedor: ContentResolver,
    private val listarGeradasViaMcp: () -> List<ArquivoDeMidiaSalvoNaGaveta> = { emptyList() },
) {
    fun descrever(): String =
        PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao.montarListagem(listarTodos())

    /** Os itens que a grade do Visualizador mostra (mais recentes, até o limite). */
    fun listarExibidos(): List<ItemDaPastaEscolhidaPeloComponente> =
        listarTodos().take(LimiteDeItensExibidosNoVisualizadorDeMidiaNaColecao.LIMITE)

    private fun listarTodos(): List<ItemDaPastaEscolhidaPeloComponente> {
        val baixados = LeituraDosArquivosDeMidiaBaixadosNaPastaDoChromeBrothers.listar(resolvedor)
        val imagens = listarImagensPublicadasNaPastaDoChromeBrothers()
        val videosMovidos = runCatching { listarVideosMovidosParaAGaleria() }.getOrDefault(emptyList())
        val geradas = runCatching { listarGeradasViaMcp() }.getOrDefault(emptyList())
        return (baixados + imagens + videosMovidos + geradas)
            .sortedByDescending { it.instante }
            .distinctBy { it.uri }
            .map(PastaVirtualDeMidiaDoChromeBrothersParaOComponenteDaColecao::itemDaPasta)
            .filter(RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao::ehMidia)
    }

    /** Vídeos gerados que o usuário moveu para Movies/ChromeBrothers (continuam no Visualizador). */
    private fun listarVideosMovidosParaAGaleria(): List<ArquivoDeMidiaSalvoNaGaveta> {
        val resultado = mutableListOf<ArquivoDeMidiaSalvoNaGaveta>()
        val colecao = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projecao = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.RELATIVE_PATH,
            MediaStore.Video.Media.IS_PENDING,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
        )
        resolvedor.query(colecao, projecao, null, null, null)?.use { cursor ->
            val colunaId = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val colunaNome = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val colunaData = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val colunaTirada = cursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN)
            val colunaCaminho = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
            val colunaPendente = cursor.getColumnIndex(MediaStore.Video.Media.IS_PENDING)
            val colunaTamanho = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
            val colunaMime = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
            while (cursor.moveToNext()) {
                if (colunaPendente >= 0 && cursor.getInt(colunaPendente) == 1) continue
                val caminho = if (colunaCaminho >= 0) cursor.getString(colunaCaminho).orEmpty() else ""
                if (!MovimentoDaMidiaGeradaParaAGaleria.ehDaPastaDosVideosMovidos(caminho)) continue
                val tirada = if (colunaTirada >= 0) cursor.getLong(colunaTirada) else 0L
                resultado += ArquivoDeMidiaSalvoNaGaveta(
                    uri = ContentUris.withAppendedId(colecao, cursor.getLong(colunaId)).toString(),
                    nomeDoArquivo = cursor.getString(colunaNome).orEmpty(),
                    // Data da geração (gravada ao mover); senão a da cópia.
                    instante = if (tirada > 0) tirada else cursor.getLong(colunaData) * 1000,
                    tamanho = if (colunaTamanho >= 0) cursor.getLong(colunaTamanho) else -1,
                    mime = if (colunaMime >= 0) cursor.getString(colunaMime).orEmpty() else "video/mp4",
                )
            }
        }
        return resultado
    }

    private fun listarImagensPublicadasNaPastaDoChromeBrothers(): List<ArquivoDeMidiaSalvoNaGaveta> {
        val resultado = mutableListOf<ArquivoDeMidiaSalvoNaGaveta>()
        val colecao = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projecao = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.RELATIVE_PATH,
            MediaStore.Images.Media.IS_PENDING,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
        )
        resolvedor.query(
            colecao,
            projecao,
            null,
            null,
            "${MediaStore.Images.Media.DATE_MODIFIED} DESC",
        )?.use { cursor ->
            val colunaId = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val colunaNome = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val colunaData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)
            val colunaTirada = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val colunaCaminho = cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
            val colunaPendente = cursor.getColumnIndex(MediaStore.Images.Media.IS_PENDING)
            val colunaTamanho = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)
            val colunaMime = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)
            while (cursor.moveToNext()) {
                if (colunaPendente >= 0 && cursor.getInt(colunaPendente) == 1) continue
                val caminho = if (colunaCaminho >= 0) {
                    cursor.getString(colunaCaminho).orEmpty().replace('\\', '/')
                } else {
                    ""
                }
                if (!caminho.contains("ChromeBrothers")) continue
                if (caminho.contains("area-de-transferencia")) continue
                val id = cursor.getLong(colunaId)
                resultado += ArquivoDeMidiaSalvoNaGaveta(
                    uri = ContentUris.withAppendedId(colecao, id).toString(),
                    nomeDoArquivo = cursor.getString(colunaNome).orEmpty(),
                    // Imagem movida para a Galeria guarda a data da geração em DATE_TAKEN.
                    instante = (if (colunaTirada >= 0) cursor.getLong(colunaTirada) else 0L)
                        .takeIf { it > 0 } ?: (cursor.getLong(colunaData) * 1000),
                    tamanho = if (colunaTamanho >= 0) cursor.getLong(colunaTamanho) else -1,
                    mime = if (colunaMime >= 0) cursor.getString(colunaMime).orEmpty() else "image/*",
                )
            }
        }
        return resultado
    }
}
