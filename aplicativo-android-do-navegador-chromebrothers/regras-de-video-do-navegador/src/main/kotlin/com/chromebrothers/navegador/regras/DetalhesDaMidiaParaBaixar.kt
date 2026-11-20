package com.chromebrothers.navegador.regras

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** O que dá para saber de uma opção de download antes de baixar (lido do começo do arquivo ou do manifesto). */
data class DetalhesDaMidiaParaBaixar(
    val largura: Int = 0,
    val altura: Int = 0,
    val duracaoMs: Long = 0,
    /** null = não deu para saber. */
    val temAudio: Boolean? = null,
    /** Playlist com várias qualidades: o app baixa a melhor. */
    val variasQualidades: Boolean = false,
    /** Taxa (bits/s) da qualidade que será baixada, quando o manifesto informa. */
    val bitsPorSegundo: Long = 0,
) {
    /** Tamanho aproximado do vídeo de uma playlist: taxa × duração. 0 se não dá para estimar. */
    val tamanhoEstimado: Long get() = if (bitsPorSegundo > 0 && duracaoMs > 0) bitsPorSegundo / 8 * duracaoMs / 1000 else 0

    /** Lado menor do quadro (vídeo em pé ou deitado): 1080 → "1080p". */
    val linhas: Int get() = if (largura > 0 && altura > 0) min(largura, altura) else max(largura, altura)
}

object LeituraDosDetalhesDoManifestoDeMidia {
    /** HLS mestre: maior RESOLUTION entre as variantes; HLS de trechos: soma das durações (EXTINF). */
    fun hls(texto: String): DetalhesDaMidiaParaBaixar {
        var largura = 0
        var altura = 0
        var variantes = 0
        var banda = 0L
        var segundos = 0.0
        texto.lineSequence().map { it.trim() }.forEach { linha ->
            when {
                linha.startsWith("#EXT-X-STREAM-INF", ignoreCase = true) -> {
                    variantes++
                    val bandaDaLinha = Regex("""(?i)(?:^|[:,])BANDWIDTH=(\d+)""").find(linha)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
                    if (bandaDaLinha > banda) banda = bandaDaLinha
                    Regex("""(?i)RESOLUTION=(\d+)x(\d+)""").find(linha)?.let {
                        val l = it.groupValues[1].toInt()
                        val a = it.groupValues[2].toInt()
                        if (min(l, a) > min(largura, altura) || largura == 0) {
                            largura = l
                            altura = a
                        }
                    }
                }
                linha.startsWith("#EXTINF:", ignoreCase = true) ->
                    segundos += linha.substringAfter(':').substringBefore(',').trim().toDoubleOrNull() ?: 0.0
            }
        }
        return DetalhesDaMidiaParaBaixar(
            largura = largura,
            altura = altura,
            duracaoMs = (segundos * 1000).roundToLong(),
            variasQualidades = variantes > 1,
            bitsPorSegundo = banda,
        )
    }

    /** DASH: maior width×height das representações de vídeo e mediaPresentationDuration (PT#H#M#S). */
    fun dash(texto: String): DetalhesDaMidiaParaBaixar {
        var largura = 0
        var altura = 0
        var representacoes = 0
        var bandaDoVideo = 0L
        var bandaDoAudio = 0L
        Regex("""(?is)<Representation\b([^>]*)>""").findAll(texto).forEach { r ->
            val atributos = r.groupValues[1]
            val bandaDaRepresentacao = Regex("""(?i)\bbandwidth\s*=\s*"(\d+)"""").find(atributos)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            if (!Regex("""(?i)\bwidth\s*=""").containsMatchIn(atributos)) {
                // Sem largura: trilha de áudio (entra na estimativa de tamanho).
                if (bandaDaRepresentacao > bandaDoAudio) bandaDoAudio = bandaDaRepresentacao
                return@forEach
            }
            if (bandaDaRepresentacao > bandaDoVideo) bandaDoVideo = bandaDaRepresentacao
            val l = Regex("""(?i)\bwidth\s*=\s*"(\d+)"""").find(atributos)?.groupValues?.get(1)?.toIntOrNull() ?: return@forEach
            val a = Regex("""(?i)\bheight\s*=\s*"(\d+)"""").find(atributos)?.groupValues?.get(1)?.toIntOrNull() ?: return@forEach
            representacoes++
            if (min(l, a) > min(largura, altura) || largura == 0) {
                largura = l
                altura = a
            }
        }
        val duracao = Regex("""(?i)mediaPresentationDuration\s*=\s*"([^"]+)"""").find(texto)?.groupValues?.get(1)
        return DetalhesDaMidiaParaBaixar(
            largura = largura,
            altura = altura,
            duracaoMs = duracao?.let { duracaoIso(it) } ?: 0,
            variasQualidades = representacoes > 1,
            bitsPorSegundo = bandaDoVideo + bandaDoAudio,
        )
    }

    /** "PT1H2M3.5S" → milissegundos. */
    fun duracaoIso(texto: String): Long {
        val achado = Regex("""(?i)P(?:(\d+)D)?T?(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?""").matchEntire(texto.trim()) ?: return 0
        val (d, h, m, s) = achado.destructured
        val segundos = (d.toDoubleOrNull() ?: 0.0) * 86400 + (h.toDoubleOrNull() ?: 0.0) * 3600 +
            (m.toDoubleOrNull() ?: 0.0) * 60 + (s.toDoubleOrNull() ?: 0.0)
        return (segundos * 1000).roundToLong()
    }
}

object TextoDaOpcaoDeMidiaParaBaixar {
    /** "1080p · 2:35 · 16,5 MB" (+ avisos) — o que ajuda a escolher. */
    fun texto(pedido: PedidoDeMidiaObservado, detalhes: DetalhesDaMidiaParaBaixar?, temAudioSeparado: Boolean): String {
        val soAudio = EscolhaDoPedidoDeMidiaParaBaixar.pareceAudio(pedido)
        val partes = mutableListOf<String>()
        when {
            soAudio -> partes += "Só áudio"
            detalhes != null && detalhes.linhas > 0 ->
                partes += "${detalhes.linhas}p" + (if (detalhes.variasQualidades) " (melhor de várias)" else "")
            pedido.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS ||
                pedido.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH -> partes += "Vídeo em partes"
            else -> partes += "Vídeo"
        }
        detalhes?.duracaoMs?.takeIf { it > 0 }?.let { partes += duracao(it) }
        val ehPlaylist = pedido.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_HLS ||
            pedido.tipo == TipoDePedidoDeMidiaParaDownload.PLAYLIST_DASH
        if (ehPlaylist) {
            // O tamanho observado é o do arquivo da playlist (1 KB), não o do vídeo: mostra a estimativa.
            detalhes?.tamanhoEstimado?.takeIf { it > 0 }?.let { partes += "≈ " + FormatoDoTamanhoDeMidiaParaDownload.texto(it) }
        } else {
            FormatoDoTamanhoDeMidiaParaDownload.texto(pedido.tamanho).takeIf { it.isNotBlank() }?.let { partes += it }
        }
        if (!soAudio && detalhes?.temAudio == false) {
            partes += if (temAudioSeparado) "som vem à parte (o app junta)" else "sem som"
        }
        return partes.joinToString(" · ")
    }

    /** 95_000 → "1:35"; 3_725_000 → "1:02:05". */
    fun duracao(ms: Long): String {
        val total = ms / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
    }

    /** Ordem da lista: vídeos da maior resolução para a menor (depois maior arquivo); áudio no fim. */
    fun ordenar(
        pedidos: List<PedidoDeMidiaObservado>,
        detalhes: Map<String, DetalhesDaMidiaParaBaixar>,
    ): List<PedidoDeMidiaObservado> = pedidos.sortedWith(
        compareBy<PedidoDeMidiaObservado> { EscolhaDoPedidoDeMidiaParaBaixar.pareceAudio(it) }
            .thenByDescending { detalhes[it.endereco]?.linhas ?: 0 }
            .thenByDescending { detalhes[it.endereco]?.tamanhoEstimado?.takeIf { e -> e > 0 } ?: it.tamanho },
    )
}
