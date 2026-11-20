package com.chromebrothers.navegador.regras

object TituloDaConversaDoChatDaOpenRouter {
    fun gerar(mensagens: List<MensagemDeChat>, maximo: Int = 60): String {
        val bruto = mensagens.firstOrNull { it.papel == "user" }?.conteudo.orEmpty()
        val pergunta = bruto.substringAfter("\n\nPergunta:\n", bruto)
        val limpo = colapsarEspacos(pergunta.trim())
        if (limpo.isEmpty()) return "Conversa"
        return limpo.take(maximo)
    }

    private fun colapsarEspacos(texto: String): String = buildString {
        var espacoPendente = false
        for (caractere in texto) {
            if (caractere.isWhitespace()) {
                espacoPendente = true
            } else {
                if (espacoPendente) append(' ')
                espacoPendente = false
                append(caractere)
            }
        }
    }
}
