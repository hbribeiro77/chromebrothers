package com.chromebrothers.navegador.regras

object TraducaoDoPedidoDeArquivoDoFirefox {
    const val TIPO_UM_ARQUIVO = 1
    const val TIPO_VARIOS_ARQUIVOS = 2
    const val TIPO_PASTA = 3

    fun escolhePasta(tipo: Int): Boolean = tipo == TIPO_PASTA

    fun variosArquivos(tipo: Int): Boolean = tipo == TIPO_VARIOS_ARQUIVOS

    fun tipos(mimeTypes: Array<String>?): List<String> = mimeTypes?.toList().orEmpty()

    fun tiposQueOSeletorEntende(mimeTypes: List<String>): List<String> {
        val aceite = Regex("^[a-z0-9.+-]+/[a-z0-9.+*-]+$")
        return mimeTypes.map { it.trim().lowercase() }.filter { it != "*/*" && aceite.matches(it) }
    }
}
