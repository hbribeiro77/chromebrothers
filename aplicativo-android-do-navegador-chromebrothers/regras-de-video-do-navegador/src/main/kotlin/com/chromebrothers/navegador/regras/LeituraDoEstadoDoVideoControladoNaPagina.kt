package com.chromebrothers.navegador.regras

data class EstadoDoVideoControladoNaPagina(
    val pausado: Boolean,
    val posicaoSegundos: Double,
    val duracaoSegundos: Double,
    val velocidade: Double,
)

object LeituraDoEstadoDoVideoControladoNaPagina {
    fun ler(texto: String): EstadoDoVideoControladoNaPagina? {
        val pausado = booleano(texto, "pausado") ?: return null
        val posicao = numero(texto, "atual") ?: return null
        val duracao = numero(texto, "duracao") ?: return null
        val taxa = numero(texto, "taxa") ?: return null
        if (posicao.isNaN() || duracao.isNaN() || taxa.isNaN()) return null
        return EstadoDoVideoControladoNaPagina(
            pausado = pausado,
            posicaoSegundos = posicao.coerceAtLeast(0.0),
            duracaoSegundos = duracao.coerceAtLeast(0.0),
            velocidade = AjusteDeVelocidadeDeReproducaoDoVideo.ajustar(taxa),
        )
    }

    private fun booleano(texto: String, chave: String): Boolean? {
        val achado = Regex(""""$chave"\s*:\s*(true|false)""").find(texto) ?: return null
        return achado.groupValues[1] == "true"
    }

    private fun numero(texto: String, chave: String): Double? {
        val achado = Regex(""""$chave"\s*:\s*(-?\d+(?:\.\d+)?)""").find(texto) ?: return null
        return achado.groupValues[1].toDoubleOrNull()
    }
}
