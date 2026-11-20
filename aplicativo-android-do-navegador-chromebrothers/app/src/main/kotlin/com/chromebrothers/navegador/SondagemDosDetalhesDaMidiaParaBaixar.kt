package com.chromebrothers.navegador

import android.media.MediaMetadataRetriever
import com.chromebrothers.navegador.regras.DetalhesDaMidiaParaBaixar
import com.chromebrothers.navegador.regras.EscolhaDoPedidoDeMidiaParaBaixar
import com.chromebrothers.navegador.regras.LeituraDePlaylistHlsParaDownload
import com.chromebrothers.navegador.regras.LeituraDosDetalhesDoManifestoDeMidia
import com.chromebrothers.navegador.regras.PedidoDeMidiaObservado
import com.chromebrothers.navegador.regras.TipoDePedidoDeMidiaParaDownload
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Resolução, duração e se tem som de cada opção, sem baixar o vídeo: MP4 pelo cabeçalho (o Android lê
 * só o começo do arquivo remoto); HLS/DASH pelo manifesto. Chamado fora da thread principal.
 */
class SondagemDosDetalhesDaMidiaParaBaixar(private val transporte: TransporteDoDownloadDeMidia = TransportePorConexaoComum()) {
    private val jaLidos = ConcurrentHashMap<String, DetalhesDaMidiaParaBaixar>()

    fun sondar(
        pedidos: List<PedidoDeMidiaObservado>,
        referer: (String) -> String,
        agente: (String) -> String,
        tempoMaximoMs: Long = 9_000,
    ): Map<String, DetalhesDaMidiaParaBaixar> {
        val faltam = pedidos.filter { !jaLidos.containsKey(it.endereco) &&!EscolhaDoPedidoDeMidiaParaBaixar.pareceAudio(it) }.take(12)
        if (faltam.isNotEmpty()) {
            val pool = Executors.newFixedThreadPool(4)
            try {
                val futuros = faltam.map { pedido ->
                    pedido.endereco to pool.submit(Callable { sondarUm(pedido, referer(pedido.endereco), agente(pedido.endereco)) })
                }
                val limite = System.currentTimeMillis() + tempoMaximoMs
                futuros.forEach { (endereco, futuro) ->
                    val restante = (limite - System.currentTimeMillis()).coerceAtLeast(1)
                    runCatching { futuro.get(restante, TimeUnit.MILLISECONDS) }.getOrNull()?.let { jaLidos[endereco] = it }
                }
            } finally {
                pool.shutdownNow()
            }
        }
        return pedidos.mapNotNull { p -> jaLidos[p.endereco]?.let { p.endereco to it } }.toMap()
    }

    private fun sondarUm(pedido: PedidoDeMidiaObservado, referer: String, agente: String): DetalhesDaMidiaParaBaixar? =
        when (pedido.tipo) {
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS -> sondarHls(pedido.endereco, referer, agente)
            TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH ->
                texto(pedido.endereco, referer, agente)?.let { LeituraDosDetalhesDoManifestoDeMidia.dash(it) }
            else -> sondarArquivo(pedido.endereco, referer, agente)
        }

    private fun sondarArquivo(endereco: String, referer: String, agente: String): DetalhesDaMidiaParaBaixar? {
        val leitor = MediaMetadataRetriever()
        return try {
            val cabecalhos = buildMap {
                if (agente.isNotBlank()) put("User-Agent", agente)
                if (referer.isNotBlank()) put("Referer", referer)
            }
            leitor.setDataSource(endereco, cabecalhos)
            fun numero(chave: Int) = leitor.extractMetadata(chave)?.toLongOrNull() ?: 0L
            var largura = numero(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
            var altura = numero(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT).toInt()
            val rotacao = numero(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION).toInt()
            if (rotacao == 90 || rotacao == 270) largura = altura.also { altura = largura }
            DetalhesDaMidiaParaBaixar(
                largura = largura,
                altura = altura,
                duracaoMs = numero(MediaMetadataRetriever.METADATA_KEY_DURATION),
                temAudio = leitor.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)?.let { it == "yes" },
            )
        } catch (_: Exception) {
            null
        } finally {
            runCatching { leitor.release() }
        }
    }

    /** Mestre: resolução da melhor variante + duração lida na playlist dela. */
    private fun sondarHls(endereco: String, referer: String, agente: String): DetalhesDaMidiaParaBaixar? {
        val mestre = texto(endereco, referer, agente) ?: return null
        val detalhes = LeituraDosDetalhesDoManifestoDeMidia.hls(mestre)
        if (detalhes.duracaoMs > 0) return detalhes
        val melhor = LeituraDePlaylistHlsParaDownload.melhorVariante(LeituraDePlaylistHlsParaDownload.ler(mestre, endereco))
            ?: return detalhes
        val variante = texto(melhor.endereco, referer, agente) ?: return detalhes
        return detalhes.copy(duracaoMs = LeituraDosDetalhesDoManifestoDeMidia.hls(variante).duracaoMs)
    }

    private fun texto(endereco: String, referer: String, agente: String): String? = runCatching {
        transporte.abrir(endereco, null, referer, agente).use { resposta ->
            if (resposta.codigo !in 200..299) return@use null
            resposta.corpo.bufferedReader().readText().take(2_000_000)
        }
    }.getOrNull()
}
