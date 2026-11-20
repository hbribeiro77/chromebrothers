package com.chromebrothers.navegador.regras

/**
 * O detector roda em todos os quadros (iframes inclusive, ex.: player do Vimeo).
 * Os comandos ao vídeo precisam ir para o quadro que o anunciou, não só para a página de cima.
 */
object RoteamentoDosComandosDoVideoParaOQuadroDeOrigemNaPagina {
    private val COMANDO = Regex("""^\s*window\.chromebrothers\w+\(\s*"([^"\\]+)"""")

    private val AVISOS_COM_ID_NO_PRIMEIRO_ARGUMENTO = setOf("onVideoPlaying", "onVideoBounds", "onVideoLongPress")

    fun idDoVideoNoComando(script: String): String? = COMANDO.find(script)?.groupValues?.get(1)

    fun idDoVideoNoAviso(nome: String, primeiroArgumento: String?): String? {
        if (nome !in AVISOS_COM_ID_NO_PRIMEIRO_ARGUMENTO) return null
        return primeiroArgumento?.takeIf { it.isNotBlank() }
    }
}
