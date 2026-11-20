package com.chromebrothers.navegador.regras

import java.net.URI

object EnderecoDeInstalacaoDeExtensaoDaLojaDoFirefox {
    const val PAGINA = "https://addons.mozilla.org/pt-BR/firefox/extensions/"

    fun eh(url: String): Boolean {
        val uri = try {
            URI(url)
        } catch (_: Exception) {
            return false
        }
        if (!uri.scheme.equals("https", ignoreCase = true)) return false
        if (!uri.host.equals("addons.mozilla.org", ignoreCase = true)) return false
        val caminho = uri.path?.lowercase().orEmpty()
        val arquivo = caminho.startsWith("/firefox/downloads/file/") ||
            caminho.startsWith("/firefox/downloads/latest/")
        return arquivo && caminho.endsWith(".xpi")
    }

    fun deveBaixar(url: String, temGestoDoUsuario: Boolean, ehRedirecionamento: Boolean): Boolean {
        if (!eh(url)) return false
        return temGestoDoUsuario || ehRedirecionamento
    }
}
