package com.chromebrothers.navegador.regras

object IncrementosDeDeslocamentoDoPlayerDeVideo {
    val opcoes = listOf(2, 5, 10, 30)
    const val PADRAO = 10

    fun ajustar(segundos: Int): Int = if (segundos in opcoes) segundos else PADRAO
}
