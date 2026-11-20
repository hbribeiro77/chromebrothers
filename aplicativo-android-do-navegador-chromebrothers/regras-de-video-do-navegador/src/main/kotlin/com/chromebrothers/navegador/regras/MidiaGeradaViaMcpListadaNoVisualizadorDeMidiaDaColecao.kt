package com.chromebrothers.navegador.regras

/**
 * Imagens e vídeos gerados via MCP (Higgsfield, Sjinn, Topview) ficam no armazenamento interno do app;
 * aqui eles viram itens da pasta virtual do Visualizador de mídia, ao lado de Downloads e Pictures.
 */
object MidiaGeradaViaMcpListadaNoVisualizadorDeMidiaDaColecao {
    const val PASTA_DAS_IMAGENS: String = "biblioteca-de-imagens-capturadas"
    const val PASTA_DOS_VIDEOS: String = "biblioteca-de-videos-higgsfield"

    private val origensDoMcp = setOf(
        OrigemDaMidiaNaBibliotecaDoNavegador.HIGGSFIELD,
        OrigemDaMidiaNaBibliotecaDoNavegador.SJINN,
        OrigemDaMidiaNaBibliotecaDoNavegador.TOPVIEW,
        OrigemDaMidiaNaBibliotecaDoNavegador.VIVIX,
    )

    fun ehOrigemDoMcp(origem: String): Boolean = OrigemDaMidiaNaBibliotecaDoNavegador.parse(origem) in origensDoMcp

    /** As capturas de tela também vão para a pasta das imagens, mas só entram na lista as de origem MCP. */
    fun ehDoMcp(uri: String): Boolean {
        val limpa = uri.trim().replace('\\', '/')
        if (!limpa.startsWith("file:", ignoreCase = true)) return false
        return limpa.contains("/$PASTA_DAS_IMAGENS/") || limpa.contains("/$PASTA_DOS_VIDEOS/")
    }

    /**
     * [pastaDasImagens] completa o caminho de imagens antigas sem `caminhoLocal`;
     * [tamanhoDoArquivo] devolve -1 se o arquivo sumiu (o item fica de fora).
     */
    fun listar(
        imagens: List<ItemDaBibliotecaDeImagensCapturadas>,
        videos: List<ItemDaBibliotecaDeVideos>,
        pastaDasImagens: String,
        tamanhoDoArquivo: (caminho: String) -> Long,
    ): List<ArquivoDeMidiaSalvoNaGaveta> {
        val resultado = mutableListOf<ArquivoDeMidiaSalvoNaGaveta>()
        imagens.filter { ehOrigemDoMcp(it.origem) }.forEach { item ->
            val caminho = item.caminhoLocal.ifBlank { "${pastaDasImagens.trimEnd('/', '\\')}/${item.nomeDoArquivo}" }
            val tamanho = tamanhoDoArquivo(caminho)
            if (tamanho < 0) return@forEach
            resultado += ArquivoDeMidiaSalvoNaGaveta(
                uri = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(caminho),
                nomeDoArquivo = caminho.substringAfterLast('/').substringAfterLast('\\'),
                instante = item.instante,
                tamanho = tamanho,
                mime = mimeDaImagem(caminho),
            )
        }
        videos.filter { ehOrigemDoMcp(it.origem) && it.caminhoLocal.isNotBlank() }.forEach { item ->
            val tamanho = tamanhoDoArquivo(item.caminhoLocal)
            if (tamanho < 0) return@forEach
            resultado += ArquivoDeMidiaSalvoNaGaveta(
                uri = RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(item.caminhoLocal),
                nomeDoArquivo = item.caminhoLocal.substringAfterLast('/').substringAfterLast('\\'),
                instante = item.instante,
                tamanho = tamanho,
                mime = "video/mp4",
            )
        }
        return resultado.sortedByDescending { it.instante }
    }

    /** Tira da biblioteca de vídeos os itens cujo arquivo foi excluído pelo Visualizador. */
    fun videosRestantes(
        videos: List<ItemDaBibliotecaDeVideos>,
        urisExcluidas: Collection<String>,
    ): List<ItemDaBibliotecaDeVideos> {
        val alvos = urisExcluidas.map(::normalizar).toSet()
        if (alvos.isEmpty()) return videos
        return videos.filterNot { item ->
            item.caminhoLocal.isNotBlank() &&
                normalizar(RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(item.caminhoLocal)) in alvos
        }
    }

    /** Idem para a biblioteca de imagens (itens antigos sem `caminhoLocal` usam [pastaDasImagens]). */
    fun imagensRestantes(
        imagens: List<ItemDaBibliotecaDeImagensCapturadas>,
        urisExcluidas: Collection<String>,
        pastaDasImagens: String,
    ): List<ItemDaBibliotecaDeImagensCapturadas> {
        val alvos = urisExcluidas.map(::normalizar).toSet()
        if (alvos.isEmpty()) return imagens
        return imagens.filterNot { item ->
            val caminho = item.caminhoLocal.ifBlank { "${pastaDasImagens.trimEnd('/', '\\')}/${item.nomeDoArquivo}" }
            normalizar(RespostaJsonDaPonteDeGeracaoMcp.fileUrlDeCaminho(caminho)) in alvos
        }
    }

    /** `file:/a` e `file:///a` apontam para o mesmo arquivo. */
    private fun normalizar(uri: String): String =
        uri.trim().replace('\\', '/').replaceFirst(Regex("^file:/+", RegexOption.IGNORE_CASE), "file:/")

    private fun mimeDaImagem(caminho: String): String = when (caminho.substringAfterLast('.').lowercase()) {
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        else -> "image/jpeg"
    }
}
