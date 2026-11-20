package com.chromebrothers.navegador.regras

object LimiteDeJanelasFlutuantesDaColecaoDoChat {
    /** Janelas visíveis (aberta ou bolha) ao mesmo tempo. */
    const val MAXIMO: Int = 3

    /** Todas as WebViews da coleção: flutuantes, abas no chat e pré-cargas invisíveis (warm-start). */
    const val MAXIMO_TOTAL: Int = MAXIMO + AbasDeComponentesAncoradosNaGavetaDoChatDoNavegador.MAXIMO_DE_COMPONENTES + 2

    fun podeAbrir(quantidadeVisivelAtual: Int): Boolean = quantidadeVisivelAtual < MAXIMO

    fun podeCriarTotal(quantidadeTotalAtual: Int): Boolean = quantidadeTotalAtual < MAXIMO_TOTAL

    fun mensagemRecusada(): String =
        "Já há $MAXIMO janelas da coleção abertas. Feche uma para abrir outra (máximo $MAXIMO simultaneamente)."
}
