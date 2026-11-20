package com.chromebrothers.navegador.regras

data class VisitaDePaginaNoNavegador(
    val titulo: String,
    val endereco: String,
)

object HistoricoDePaginasVisitadasNoNavegador {
    const val LIMITE = 80

    fun lembrar(
        visitas: List<VisitaDePaginaNoNavegador>,
        titulo: String,
        endereco: String,
    ): List<VisitaDePaginaNoNavegador> {
        val url = endereco.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return visitas
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return visitas
        val nome = titulo.trim().ifBlank { url }
        val nova = VisitaDePaginaNoNavegador(nome, url)
        return (listOf(nova) + visitas.filter { it.endereco != url }).take(LIMITE)
    }

    fun esquecer(
        visitas: List<VisitaDePaginaNoNavegador>,
        endereco: String,
    ): List<VisitaDePaginaNoNavegador> {
        val url = endereco.trim()
        return visitas.filter { it.endereco != url }
    }
}

object TextoDoHistoricoDePaginasVisitadas {
    fun gravar(visitas: List<VisitaDePaginaNoNavegador>): String {
        return visitas.joinToString("\n") { "${escapar(it.endereco)}\t${escapar(it.titulo)}" }
    }

    fun ler(bruto: String): List<VisitaDePaginaNoNavegador> {
        if (bruto.isBlank()) return emptyList()
        return bruto.lineSequence().mapNotNull { linha ->
            val tab = linha.indexOf('\t')
            if (tab <= 0) return@mapNotNull null
            VisitaDePaginaNoNavegador(
                titulo = desescapar(linha.substring(tab + 1)),
                endereco = desescapar(linha.substring(0, tab)),
            )
        }.toList()
    }

    private fun escapar(texto: String) = buildString {
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(caractere)
            }
        }
    }

    private fun desescapar(texto: String) = buildString {
        var indice = 0
        while (indice < texto.length) {
            val caractere = texto[indice]
            if (caractere == '\\' && indice + 1 < texto.length) {
                when (texto[indice + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    't' -> append('\t')
                    '\\' -> append('\\')
                    else -> append(texto[indice + 1])
                }
                indice += 2
            } else {
                append(caractere)
                indice += 1
            }
        }
    }
}
