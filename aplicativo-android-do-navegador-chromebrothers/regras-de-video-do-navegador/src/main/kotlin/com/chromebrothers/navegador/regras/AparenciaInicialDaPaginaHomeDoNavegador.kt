package com.chromebrothers.navegador.regras

/**
 * A home não pode depender do JS para pintar o fundo: até o script rodar,
 * o Gecko mostra o CSS inicial (branco) — daí a piscada no tema escuro.
 */
data class AparenciaInicialDaPaginaHomeDoNavegador(
    val classeDoCorpo: String,
    val fundoCss: String,
    val esquemaDeCor: String,
) {
    companion object {
        fun aPartirDoEstado(estadoJson: String): AparenciaInicialDaPaginaHomeDoNavegador {
            val escura = estadoJsonContemEscura(estadoJson)
            return if (escura) {
                AparenciaInicialDaPaginaHomeDoNavegador(
                    classeDoCorpo = "escura",
                    fundoCss = "#202124",
                    esquemaDeCor = "dark",
                )
            } else {
                AparenciaInicialDaPaginaHomeDoNavegador(
                    classeDoCorpo = "",
                    fundoCss = "#ffffff",
                    esquemaDeCor = "light",
                )
            }
        }

        fun aplicarNoDocumento(
            documento: String,
            aparencia: AparenciaInicialDaPaginaHomeDoNavegador,
        ): String = documento
            .replace("%%CLASSE_CORPO%%", aparencia.classeDoCorpo)
            .replace("%%FUNDO%%", aparencia.fundoCss)
            .replace("%%ESQUEMA%%", aparencia.esquemaDeCor)

        private fun estadoJsonContemEscura(estadoJson: String): Boolean {
            val limpo = estadoJson.trim()
            if (limpo.isEmpty()) return false
            // Aceita "escura":true com ou sem espaços.
            return Regex("\"escura\"\\s*:\\s*true").containsMatchIn(limpo)
        }
    }
}
