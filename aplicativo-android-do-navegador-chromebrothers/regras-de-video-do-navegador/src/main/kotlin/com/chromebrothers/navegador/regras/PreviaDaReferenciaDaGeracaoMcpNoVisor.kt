package com.chromebrothers.navegador.regras

/**
 * Tocar numa referência do gerador MCP abre o visor com os bytes exatos que serão enviados
 * (não a miniatura de 320px do HTML). Os bytes vão para arquivos temporários nesta pasta do cache.
 */
object PreviaDaReferenciaDaGeracaoMcpNoVisor {
    const val PASTA = "previas-de-referencia"

    /** Prefixo dos arquivos de um gerador ("imagem" / "video"): limpa só as prévias dele. */
    fun prefixo(tipo: String): String = "ref-" + tipo.trim().lowercase().ifBlank { "imagem" } + "-"

    /** Nome único por gravação: o cache de miniaturas é por endereço e não pode mostrar a ref antiga. */
    fun nomeDoArquivo(tipo: String, indice: Int, carimbo: Long, extensao: String): String =
        prefixo(tipo) + indice + "-" + carimbo + "." + extensao.ifBlank { "jpg" }

    fun extensaoDoMime(mime: String): String = when (mime.lowercase()) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    /** Prévia de referência aberta no visor: só para conferir (sem arraste nem menu de mover/excluir). */
    fun ehPrevia(uri: String): Boolean = uri.substringBefore('?').contains("/$PASTA/")
}
