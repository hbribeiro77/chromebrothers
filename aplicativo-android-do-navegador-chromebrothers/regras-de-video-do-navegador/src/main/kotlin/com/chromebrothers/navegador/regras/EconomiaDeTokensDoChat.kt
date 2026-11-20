package com.chromebrothers.navegador.regras

import java.util.Locale

/** Uso de uma chamada (ou a soma de uma resposta inteira), como a OpenRouter devolve em `usage`. */
data class UsoDoChat(
    val tokensDeEntrada: Int = 0,
    val tokensEmCache: Int = 0,
    val tokensDeSaida: Int = 0,
    val custo: Double? = null,
    val chamadas: Int = 1,
) {
    operator fun plus(outro: UsoDoChat): UsoDoChat = UsoDoChat(
        tokensDeEntrada = tokensDeEntrada + outro.tokensDeEntrada,
        tokensEmCache = tokensEmCache + outro.tokensEmCache,
        tokensDeSaida = tokensDeSaida + outro.tokensDeSaida,
        custo = if (custo == null && outro.custo == null) null else (custo ?: 0.0) + (outro.custo ?: 0.0),
        chamadas = chamadas + outro.chamadas,
    )

    /** Ex.: "3 chamadas · 24,1 mil tokens (18,0 mil em cache) · US$ 0,0031" */
    fun linha(): String {
        val partes = mutableListOf<String>()
        if (chamadas > 1) partes += "$chamadas chamadas"
        val total = tokensDeEntrada + tokensDeSaida
        partes += if (tokensEmCache > 0) {
            "${quantidade(total)} tokens (${quantidade(tokensEmCache)} em cache)"
        } else {
            "${quantidade(total)} tokens"
        }
        custo?.let { partes += "US$ " + String.format(Locale("pt", "BR"), "%.4f", it) }
        return partes.joinToString(" · ")
    }

    private fun quantidade(n: Int): String =
        if (n < 1000) "$n" else String.format(Locale("pt", "BR"), "%.1f mil", n / 1000.0)

    companion object {
        /** Lê o objeto `usage` de um pedaço do fluxo; null se não houver. */
        fun doJson(dados: String): UsoDoChat? {
            val uso = TextoJsonDoChat.objetoDaChave(dados, "usage") ?: return null
            val entrada = TextoJsonDoChat.inteiroDaChave(uso, "prompt_tokens") ?: return null
            return UsoDoChat(
                tokensDeEntrada = entrada,
                tokensEmCache = TextoJsonDoChat.inteiroDaChave(uso, "cached_tokens") ?: 0,
                tokensDeSaida = TextoJsonDoChat.inteiroDaChave(uso, "completion_tokens") ?: 0,
                custo = TextoJsonDoChat.numeroDaChave(uso, "cost"),
            )
        }
    }
}

/**
 * Enxuga o histórico de respostas que já terminaram: resultado longo de ferramenta vira uma
 * linha curta e a imagem injetada por ferramenta (captura) sai. O texto do usuário e do modelo,
 * os anexos do usuário e as chamadas ficam, para a conversa continuar fazendo sentido.
 */
object CompactacaoDoHistoricoDoChat {
    const val LIMITE_DO_RESULTADO = 400

    fun compactar(historico: List<MensagemDeChat>): List<MensagemDeChat> {
        val nomes = historico.flatMap { it.chamadas }.associate { it.id to it.nome }
        var anterior: MensagemDeChat? = null
        return historico.map { mensagem ->
            val nova = when {
                mensagem.papel == "tool" && mensagem.conteudo.length > LIMITE_DO_RESULTADO &&
                    !mensagem.conteudo.startsWith(MARCA) -> mensagem.copy(
                    conteudo = "$MARCA ${nomes[mensagem.idDaChamada] ?: "ferramenta"}: " +
                        "${mensagem.conteudo.length} caracteres, tirados do histórico para economizar. " +
                        "Começo: ${mensagem.conteudo.take(160).replace(Regex("\\s+"), " ")}…]",
                )
                mensagem.papel == "user" && anterior?.papel == "tool" &&
                    mensagem.partes.any { it.tipo == "imagem" } -> mensagem.copy(
                    conteudo = "$MARCA imagem da captura tirada do histórico. Capture de novo se precisar.]",
                    partes = emptyList(),
                )
                else -> mensagem
            }
            anterior = mensagem
            nova
        }
    }

    private const val MARCA = "[Resultado antigo de"
}
