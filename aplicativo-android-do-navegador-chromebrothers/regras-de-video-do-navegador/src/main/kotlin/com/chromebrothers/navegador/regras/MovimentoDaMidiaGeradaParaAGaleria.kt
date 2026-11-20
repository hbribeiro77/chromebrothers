package com.chromebrothers.navegador.regras

/**
 * "Mover para a Galeria": a mídia gerada via MCP sai do armazenamento interno do app (que some ao
 * desinstalar e a Galeria não enxerga) e vai para Movies/ChromeBrothers (vídeos) ou
 * Pictures/ChromeBrothers (imagens). A cópia interna só é apagada depois de a cópia pública conferir.
 */
object MovimentoDaMidiaGeradaParaAGaleria {
    const val PASTA_DOS_VIDEOS = "Movies/ChromeBrothers"
    const val PASTA_DAS_IMAGENS = "Pictures/ChromeBrothers"

    /** Só a mídia gerada guardada dentro do app (file:// nas pastas do MCP) pode ser movida. */
    fun podeMover(uri: String): Boolean = MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao.ehDoMcp(uri)

    fun ehVideo(nomeDoArquivo: String): Boolean {
        val extensao = nomeDoArquivo.substringAfterLast('.', "").lowercase()
        return extensao in setOf("mp4", "webm", "mov", "m4v", "mkv")
    }

    fun pastaDeDestino(nomeDoArquivo: String): String =
        if (ehVideo(nomeDoArquivo)) PASTA_DOS_VIDEOS else PASTA_DAS_IMAGENS

    fun mime(nomeDoArquivo: String): String = when (nomeDoArquivo.substringAfterLast('.', "").lowercase()) {
        "mp4", "m4v" -> "video/mp4"
        "webm" -> "video/webm"
        "mov" -> "video/quicktime"
        "mkv" -> "video/x-matroska"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }

    /** A cópia pública só vale se tiver exatamente o tamanho do original (e o original não for vazio). */
    fun copiaConfere(tamanhoDoOriginal: Long, tamanhoDaCopia: Long): Boolean =
        tamanhoDoOriginal > 0 && tamanhoDaCopia == tamanhoDoOriginal

    /** Vídeo em Movies/ChromeBrothers (entra na pasta virtual do Visualizador). */
    fun ehDaPastaDosVideosMovidos(caminhoRelativo: String): Boolean {
        val limpo = caminhoRelativo.replace('\\', '/').trim('/')
        return limpo.equals(PASTA_DOS_VIDEOS, ignoreCase = true) ||
            limpo.startsWith("$PASTA_DOS_VIDEOS/", ignoreCase = true)
    }

    /** Aviso curto ao terminar. */
    fun resumo(movidos: Int, falhas: Int): String = when {
        movidos == 0 && falhas == 0 -> "Nada para mover."
        falhas == 0 && movidos == 1 -> "Movido para a Galeria."
        falhas == 0 -> "$movidos arquivos movidos para a Galeria."
        movidos == 0 -> "Não deu para mover para a Galeria."
        else -> "$movidos " + (if (movidos == 1) "movido" else "movidos") + " para a Galeria; $falhas " +
            (if (falhas == 1) "não deu certo." else "não deram certo.")
    }
}
