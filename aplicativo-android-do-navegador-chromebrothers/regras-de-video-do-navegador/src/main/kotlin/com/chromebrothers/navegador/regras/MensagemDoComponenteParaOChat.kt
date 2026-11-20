package com.chromebrothers.navegador.regras

object MensagemDoComponenteParaOChat {
    const val LIMITE = 100_000

    fun limpar(texto: String): String? {
        val limpo = texto.trim()
        if (limpo.isEmpty()) return null
        return limpo.take(LIMITE)
    }
}
