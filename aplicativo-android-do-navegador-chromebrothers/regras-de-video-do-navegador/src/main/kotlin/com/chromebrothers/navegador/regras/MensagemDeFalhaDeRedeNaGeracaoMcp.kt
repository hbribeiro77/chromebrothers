package com.chromebrothers.navegador.regras

/**
 * Traduz erros de rede crus ("Unable to resolve host…", timeout…) numa mensagem que diz o que fazer:
 * sem tarefa o pedido não chegou ao serviço (Tentar de novo); com tarefa a conexão caiu na espera (Verificar).
 */
object MensagemDeFalhaDeRedeNaGeracaoMcp {
    /** A rede caiu no envio: o serviço pode ter recebido o pedido mesmo sem a resposta ter voltado. */
    const val SEM_TAREFA = "A conexão falhou durante o envio (rede ou DNS); o pedido pode ter chegado ao serviço. " +
        "Toque em Verificar, se aparecer, antes de Tentar de novo, para não gerar em dobro."
    const val COM_TAREFA = "A conexão caiu enquanto esperava o resultado. O serviço pode ter terminado: toque em Verificar."

    private val sinais = listOf(
        "unable to resolve host",
        "unknownhost",
        "no address associated with hostname",
        "failed to connect",
        "connection refused",
        "connection reset",
        "connection abort",
        "network is unreachable",
        "timeout",
        "timed out",
        "sslhandshake",
        "ssl handshake",
        "unexpected end of stream",
    )

    fun ehDeRede(mensagem: String): Boolean {
        val texto = mensagem.lowercase()
        return sinais.any { it in texto }
    }

    /** Mensagem final do card; mantém o detalhe técnico no fim para diagnóstico. */
    fun amigavel(mensagem: String, temTarefa: Boolean): String {
        if (!ehDeRede(mensagem) || mensagem.startsWith(SEM_TAREFA) || mensagem.startsWith(COM_TAREFA)) return mensagem
        val base = if (temTarefa) COM_TAREFA else SEM_TAREFA
        return "$base (${mensagem.trim().take(160)})"
    }
}
