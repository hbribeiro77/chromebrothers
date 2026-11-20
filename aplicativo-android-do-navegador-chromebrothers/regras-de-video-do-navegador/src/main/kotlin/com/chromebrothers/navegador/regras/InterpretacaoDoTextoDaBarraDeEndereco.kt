package com.chromebrothers.navegador.regras

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object InterpretacaoDoTextoDaBarraDeEndereco {
    fun resolver(
        digitado: String,
        motor: MotorDeBuscaDaBarraDeEndereco = MotorDeBuscaDaBarraDeEndereco.GOOGLE,
    ): String {
        val texto = digitado.trim()
        if (texto.isEmpty()) return ""
        if (texto.contains("://")) return texto
        if (texto.any { it.isWhitespace() } || !pareceEndereco(texto)) {
            val consulta = URLEncoder.encode(texto, StandardCharsets.UTF_8)
            return motor.prefixo + consulta
        }
        return "https://$texto"
    }

    private fun pareceEndereco(texto: String): Boolean {
        val host = texto.substringBefore("/").substringBefore("?").substringBefore("#")
        if (host.equals("localhost", ignoreCase = true)) return true
        if (host.startsWith("localhost:", ignoreCase = true)) return true
        return host.contains(".") && !host.startsWith(".") && !host.endsWith(".")
    }
}
