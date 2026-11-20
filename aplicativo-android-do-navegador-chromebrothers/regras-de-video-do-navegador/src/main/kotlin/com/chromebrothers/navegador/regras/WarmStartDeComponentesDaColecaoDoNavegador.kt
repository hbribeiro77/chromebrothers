package com.chromebrothers.navegador.regras

/**
 * Decide quais componentes HTML da coleção entram no pré-carregamento invisível (warm-start).
 * Marcação: tag `warm-start`, IDs fixos dos MCP, ou nome do Visualizador de mídia.
 */
object WarmStartDeComponentesDaColecaoDoNavegador {
    const val TAG: String = "warm-start"

    val IDS_MCP: Set<String> = setOf(
        HtmlDoComponenteDeGeracaoDeImagemViaMcpNaColecao.ID,
        HtmlDoComponenteDeGeracaoDeVideoViaMcpNaColecao.ID,
    )

    fun marcadoNoComponente(item: ItemDaBibliotecaDeComponentesHtmlDaColecao): Boolean {
        if (item.id in IDS_MCP) return true
        if (item.tags.any { it.equals(TAG, ignoreCase = true) }) return true
        val nome = item.nome.trim().lowercase()
        return nome.contains("visualizador de mídia") || nome.contains("visualizador de midia")
    }

    fun devePreCarregar(
        item: ItemDaBibliotecaDeComponentesHtmlDaColecao,
        preferenciaGlobalLigada: Boolean,
    ): Boolean = preferenciaGlobalLigada && marcadoNoComponente(item)

    fun filtrarParaPreCarga(
        itens: List<ItemDaBibliotecaDeComponentesHtmlDaColecao>,
        preferenciaGlobalLigada: Boolean,
    ): List<ItemDaBibliotecaDeComponentesHtmlDaColecao> {
        if (!preferenciaGlobalLigada) return emptyList()
        return itens.filter { marcadoNoComponente(it) }
            .distinctBy { it.id }
    }
}
