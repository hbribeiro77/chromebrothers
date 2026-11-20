package com.chromebrothers.navegador.regras

object GeracaoDeVideoPeloTopviewNoChat {
    const val SEM_SESSAO = "Ligue o Topview na engrenagem do chat."
    const val MODELO_PADRAO = PedidoDeGeracaoDeVideoViaMcpNaColecao.MODELO_PADRAO_DA_COLECAO_TOPVIEW
    const val LIMITE_DO_PROMPT = 20_000

    fun resolverModelo(modeloIdOuNome: String): String =
        CatalogoDeModelosDeVideoDoTopviewNaColecao.displayNameDe(
            modeloIdOuNome.ifBlank { MODELO_PADRAO },
        )

    fun lerVideo(argumentos: String): ResultadoDaGeracaoTopviewDoChat {
        val prompt = TextoJsonDoChat.valorDaChave(argumentos, "prompt")?.trim().orEmpty()
        val modelo = TextoJsonDoChat.valorDaChave(argumentos, "modelo")?.trim().orEmpty()
        return when {
            prompt.isEmpty() -> ResultadoDaGeracaoTopviewDoChat.Recusado("Faltou o prompt.")
            prompt.length > LIMITE_DO_PROMPT ->
                ResultadoDaGeracaoTopviewDoChat.Recusado("O prompt passou do limite.")
            else -> ResultadoDaGeracaoTopviewDoChat.Pronto(
                prompt = prompt,
                modelo = modelo.ifBlank { MODELO_PADRAO },
            )
        }
    }

    fun perguntaVideo(prompt: String, modelo: String): String {
        val m = resolverModelo(modelo)
        return "Gerar vídeo no Topview?\nModelo: $m\n${prompt.take(160)}"
    }
}

sealed class ResultadoDaGeracaoTopviewDoChat {
    data class Pronto(val prompt: String, val modelo: String) : ResultadoDaGeracaoTopviewDoChat()
    data class Recusado(val mensagem: String) : ResultadoDaGeracaoTopviewDoChat()
}
