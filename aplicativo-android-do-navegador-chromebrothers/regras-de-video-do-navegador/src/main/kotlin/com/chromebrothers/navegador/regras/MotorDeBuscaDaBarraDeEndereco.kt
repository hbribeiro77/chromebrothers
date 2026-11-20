package com.chromebrothers.navegador.regras

enum class MotorDeBuscaDaBarraDeEndereco(val id: String, val prefixo: String, val nome: String) {
    GOOGLE("google", "https://www.google.com/search?q=", "Google"),
    DUCKDUCKGO("duckduckgo", "https://duckduckgo.com/?q=", "DuckDuckGo"),
    BING("bing", "https://www.bing.com/search?q=", "Bing"),
    ;

    companion object {
        fun peloId(id: String): MotorDeBuscaDaBarraDeEndereco {
            return entries.firstOrNull { it.id == id } ?: GOOGLE
        }
    }
}
