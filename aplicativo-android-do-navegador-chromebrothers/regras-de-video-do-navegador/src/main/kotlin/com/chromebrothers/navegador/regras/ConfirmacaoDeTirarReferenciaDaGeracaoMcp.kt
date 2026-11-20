package com.chromebrothers.navegador.regras

/** Textos do modal que confirma tirar referência(s) do gerador MCP (o "×" fica fácil de tocar sem querer). */
object ConfirmacaoDeTirarReferenciaDaGeracaoMcp {
    const val CONFIRMAR = "Tirar"

    fun tituloDeUma(): String = "Tirar referência?"

    /** [indice] começa em 0; o texto mostra a posição como o usuário vê (1, 2, …). */
    fun mensagemDeUma(indice: Int): String =
        "A referência ${indice + 1} sai deste pedido. O arquivo original continua onde estava."

    fun tituloDeTodas(quantidade: Int): String =
        if (quantidade == 1) "Tirar a referência?" else "Tirar as $quantidade referências?"

    fun mensagemDeTodas(quantidade: Int): String =
        (if (quantidade == 1) "A referência sai" else "As $quantidade referências saem") +
            " deste pedido. Os arquivos originais continuam onde estavam."
}
