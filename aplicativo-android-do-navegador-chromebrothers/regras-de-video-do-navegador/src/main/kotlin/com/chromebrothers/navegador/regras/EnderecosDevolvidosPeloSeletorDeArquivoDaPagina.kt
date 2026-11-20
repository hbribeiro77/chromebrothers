package com.chromebrothers.navegador.regras

object EnderecosDevolvidosPeloSeletorDeArquivoDaPagina {
    fun escolher(principal: String?, clipes: List<String?>, extras: List<String?>): List<String> {
        val vistos = LinkedHashSet<String>()
        fun guardar(valor: String?) {
            val texto = valor?.trim().orEmpty()
            if (texto.isNotEmpty()) vistos += texto
        }
        guardar(principal)
        clipes.forEach(::guardar)
        extras.forEach(::guardar)
        return vistos.toList()
    }
}
