package com.chromebrothers.navegador.regras

object IconeDaPaginaNaBarraDeEndereco {
    private val cores = intArrayOf(
        0xFF1A73E8.toInt(),
        0xFF188038.toInt(),
        0xFFC5221F.toInt(),
        0xFFE37400.toInt(),
        0xFF9334E6.toInt(),
        0xFF007B83.toInt(),
        0xFF1967D2.toInt(),
        0xFFB31412.toInt(),
    )

    fun hospedeiro(endereco: String): String {
        val host = runCatching { java.net.URI(endereco.trim()).host }.getOrNull().orEmpty()
        return host.lowercase().removePrefix("www.")
    }

    fun letra(titulo: String, endereco: String): String {
        val base = titulo.trim().ifBlank { hospedeiro(endereco) }
        val caractere = base.firstOrNull { it.isLetterOrDigit() } ?: return "?"
        return caractere.uppercaseChar().toString()
    }

    fun cor(endereco: String): Int {
        val chave = hospedeiro(endereco).ifBlank { endereco.trim() }
        val indice = (chave.hashCode() and Int.MAX_VALUE) % cores.size
        return cores[indice]
    }
}

object FaixaDeAtalhosDaBarraDeEndereco {
    const val LIMITE = 8

    fun montar(
        fixos: List<AtalhoDaPaginaInicial>,
        visitas: List<VisitaDePaginaNoNavegador>,
        limite: Int = LIMITE,
    ): List<AtalhoDaPaginaInicial> {
        val vistos = linkedSetOf<String>()
        val saida = mutableListOf<AtalhoDaPaginaInicial>()
        fun acrescentar(titulo: String, endereco: String) {
            if (saida.size >= limite) return
            val host = IconeDaPaginaNaBarraDeEndereco.hospedeiro(endereco)
            if (host.isBlank() || !vistos.add(host)) return
            saida.add(AtalhoDaPaginaInicial(titulo.trim().ifBlank { host }, endereco))
        }
        fixos.forEach { acrescentar(it.titulo, it.endereco) }
        visitas.forEach { acrescentar(it.titulo, it.endereco) }
        return saida
    }
}
