package com.chromebrothers.navegador

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.muxer.Mp4Muxer
import androidx.media3.muxer.Muxer
import com.chromebrothers.navegador.regras.PreparacaoDaAmostraDeVideoAvcParaOMuxer
import com.chromebrothers.navegador.regras.TempoDeExibicaoDaAmostraParaOArquivo
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

@androidx.annotation.OptIn(UnstableApi::class)
object MuxDeTrilhaDeVideoETrilhaDeAudioEmUmArquivoMp4 {
    fun temAudio(arquivo: File): Boolean {
        val extrator = MediaExtractor()
        return try {
            extrator.setDataSource(arquivo.absolutePath)
            (0 until extrator.trackCount).any { indice ->
                extrator.getTrackFormat(indice).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            }
        } catch (_: Exception) {
            false
        } finally {
            extrator.release()
        }
    }

    fun juntar(video: File, audio: File, destino: File) {
        val extratorDeVideo = MediaExtractor()
        val extratorDeAudio = MediaExtractor()
        try {
            extratorDeVideo.setDataSource(video.absolutePath)
            extratorDeAudio.setDataSource(audio.absolutePath)
            val indiceDeVideo = indiceDaFaixa(extratorDeVideo, "video/")
                ?: throw FalhaNoDownloadDeMidia("Não achei a imagem desse vídeo.")
            val indiceDeAudio = indiceDaFaixa(extratorDeAudio, "audio/")
                ?: throw FalhaNoDownloadDeMidia("Não achei o som desse vídeo.")
            val formatoDeVideo = extratorDeVideo.getTrackFormat(indiceDeVideo)
            val formatoDeAudio = extratorDeAudio.getTrackFormat(indiceDeAudio)
            extratorDeVideo.selectTrack(indiceDeVideo)
            extratorDeAudio.selectTrack(indiceDeAudio)
            FileOutputStream(destino).use { saida ->
                val gravador = Mp4Muxer.Builder(saida)
                    .setSampleBatchingEnabled(false)
                    .setAttemptStreamableOutputEnabled(false)
                    .build()
                try {
                    val faixaDeVideo = gravador.addTrack(formatoParaOMuxer(formatoDeVideo))
                    val faixaDeAudio = gravador.addTrack(formatoParaOMuxer(formatoDeAudio))
                    copiarAmostras(extratorDeVideo, gravador, faixaDeVideo, formatoDeVideo)
                    copiarAmostras(extratorDeAudio, gravador, faixaDeAudio, formatoDeAudio)
                } finally {
                    gravador.close()
                }
            }
        } catch (erro: FalhaNoDownloadDeMidia) {
            throw erro
        } catch (_: Exception) {
            throw FalhaNoDownloadDeMidia("Não consegui juntar a imagem e o som desse vídeo.")
        } finally {
            extratorDeVideo.release()
            extratorDeAudio.release()
        }
    }

    private fun indiceDaFaixa(extrator: MediaExtractor, prefixo: String): Int? {
        return (0 until extrator.trackCount).firstOrNull { indice ->
            extrator.getTrackFormat(indice).getString(MediaFormat.KEY_MIME)?.startsWith(prefixo) == true
        }
    }

    private fun formatoParaOMuxer(formato: MediaFormat): Format {
        val mime = formato.getString(MediaFormat.KEY_MIME)
            ?: throw FalhaNoDownloadDeMidia("Não consegui juntar a imagem e o som desse vídeo.")
        val construtor = Format.Builder().setSampleMimeType(mime)
        inteiro(formato, MediaFormat.KEY_WIDTH)?.let { construtor.setWidth(it) }
        inteiro(formato, MediaFormat.KEY_HEIGHT)?.let { construtor.setHeight(it) }
        inteiro(formato, MediaFormat.KEY_SAMPLE_RATE)?.let { construtor.setSampleRate(it) }
        inteiro(formato, MediaFormat.KEY_CHANNEL_COUNT)?.let { construtor.setChannelCount(it) }
        inteiro(formato, MediaFormat.KEY_BIT_RATE)?.let { construtor.setAverageBitrate(it) }
        inteiro(formato, MediaFormat.KEY_ROTATION)?.let { construtor.setRotationDegrees(it) }
        val primeiro = bytes(formato, "csd-0")
        val segundo = bytes(formato, "csd-1")
        if (mime == MediaFormat.MIMETYPE_VIDEO_AVC) {
            val inicial = primeiro?.let { PreparacaoDaAmostraDeVideoAvcParaOMuxer.inicializacao(it, segundo) }
                ?: throw FalhaNoDownloadDeMidia("Não consegui juntar a imagem e o som desse vídeo.")
            construtor.setInitializationData(inicial)
        } else {
            val dados = listOfNotNull(primeiro, segundo)
            if (dados.isNotEmpty()) construtor.setInitializationData(dados)
        }
        return construtor.build()
    }

    private fun copiarAmostras(
        extrator: MediaExtractor,
        muxer: Mp4Muxer,
        faixa: Muxer.TrackToken,
        formato: MediaFormat,
    ) {
        val mime = formato.getString(MediaFormat.KEY_MIME).orEmpty()
        val videoAvc = mime == MediaFormat.MIMETYPE_VIDEO_AVC
        val tamanhoDoComprimento = if (videoAvc) {
            PreparacaoDaAmostraDeVideoAvcParaOMuxer.tamanhoDoComprimento(bytes(formato, "csd-0") ?: ByteArray(0))
        } else {
            4
        }
        val capacidade = (inteiro(formato, MediaFormat.KEY_MAX_INPUT_SIZE) ?: (4 * 1024 * 1024))
            .coerceAtLeast(1024 * 1024)
        val buffer = ByteBuffer.allocate(capacidade)
        while (true) {
            buffer.clear()
            val lido = extrator.readSampleData(buffer, 0)
            if (lido < 0) break
            val bruto = ByteArray(lido)
            buffer.position(0)
            buffer.limit(lido)
            buffer.get(bruto)
            val pronto = if (videoAvc) {
                PreparacaoDaAmostraDeVideoAvcParaOMuxer.amostra(bruto, tamanhoDoComprimento)
            } else {
                bruto
            }
            val tempo = TempoDeExibicaoDaAmostraParaOArquivo.sequencia(listOf(extrator.sampleTime)).first()
            val flags = if (extrator.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC != 0) {
                MediaCodec.BUFFER_FLAG_KEY_FRAME
            } else {
                0
            }
            val info = MediaCodec.BufferInfo()
            info.set(0, pronto.size, tempo, flags)
            muxer.writeSampleData(faixa, ByteBuffer.wrap(pronto), info)
            extrator.advance()
        }
    }

    private fun inteiro(formato: MediaFormat, chave: String): Int? {
        if (!formato.containsKey(chave)) return null
        return try {
            formato.getInteger(chave)
        } catch (_: Exception) {
            null
        }
    }

    private fun bytes(formato: MediaFormat, chave: String): ByteArray? {
        val buffer = formato.getByteBuffer(chave) ?: return null
        val copia = buffer.duplicate()
        copia.rewind()
        if (!copia.hasRemaining()) return null
        val dados = ByteArray(copia.remaining())
        copia.get(dados)
        return dados
    }
}
