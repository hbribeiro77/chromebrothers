package com.chromebrothers.navegador.regras

import java.net.URI

object DecisaoDeAbrirVideoNoPlayerNativo {
    private val extensoesDePagina = listOf(".html", ".htm", ".php", ".asp", ".aspx")

    fun podeAbrir(endereco: String): Boolean {
        val uri = try {
            URI(endereco.trim())
        } catch (_: Exception) {
            return false
        }
        val esquema = uri.scheme?.lowercase() ?: return false
        if (esquema != "http" && esquema != "https") {
            return false
        }
        val caminho = uri.path?.lowercase().orEmpty()
        return extensoesDePagina.none { caminho.endsWith(it) }
    }
}
