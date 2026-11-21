package com.chromebrothers.navegador.regras

/**
 * Recorte de um trecho do vídeo sem reencodar. Início exato via "edit list" do MP4 (o arquivo guarda desde o
 * quadro-chave e o player começa no ponto escolhido); se não der, o início recua ao quadro-chave anterior.
 * O fim é sempre exato. Salvo em Movies/ChromeBrothers (Galeria).
 */
object RecorteDeTrechoDeVideo {
    /** Trecho mínimo para valer a pena salvar. */
    const val DURACAO_MINIMA_MS = 500L

    /** Diferença até a qual o início "já está" num quadro-chave (não precisa reencodar nada). */
    const val TOLERANCIA_DO_QUADRO_CHAVE_MS = 40L

    /** O recorte só vale para arquivo do aparelho (stream de site não tem o que copiar). */
    fun podeRecortar(endereco: String?): Boolean {
        val e = endereco?.trim()?.lowercase().orEmpty()
        // A biblioteca guarda "file:/data/…" (uma barra só, forma do File.toURI) — vale também.
        return e.startsWith("content://") || e.startsWith("file:") || e.startsWith("/")
    }

    /** Último quadro-chave em ou antes de [ms] (0 se a lista estiver vazia). [chaves] em ordem crescente. */
    fun quadroChaveAnterior(chaves: List<Long>, ms: Long): Long =
        chaves.lastOrNull { it <= ms + TOLERANCIA_DO_QUADRO_CHAVE_MS } ?: 0L

    /** Início sem nenhum encode: recua ao quadro-chave anterior. */
    fun inicioSemEncode(chaves: List<Long>, inicioMs: Long): Long = quadroChaveAnterior(chaves, inicioMs)

    /** O início escolhido já cai num quadro-chave: nem o corte inteligente precisa reencodar. */
    fun inicioJaEhQuadroChave(chaves: List<Long>, inicioMs: Long): Boolean =
        chaves.any { kotlin.math.abs(it - inicioMs) <= TOLERANCIA_DO_QUADRO_CHAVE_MS } || inicioMs <= TOLERANCIA_DO_QUADRO_CHAVE_MS

    /** Início e fim em ordem, dentro do vídeo; null se o trecho ficar curto demais. */
    fun trechoValido(inicioMs: Long, fimMs: Long, duracaoMs: Long): Pair<Long, Long>? {
        val limite = if (duracaoMs > 0) duracaoMs else Long.MAX_VALUE
        val a = minOf(inicioMs, fimMs).coerceIn(0L, limite)
        val b = maxOf(inicioMs, fimMs).coerceIn(0L, limite)
        return if (b - a >= DURACAO_MINIMA_MS) a to b else null
    }

    /** Tamanho aproximado do recorte: proporcional ao trecho (bitrate médio do arquivo). */
    fun tamanhoEstimadoBytes(tamanhoDoArquivo: Long, duracaoMs: Long, inicioMs: Long, fimMs: Long): Long {
        if (tamanhoDoArquivo <= 0 || duracaoMs <= 0 || fimMs <= inicioMs) return -1
        return tamanhoDoArquivo * (fimMs - inicioMs) / duracaoMs
    }

    /** 0:12,4 / 1:05:03,0 — décimos ajudam a ver o ajuste ao quadro-chave. */
    fun tempoComDecimos(ms: Long): String {
        val total = ms.coerceAtLeast(0L)
        val decimos = (total % 1000) / 100
        val segundos = (total / 1000) % 60
        val minutos = (total / 60_000) % 60
        val horas = total / 3_600_000
        return if (horas > 0) {
            "%d:%02d:%02d,%d".format(horas, minutos, segundos, decimos)
        } else {
            "%d:%02d,%d".format(minutos, segundos, decimos)
        }
    }

    fun tamanhoLegivel(bytes: Long): String = when {
        bytes < 0 -> "?"
        bytes < 1024 * 1024 -> "%d KB".format((bytes / 1024).coerceAtLeast(1))
        bytes < 1024L * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024)).replace('.', ',')
        else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024)).replace('.', ',')
    }

    /** "trailer_recorte_0m12s-0m45s.mp4" a partir do nome original (sem caracteres proibidos). */
    fun nomeSugerido(nomeOriginal: String, inicioMs: Long, fimMs: Long): String {
        val base = nomeOriginal.substringAfterLast('/').substringBefore('?').substringBeforeLast('.')
            .ifBlank { "video" }
        return limparNome(base) + "_recorte_" + marca(inicioMs) + "-" + marca(fimMs) + ".mp4"
    }

    /** Nome digitado: sem barras/caracteres proibidos e sempre terminando em .mp4. */
    fun nomeFinal(digitado: String, sugerido: String): String {
        val limpo = limparNome(digitado.trim().removeSuffix(".mp4").removeSuffix(".MP4")).trim('.', ' ')
        return if (limpo.isBlank()) sugerido else "$limpo.mp4"
    }

    private fun marca(ms: Long): String {
        val s = ms.coerceAtLeast(0L) / 1000
        val h = s / 3600
        val m = (s % 3600) / 60
        val seg = s % 60
        return if (h > 0) "${h}h${m}m${seg}s" else "${m}m${seg}s"
    }

    private fun limparNome(texto: String): String =
        texto.replace(Regex("[\\\\/:*?\"<>|\\u0000-\\u001F]"), "_").replace(Regex("\\s+"), " ").take(120)
}
