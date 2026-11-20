package com.chromebrothers.navegador.regras

/**
 * Pedido focado (sem tools) para a IA escolher como identificar o elemento no gravador.
 * Resposta esperada: JSON {"indice":N} ou {"seletor":"...","texto":"..."}.
 */
object EscolhaDeCandidataPelaIaDoGravadorDeRoteiro {
    const val LIMITE_DO_TRECHO_DOM = 2500
    const val LIMITE_DA_INTENCAO = 200

    fun mensagemDoSistema(): String =
        """
        Você ajuda a gravar automação de página. O usuário descreve o elemento desejado.
        Você recebe candidatas numeradas (0..n-1) e um trecho do DOM perto do alvo atual.
        Responda SOMENTE com um JSON válido, sem markdown:
        {"indice":N} — N é o índice da melhor candidata da lista; ou
        {"seletor":"css","texto":"opcional"} — se nenhuma candidata servir, um seletor CSS (e texto opcional para filtrar).
        Prefira candidata marcada como única. Não invente seletores frágeis com nth-of-type se houver id/name/data-testid.
        """.trimIndent()

    fun mensagemDoUsuario(
        intencao: String,
        url: String,
        marca: MarcaDoElementoParaORoteiroDeAutomacao,
        candidatas: List<CandidataDoElementoParaORoteiro>,
        trechoDom: String,
    ): String {
        val lista = candidatas.mapIndexed { i, c ->
            val como = c.seletor.ifBlank { "texto: ${c.texto}" }
            val uni = when (c.matches) {
                1 -> " única"
                null -> ""
                else -> " ${c.matches} matches"
            }
            "$i) ${c.rotulo.substringBefore(" · ")} → $como$uni"
        }.joinToString("\n")
        val alvo = buildString {
            append("<${marca.tag.ifBlank { "?" }}>")
            if (marca.id.isNotEmpty()) append(" id=${marca.id}")
            if (marca.texto.isNotEmpty()) append(" texto=«${marca.texto.take(40)}»")
            if (marca.ariaLabel.isNotEmpty()) append(" aria=«${marca.ariaLabel.take(40)}»")
        }
        return buildString {
            appendLine("Intenção do usuário: ${intencao.trim().take(LIMITE_DA_INTENCAO)}")
            appendLine("URL: ${url.take(300)}")
            appendLine("Alvo atual do pick: $alvo")
            appendLine("Candidatas:")
            appendLine(lista.ifBlank { "(nenhuma)" })
            appendLine("Trecho DOM:")
            append(trechoDom.take(LIMITE_DO_TRECHO_DOM).ifBlank { "(vazio)" })
        }
    }

    fun mensagens(
        intencao: String,
        url: String,
        marca: MarcaDoElementoParaORoteiroDeAutomacao,
        candidatas: List<CandidataDoElementoParaORoteiro>,
        trechoDom: String,
    ): List<MensagemDeChat> = listOf(
        MensagemDeChat("system", mensagemDoSistema()),
        MensagemDeChat(
            "user",
            mensagemDoUsuario(intencao, url, marca, candidatas, trechoDom),
        ),
    )

    fun interpretar(
        resposta: String,
        candidatas: List<CandidataDoElementoParaORoteiro>,
    ): ResultadoDaEscolhaDeCandidataPelaIa {
        val json = extrairJson(resposta)
            ?: return ResultadoDaEscolhaDeCandidataPelaIa.Erro("A IA não devolveu JSON.")
        val indice = TextoJsonDoChat.inteiroDaChave(json, "indice")
        if (indice != null) {
            if (indice !in candidatas.indices) {
                return ResultadoDaEscolhaDeCandidataPelaIa.Erro("Índice $indice fora da lista.")
            }
            return ResultadoDaEscolhaDeCandidataPelaIa.Candidata(candidatas[indice])
        }
        val seletor = TextoJsonDoChat.valorDaChave(json, "seletor")?.trim().orEmpty()
        val texto = TextoJsonDoChat.valorDaChave(json, "texto")?.trim().orEmpty()
        if (seletor.isEmpty() && texto.isEmpty()) {
            return ResultadoDaEscolhaDeCandidataPelaIa.Erro("JSON sem indice nem seletor.")
        }
        if (seletor.length > RoteiroDeAutomacaoDaPaginaDoNavegador.LIMITE_DO_SELETOR) {
            return ResultadoDaEscolhaDeCandidataPelaIa.Erro("Seletor longo demais.")
        }
        return ResultadoDaEscolhaDeCandidataPelaIa.Candidata(
            CandidataDoElementoParaORoteiro(
                rotulo = "IA · ${seletor.ifBlank { "texto" }.take(40)}",
                seletor = seletor,
                texto = texto.take(RoteiroDeAutomacaoDaPaginaDoNavegador.LIMITE_DO_TEXTO),
            ),
        )
    }

    private fun extrairJson(resposta: String): String? {
        val bruto = resposta.trim()
        if (bruto.isEmpty()) return null
        val semCerca = bruto
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val inicio = semCerca.indexOf('{')
        val fim = semCerca.lastIndexOf('}')
        if (inicio < 0 || fim <= inicio) return null
        return semCerca.substring(inicio, fim + 1)
    }
}

sealed class ResultadoDaEscolhaDeCandidataPelaIa {
    data class Candidata(val candidata: CandidataDoElementoParaORoteiro) : ResultadoDaEscolhaDeCandidataPelaIa()
    data class Erro(val mensagem: String) : ResultadoDaEscolhaDeCandidataPelaIa()
}
