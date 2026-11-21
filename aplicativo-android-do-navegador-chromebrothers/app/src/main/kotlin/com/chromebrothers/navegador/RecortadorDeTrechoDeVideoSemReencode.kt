package com.chromebrothers.navegador

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.muxer.BufferInfo
import androidx.media3.muxer.Mp4Muxer
import androidx.media3.transformer.Composition
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.chromebrothers.navegador.regras.MovimentoDaMidiaGeradaParaAGaleria
import com.chromebrothers.navegador.regras.PreparacaoDaAmostraDeVideoAvcParaOMuxer
import com.chromebrothers.navegador.regras.RecorteDeTrechoDeVideo
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * Recorta [inicioMs, fimMs] de um vídeo do aparelho e salva em Movies/ChromeBrothers.
 * - Início exato sem encode: media3 Transformer com "edit list" do MP4 (copia desde o quadro-chave e o player
 *   começa no ponto escolhido). Só vale se nada passou por encoder.
 * - Se algo seria reencodado (ou der erro), o trecho é copiado sem encode nenhum, com o início recuado ao
 *   quadro-chave anterior.
 * Chamar [recortar] na thread principal; os retornos também chegam nela.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class RecortadorDeTrechoDeVideoSemReencode(private val context: Context) {
    data class Resultado(
        val uriNaGaleria: String,
        val inicioEfetivoMs: Long,
        /** true: o início recuou ao quadro-chave (cópia pura). */
        val inicioAjustado: Boolean,
    )

    private val principal = Handler(Looper.getMainLooper())
    private var transformerAtual: Transformer? = null
    @Volatile private var cancelado = false

    /** Tempos (ms) dos quadros-chave do vídeo, em ordem. Bloqueante (lê o arquivo): fora da principal. */
    fun listarQuadrosChave(uri: String): List<Long> {
        val extrator = MediaExtractor()
        return try {
            extrator.setDataSource(context, uriDe(uri), null)
            val faixa = indiceDaFaixa(extrator, "video/") ?: return emptyList()
            extrator.selectTrack(faixa)
            val chaves = mutableListOf<Long>()
            while (true) {
                val tempo = extrator.sampleTime
                if (tempo < 0) break
                if (extrator.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) chaves += tempo / 1000
                if (!extrator.advance()) break
            }
            chaves.distinct().sorted()
        } catch (_: Exception) {
            emptyList()
        } finally {
            extrator.release()
        }
    }

    /** Nome de exibição do arquivo (para sugerir o nome do recorte). */
    fun nomeDoArquivo(uri: String): String {
        val u = uriDe(uri)
        if (u.scheme.equals("content", ignoreCase = true)) {
            runCatching {
                context.contentResolver.query(u, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                    if (c.moveToFirst()) return c.getString(0).orEmpty()
                }
            }
        }
        return Uri.decode(uri.substringBefore('?').substringAfterLast('/'))
    }

    fun tamanhoDoArquivo(uri: String): Long = runCatching {
        context.contentResolver.openFileDescriptor(uriDe(uri), "r")?.use { it.statSize } ?: -1L
    }.getOrDefault(-1L)

    fun cancelar() {
        cancelado = true
        transformerAtual?.cancel()
        transformerAtual = null
    }

    fun recortar(
        uri: String,
        inicioMs: Long,
        fimMs: Long,
        nomeDoArquivo: String,
        quadrosChave: List<Long>,
        aoProgresso: (Int) -> Unit,
        aoTerminar: (Result<Resultado>) -> Unit,
    ) {
        cancelado = false
        val temporario = File(context.cacheDir, "recortes").apply { mkdirs() }
            .let { File(it, "recorte-${System.currentTimeMillis()}.mp4") }
        // Início já num quadro-chave: a cópia pura é exata e instantânea.
        if (RecorteDeTrechoDeVideo.inicioJaEhQuadroChave(quadrosChave, inicioMs)) {
            copiarEPublicar(uri, inicioMs, fimMs, nomeDoArquivo, temporario, aoTerminar)
            return
        }
        val item = MediaItem.Builder()
            .setUri(uriDe(uri))
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(inicioMs)
                    .setEndPositionMs(fimMs)
                    .build(),
            )
            .build()
        val progresso = ProgressHolder()
        lateinit var acompanhar: Runnable
        // Início exato sem encode: copia a partir do quadro-chave e grava um "edit list" no MP4 que manda o
        // player começar no ponto escolhido (como o recorte do iPhone). O "corte inteligente" (reencodar só o
        // começo) foi testado e o encoder deste tablet não casa com H.264 High (FORMAT_MISMATCH): reencodava tudo.
        val transformer = Transformer.Builder(context)
            .experimentalSetMp4EditListTrimEnabled(true)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    principal.removeCallbacks(acompanhar)
                    transformerAtual = null
                    android.util.Log.i(
                        TAG,
                        "recorte: videoEncoder=${exportResult.videoEncoderName} " +
                            "audioEncoder=${exportResult.audioEncoderName} duracao=${exportResult.durationMs}",
                    )
                    if (cancelado) return
                    // Vale só se nada passou por encoder (senão seria reencode, que não queremos).
                    val semEncode = exportResult.videoEncoderName == null && exportResult.audioEncoderName == null
                    if (semEncode) {
                        publicarEmSegundoPlano(temporario, nomeDoArquivo, aoTerminar) {
                            Resultado(it, inicioMs, inicioAjustado = false)
                        }
                    } else {
                        // Houve encode: descarta e copia sem encode (início no quadro-chave).
                        temporario.delete()
                        copiarEPublicar(uri, inicioMs, fimMs, nomeDoArquivo, temporario, aoTerminar)
                    }
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    android.util.Log.w(TAG, "recorte por edit list falhou: ${exportException.errorCodeName}", exportException)
                    principal.removeCallbacks(acompanhar)
                    transformerAtual = null
                    temporario.delete()
                    if (cancelado) return
                    copiarEPublicar(uri, inicioMs, fimMs, nomeDoArquivo, temporario, aoTerminar)
                }
            })
            .build()
        transformerAtual = transformer
        acompanhar = Runnable {
            val atual = transformerAtual ?: return@Runnable
            if (atual.getProgress(progresso) == Transformer.PROGRESS_STATE_AVAILABLE) aoProgresso(progresso.progress)
            principal.postDelayed(acompanhar, 250L)
        }
        transformer.start(item, temporario.absolutePath)
        principal.postDelayed(acompanhar, 250L)
    }

    private fun copiarEPublicar(
        uri: String,
        inicioMs: Long,
        fimMs: Long,
        nome: String,
        temporario: File,
        aoTerminar: (Result<Resultado>) -> Unit,
    ) {
        fila.execute {
            val resultado = runCatching {
                val efetivo = copiarTrecho(uri, inicioMs, fimMs, temporario)
                if (cancelado) error("cancelado")
                val publicado = publicarNaGaleria(temporario, nome)
                Resultado(publicado, efetivo, inicioAjustado = efetivo + RecorteDeTrechoDeVideo.TOLERANCIA_DO_QUADRO_CHAVE_MS < inicioMs)
            }
            temporario.delete()
            principal.post { if (!cancelado) aoTerminar(resultado) }
        }
    }

    private fun publicarEmSegundoPlano(
        temporario: File,
        nome: String,
        aoTerminar: (Result<Resultado>) -> Unit,
        montar: (String) -> Resultado,
    ) {
        fila.execute {
            val resultado = runCatching { montar(publicarNaGaleria(temporario, nome)) }
            temporario.delete()
            principal.post { if (!cancelado) aoTerminar(resultado) }
        }
    }

    /**
     * Cópia pura (sem encode): do quadro-chave em/antes de [inicioMs] até [fimMs]; tempos recomeçam do zero.
     * Devolve o início efetivo (ms).
     */
    private fun copiarTrecho(uri: String, inicioMs: Long, fimMs: Long, destino: File): Long {
        val origem = uriDe(uri)
        val sonda = MediaExtractor()
        val faixas: List<Pair<Int, MediaFormat>>
        val indiceDoVideo: Int?
        try {
            sonda.setDataSource(context, origem, null)
            faixas = (0 until sonda.trackCount).map { it to sonda.getTrackFormat(it) }
                .filter { (_, f) ->
                    val mime = f.getString(MediaFormat.KEY_MIME).orEmpty()
                    mime.startsWith("video/") || mime.startsWith("audio/")
                }
            indiceDoVideo = faixas.firstOrNull { it.second.getString(MediaFormat.KEY_MIME).orEmpty().startsWith("video/") }?.first
        } finally {
            sonda.release()
        }
        if (indiceDoVideo == null) throw IllegalStateException("Esse arquivo não tem imagem de vídeo.")
        // Início efetivo: o quadro-chave em/antes do início pedido.
        val inicioUs = run {
            val e = MediaExtractor()
            try {
                e.setDataSource(context, origem, null)
                e.selectTrack(indiceDoVideo)
                e.seekTo(inicioMs * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
                e.sampleTime.coerceAtLeast(0L)
            } finally {
                e.release()
            }
        }
        val fimUs = fimMs * 1000
        FileOutputStream(destino).use { saida ->
            val muxer = Mp4Muxer.Builder(saida)
                .setSampleBatchingEnabled(false)
                .setAttemptStreamableOutputEnabled(false)
                .build()
            try {
                val destinos = faixas.associate { (indice, formato) ->
                    indice to muxer.addTrack(MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.formatoParaOMuxer(formato))
                }
                for ((indice, formato) in faixas) {
                    if (cancelado) break
                    copiarFaixa(origem, indice, formato, muxer, destinos.getValue(indice), inicioUs, fimUs)
                }
            } finally {
                muxer.close()
            }
        }
        return inicioUs / 1000
    }

    private fun copiarFaixa(
        origem: Uri,
        indice: Int,
        formato: MediaFormat,
        muxer: Mp4Muxer,
        faixaNoMuxer: Int,
        inicioUs: Long,
        fimUs: Long,
    ) {
        val mime = formato.getString(MediaFormat.KEY_MIME).orEmpty()
        val videoAvc = mime == MediaFormat.MIMETYPE_VIDEO_AVC
        val video = mime.startsWith("video/")
        val tamanhoDoComprimento = if (videoAvc) {
            PreparacaoDaAmostraDeVideoAvcParaOMuxer.tamanhoDoComprimento(
                MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.bytes(formato, "csd-0") ?: ByteArray(0),
            )
        } else {
            4
        }
        val capacidade = (MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4.inteiro(formato, MediaFormat.KEY_MAX_INPUT_SIZE)
            ?: (4 * 1024 * 1024)).coerceAtLeast(1024 * 1024)
        val buffer = ByteBuffer.allocate(capacidade)
        val extrator = MediaExtractor()
        try {
            extrator.setDataSource(context, origem, null)
            extrator.selectTrack(indice)
            extrator.seekTo(inicioUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)
            // B-frames chegam fora de ordem: segue um pouco depois do fim para não perder quadros ≤ fim.
            val paradaUs = fimUs + 1_000_000
            while (!cancelado) {
                val tempo = extrator.sampleTime
                if (tempo < 0 || tempo > paradaUs) break
                val dentro = tempo <= fimUs && (video || tempo >= inicioUs)
                if (dentro) {
                    buffer.clear()
                    val lido = extrator.readSampleData(buffer, 0)
                    if (lido < 0) break
                    val bruto = ByteArray(lido)
                    buffer.position(0)
                    buffer.limit(lido)
                    buffer.get(bruto)
                    val pronto = if (videoAvc) PreparacaoDaAmostraDeVideoAvcParaOMuxer.amostra(bruto, tamanhoDoComprimento) else bruto
                    val flags = if (extrator.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) MediaCodec.BUFFER_FLAG_KEY_FRAME else 0
                    val relativo = (tempo - inicioUs).coerceAtLeast(0L)
                    muxer.writeSampleData(faixaNoMuxer, ByteBuffer.wrap(pronto), BufferInfo(relativo, pronto.size, flags))
                }
                if (!extrator.advance()) break
            }
        } finally {
            extrator.release()
        }
    }

    /** Copia o arquivo pronto para Movies/ChromeBrothers (Galeria) e devolve o content://. */
    private fun publicarNaGaleria(arquivo: File, nome: String): String {
        if (!arquivo.isFile || arquivo.length() <= 0L) throw IllegalStateException("O recorte saiu vazio.")
        val resolvedor = context.contentResolver
        val valores = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, nome)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.MediaColumns.RELATIVE_PATH, MovimentoDaMidiaGeradaParaAGaleria.PASTA_DOS_VIDEOS)
            put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val colecao = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val destino = resolvedor.insert(colecao, valores) ?: throw IllegalStateException("Não consegui criar o arquivo na Galeria.")
        try {
            resolvedor.openOutputStream(destino)?.use { saida ->
                arquivo.inputStream().use { it.copyTo(saida, 256 * 1024) }
            } ?: throw IllegalStateException("Não consegui gravar na Galeria.")
            valores.clear()
            valores.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolvedor.update(destino, valores, null, null)
        } catch (e: Exception) {
            runCatching { resolvedor.delete(destino, null, null) }
            throw e
        }
        return destino.toString()
    }

    /** content:// e file: como vêm; caminho puro (/data/…) vira file://. */
    private fun uriDe(texto: String): Uri =
        if (texto.startsWith("/")) Uri.fromFile(File(texto)) else Uri.parse(texto)

    private fun indiceDaFaixa(extrator: MediaExtractor, prefixo: String): Int? =
        (0 until extrator.trackCount).firstOrNull { indice ->
            extrator.getTrackFormat(indice).getString(MediaFormat.KEY_MIME)?.startsWith(prefixo) == true
        }

    private companion object {
        const val TAG = "RecorteDeTrecho"
        val fila = Executors.newSingleThreadExecutor()
    }
}
