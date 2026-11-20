package com.chromebrothers.navegador.regras

object PacoteDaListaDeBloqueioParaOFirefox {
    fun json(lista: ListaDeBloqueioDeAnuncio): String {
        return buildString {
            append("{\"bloqueios\":")
            append(listaJson(lista.dominiosDeBloqueio()))
            append(",\"bloqueiosDeTerceiros\":")
            append(listaJson(lista.dominiosDeBloqueioDeTerceiros()))
            append(",\"excecoes\":")
            append(listaJson(lista.dominiosDeExcecao()))
            append(",\"excecoesDeTerceiros\":")
            append(listaJson(lista.dominiosDeExcecaoDeTerceiros()))
            append("}")
        }
    }

    private fun listaJson(dominios: List<String>): String {
        return dominios.joinToString(prefix = "[", postfix = "]") { "\"${escapar(it)}\"" }
    }

    private fun escapar(valor: String): String {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"")
    }
}
