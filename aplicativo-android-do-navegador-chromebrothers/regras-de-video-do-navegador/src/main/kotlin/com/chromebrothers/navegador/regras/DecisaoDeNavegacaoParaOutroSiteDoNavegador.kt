package com.chromebrothers.navegador.regras

import java.net.URI

object DecisaoDeNavegacaoParaOutroSite {
    fun deveCancelar(
        bloqueioLigado: Boolean,
        temGestoDoUsuario: Boolean,
        ehRedirecionamento: Boolean,
        urlAtual: String,
        urlNova: String,
        lista: ListaDeBloqueioDeAnuncio,
        pedidaPeloAplicativo: Boolean = false,
    ): Boolean {
        if (!bloqueioLigado || urlNova.isBlank()) return false
        val destinoDeAnuncio = DecisaoDeBloqueioDeAnuncio.deveBloquear(
            habilitado = true,
            ehQuadroPrincipal = false,
            urlDoPedido = urlNova,
            urlDaPagina = urlAtual.ifBlank { urlNova },
            lista = lista,
        )
        if (destinoDeAnuncio) return true
        if (ehPaginaDeEntregaDeLoginDoGoogle(urlAtual)) return false
        if (pedidaPeloAplicativo || temGestoDoUsuario || ehRedirecionamento || urlAtual.isBlank()) return false
        return !DecisaoDeBloqueioDeAnuncio.urlsDoMesmoSite(urlAtual, urlNova)
    }

    fun ehPaginaDeEntregaDeLoginDoGoogle(url: String): Boolean {
        val host = try {
            URI(url).host?.lowercase()?.trim('.') ?: ""
        } catch (_: Exception) {
            ""
        }
        return host == "accounts.google.com"
    }
}
