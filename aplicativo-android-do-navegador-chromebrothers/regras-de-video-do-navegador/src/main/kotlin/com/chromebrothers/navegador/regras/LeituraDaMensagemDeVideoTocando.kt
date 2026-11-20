package com.chromebrothers.navegador.regras

data class MensagemDeVideoTocandoNaPagina(
    val id: String,
    val endereco: String,
    val velocidade: Double,
)

object LeituraDaMensagemDeVideoTocando {
    fun ler(id: String, endereco: String, velocidadeTexto: String): MensagemDeVideoTocandoNaPagina {
        val pedida = velocidadeTexto.toDoubleOrNull() ?: 1.0
        return MensagemDeVideoTocandoNaPagina(
            id = id,
            endereco = endereco.trim(),
            velocidade = AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(pedida),
        )
    }
}
