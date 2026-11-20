package com.chromebrothers.navegador.regras

/**
 * Decisões puras da grade nativa do Visualizador de mídia (colunas, filtro, visor, toque).
 */
object RegrasDaGradeNativaDoVisualizadorDeMidiaNaColecao {
    const val LARGURA_MINIMA_DA_CARTA_DP: Int = 108
    const val COLUNAS_NO_MODO_MAIORES: Int = 2
    const val ESPERA_DO_TOQUE_LONGO_MS: Long = 300L

    enum class Filtro(val chave: String) {
        TUDO("tudo"),
        IMAGENS("imagens"),
        VIDEOS("videos"),
        MCP("mcp"),
        ;

        companion object {
            fun daChave(chave: String?): Filtro = entries.firstOrNull { it.chave == chave } ?: TUDO
        }
    }

    private val extensoesDeVideo = Regex("""\.(mp4|webm|mkv|mov|m4v|avi)$""", RegexOption.IGNORE_CASE)
    private val extensoesDeImagem = Regex("""\.(jpe?g|png|webp|gif|bmp|heic|heif|avif)$""", RegexOption.IGNORE_CASE)

    /** Só imagem e vídeo entram na grade (PDF, ZIP etc. da pasta ficam de fora). */
    fun ehMidia(item: ItemDaPastaEscolhidaPeloComponente): Boolean {
        if (item.pasta) return false
        val mime = item.mime.lowercase()
        if (mime.startsWith("image/") || mime.startsWith("video/")) return true
        val alvo = item.nome.ifBlank { item.uri.substringBefore('?') }
        return extensoesDeImagem.containsMatchIn(alvo) || extensoesDeVideo.containsMatchIn(alvo)
    }

    fun ehVideo(item: ItemDaPastaEscolhidaPeloComponente): Boolean {
        if (item.mime.lowercase().startsWith("video/")) return true
        val alvo = item.nome.ifBlank { item.uri.substringBefore('?') }
        return extensoesDeVideo.containsMatchIn(alvo)
    }

    /** Colunas que cabem em [larguraDisponivelPx] com carta mínima de 108dp (ou 2 no modo Maiores). */
    fun colunas(larguraDisponivelPx: Int, densidade: Float, maiores: Boolean): Int {
        if (maiores) return COLUNAS_NO_MODO_MAIORES
        val minimaPx = (LARGURA_MINIMA_DA_CARTA_DP * densidade).coerceAtLeast(1f)
        return (larguraDisponivelPx / minimaPx).toInt().coerceAtLeast(1)
    }

    fun filtrar(
        itens: List<ItemDaPastaEscolhidaPeloComponente>,
        filtro: Filtro,
    ): List<ItemDaPastaEscolhidaPeloComponente> = itens.filter { item ->
        if (!ehMidia(item)) return@filter false
        when (filtro) {
            Filtro.TUDO -> true
            Filtro.IMAGENS -> !ehVideo(item)
            Filtro.VIDEOS -> ehVideo(item)
            Filtro.MCP -> MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao.ehDoMcp(item.uri)
        }
    }

    /** Vídeos visíveis (na ordem da grade) para o player embutido e o índice do tocado. */
    fun videosParaOPlayer(
        visiveis: List<ItemDaPastaEscolhidaPeloComponente>,
        uriTocada: String,
    ): Pair<List<String>, Int> {
        val videos = visiveis.filter { ehVideo(it) }.map { it.uri }
        val indice = videos.indexOf(uriTocada).coerceAtLeast(0)
        return videos to indice
    }

    /** Lista de imagens (sem vídeos) para o visor e o índice da tocada nela. */
    fun imagensParaOVisor(
        visiveis: List<ItemDaPastaEscolhidaPeloComponente>,
        uriTocada: String,
    ): Pair<List<String>, Int> {
        val imagens = visiveis.filter { !ehVideo(it) }.map { it.uri }
        val indice = imagens.indexOf(uriTocada).coerceAtLeast(0)
        return imagens to indice
    }

    /** Movimento até aqui (px²) ainda é toque parado; acima disso é rolagem. */
    fun moveuAlemDaFolga(dx: Float, dy: Float, folgaPx: Float): Boolean =
        dx * dx + dy * dy > folgaPx * folgaPx
}
