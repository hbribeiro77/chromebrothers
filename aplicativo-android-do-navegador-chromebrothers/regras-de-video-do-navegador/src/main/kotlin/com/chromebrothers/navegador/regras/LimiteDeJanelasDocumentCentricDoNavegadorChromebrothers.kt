package com.chromebrothers.navegador.regras

object LimiteDeJanelasDocumentCentricDoNavegadorChromebrothers {
    const val MAXIMO = 3
    const val EXTRA_ENDERECO_INICIAL = "endereco_inicial_da_janela_do_navegador_chromebrothers"

    fun podeAbrir(quantidadeAtual: Int): Boolean = quantidadeAtual < MAXIMO

    fun mensagemRecusada(): String =
        "Já há $MAXIMO janelas abertas. Feche uma para abrir outra."
}
