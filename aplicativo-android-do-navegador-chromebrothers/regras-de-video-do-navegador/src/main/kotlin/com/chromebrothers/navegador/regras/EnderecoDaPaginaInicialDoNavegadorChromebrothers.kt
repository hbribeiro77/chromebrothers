package com.chromebrothers.navegador.regras

object EnderecoDaPaginaInicialDoNavegadorChromebrothers {
    const val URL = "https://pagina-inicial.chromebrothers/"

    fun eh(endereco: String?): Boolean {
        if (endereco.isNullOrBlank()) return false
        return endereco == URL.trimEnd('/') || endereco.startsWith(URL)
    }
}
