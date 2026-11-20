package com.chromebrothers.navegador.regras

import java.net.URI

/**
 * `window.open` / onNewSession: por padrão vira **nova aba**.
 * Só fica na modal de login (overlay com fechar) quando o destino já é
 * um provedor de OAuth/login conhecido — ex.: Google GSI.
 */
object JanelaNovaDoSiteComoModalDeLoginDoNavegador {
    fun deveAbrirComoModalDeLogin(uri: String): Boolean {
        val host = hospedeiro(uri)
        if (host.isEmpty()) return false
        if (host == "accounts.google.com" || host == "accounts.youtube.com") return true
        if (host == "appleid.apple.com") return true
        if (host == "login.microsoftonline.com" || host == "login.live.com" || host == "account.live.com") {
            return true
        }
        if (host == "oauth.telegram.org") return true
        if (host.endsWith(".okta.com")) return true
        return false
    }

    fun hospedeiro(uri: String): String {
        if (uri.isBlank() || uri.equals("about:blank", ignoreCase = true)) return ""
        return try {
            URI(uri).host?.lowercase()?.trim('.') ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
