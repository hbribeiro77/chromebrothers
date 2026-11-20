package com.chromebrothers.navegador.regras

data class AvisoDeDeslocamentoDoPlayerDeVideo(
    val texto: String,
    val visivel: Boolean,
)

class AcumuloDoAvisoDeDeslocamentoDoPlayerDeVideo(
    private val janelaMillis: Long = 1200,
) {
    private var total = 0
    private var ultimoInstante = 0L

    fun registrar(segundos: Int, instante: Long): AvisoDeDeslocamentoDoPlayerDeVideo {
        if (ultimoInstante == 0L || instante - ultimoInstante > janelaMillis) {
            total = 0
        }
        total += segundos
        ultimoInstante = instante
        val texto = when {
            total > 0 -> "+$total"
            total < 0 -> total.toString()
            else -> ""
        }
        return AvisoDeDeslocamentoDoPlayerDeVideo(texto, total != 0)
    }

    fun reiniciar() {
        total = 0
        ultimoInstante = 0L
    }
}
