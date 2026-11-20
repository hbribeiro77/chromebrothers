package com.chromebrothers.navegador.regras

object CompactacaoDaConversaDoChatDaOpenRouter {
    const val ULTIMAS_TROCAS_USER_ASSISTANT = 2

    fun promptParaResumir(mensagens: List<MensagemDeChat>): List<MensagemDeChat> {
        val historico = mensagens
            .filter { it.papel == "user" || it.papel == "assistant" }
            .joinToString("\n") { mensagem ->
                "${mensagem.papel}: ${textoVisivelParaOResumo(mensagem)}"
            }
        return listOf(
            MensagemDeChat(
                papel = "system",
                conteudo = "Você vai resumir a conversa de forma fiel, em português do Brasil. " +
                    "Não invente fatos. Preserve nomes, decisões, números e pedidos. " +
                    "Anexos aparecem só como [imagem] ou [arquivo], sem o conteúdo binário. " +
                    "Responda apenas com o resumo.",
            ),
            MensagemDeChat(
                papel = "user",
                conteudo = "resumir o histórico abaixo em português:\n$historico",
            ),
        )
    }

    fun aplicar(resumo: String, mensagensOriginais: List<MensagemDeChat>): List<MensagemDeChat> {
        return listOf(MensagemDeChat("assistant", resumo.trim())) + ultimasTrocas(mensagensOriginais)
    }

    private fun ultimasTrocas(mensagens: List<MensagemDeChat>): List<MensagemDeChat> {
        val visiveis = mensagens.filter { it.papel == "user" || it.papel == "assistant" }
        val escolhidas = ArrayDeque<MensagemDeChat>()
        var indice = visiveis.lastIndex
        var trocas = 0
        while (indice >= 0 && trocas < ULTIMAS_TROCAS_USER_ASSISTANT) {
            val atual = visiveis[indice]
            if (atual.papel == "assistant" && indice > 0 && visiveis[indice - 1].papel == "user") {
                escolhidas.addFirst(soOTexto(atual))
                escolhidas.addFirst(soOTexto(visiveis[indice - 1]))
                indice -= 2
            } else {
                escolhidas.addFirst(soOTexto(atual))
                indice -= 1
            }
            trocas += 1
        }
        return escolhidas.toList()
    }

    private fun soOTexto(mensagem: MensagemDeChat): MensagemDeChat {
        return MensagemDeChat(papel = mensagem.papel, conteudo = mensagem.conteudo)
    }

    private fun textoVisivelParaOResumo(mensagem: MensagemDeChat): String {
        val pedacos = buildList {
            if (mensagem.conteudo.isNotBlank()) add(mensagem.conteudo)
            mensagem.partes.forEach { parte ->
                when (parte.tipo) {
                    "imagem" -> add("[imagem]")
                    "arquivo" -> add("[arquivo]")
                    else -> if (parte.texto.isNotBlank()) add(parte.texto)
                }
            }
        }
        return pedacos.joinToString(" ")
    }
}
