package com.chromebrothers.navegador.regras

data class AtalhoDaPaginaInicial(
    val titulo: String,
    val endereco: String,
)

enum class ResultadoDoAtalhoDaPaginaInicial {
    ADICIONADO,
    REPETIDO,
    LIMITE,
    INVALIDO,
}

class ListaDeAtalhosDaPaginaInicialDoNavegador(
    inicial: List<AtalhoDaPaginaInicial> = emptyList(),
    private val limite: Int = 8,
) {
    private val atalhos = inicial.toMutableList()

    fun listar(): List<AtalhoDaPaginaInicial> = atalhos.toList()

    fun contem(endereco: String): Boolean {
        val chave = normalizar(endereco)
        return atalhos.any { normalizar(it.endereco) == chave }
    }

    fun remover(endereco: String) {
        val chave = normalizar(endereco)
        atalhos.removeAll { normalizar(it.endereco) == chave }
    }

    fun tentarAdicionar(titulo: String, endereco: String): ResultadoDoAtalhoDaPaginaInicial {
        val url = endereco.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) return ResultadoDoAtalhoDaPaginaInicial.INVALIDO
        if (EnderecoDaPaginaInicialDoNavegadorChromebrothers.eh(url)) return ResultadoDoAtalhoDaPaginaInicial.INVALIDO
        if (contem(url)) return ResultadoDoAtalhoDaPaginaInicial.REPETIDO
        if (atalhos.size >= limite) return ResultadoDoAtalhoDaPaginaInicial.LIMITE
        val nome = titulo.trim().ifBlank { url }
        atalhos.add(AtalhoDaPaginaInicial(nome, url))
        return ResultadoDoAtalhoDaPaginaInicial.ADICIONADO
    }

    private fun normalizar(endereco: String): String = endereco.trim().trimEnd('/')
}
