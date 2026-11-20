package com.chromebrothers.navegador.regras

/**
 * Mensagens que o usuário manda enquanto o modelo ainda trabalha: não interrompem, entram
 * no próximo passo (antes da próxima chamada ao modelo) como orientação.
 */
object OrientacaoDuranteARespostaDoChat {
    const val PREFIXO =
        "[Mensagem do usuário enviada enquanto você trabalhava. Leve em conta a partir de agora; " +
            "ajuste o que estiver fazendo se for o caso e continue.]"

    fun mensagem(textos: List<String>): MensagemDeChat? {
        val limpos = textos.map { it.trim() }.filter { it.isNotEmpty() }
        if (limpos.isEmpty()) return null
        return MensagemDeChat("user", "$PREFIXO\n" + limpos.joinToString("\n\n"))
    }
}

/**
 * Ao parar no meio, a última mensagem do modelo pode ter chamadas de ferramenta sem resultado.
 * A API recusa esse histórico na próxima pergunta; aqui cada chamada pendente ganha um
 * resultado "Interrompido pelo usuário.".
 */
object FechamentoDasChamadasPendentesDoChat {
    const val RESULTADO = "Interrompido pelo usuário."

    fun fechar(historico: List<MensagemDeChat>): List<MensagemDeChat> {
        val indice = historico.indexOfLast { it.papel == "assistant" && it.chamadas.isNotEmpty() }
        if (indice < 0) return historico
        val respondidas = historico.drop(indice + 1).filter { it.papel == "tool" }.map { it.idDaChamada }.toSet()
        val pendentes = historico[indice].chamadas.filter { it.id !in respondidas }
        if (pendentes.isEmpty()) return historico
        return historico + pendentes.map { MensagemDeChat(papel = "tool", conteudo = RESULTADO, idDaChamada = it.id) }
    }
}
