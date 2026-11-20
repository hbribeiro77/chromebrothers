package com.chromebrothers.navegador.regras

data class MensagemDeChat(
    val papel: String,
    val conteudo: String = "",
    val partes: List<ParteDeConteudoDoChat> = emptyList(),
    val chamadas: List<ChamadaDeFerramenta> = emptyList(),
    val idDaChamada: String = "",
)

object PedidoDeChatParaAOpenRouter {
    const val URL = "https://openrouter.ai/api/v1/chat/completions"
    const val MODELO_PADRAO = "deepseek/deepseek-v4.1-flash-1017"
    const val MODELO_PADRAO_ANTIGO = "openai/gpt-4o-mini"

    fun corpo(
        modelo: String,
        mensagens: List<MensagemDeChat>,
        comFerramentas: Boolean = false,
        somenteLeitura: Boolean = false,
    ): String {
        val nome = modelo.trim().ifBlank { MODELO_PADRAO }
        val itens = mensagens.joinToString(",") { jsonDaMensagem(it) }
        val ferramentas = if (comFerramentas) {
            ""","tools":${FerramentasDeLeituraDoChatDaOpenRouter.json(somenteLeitura)}"""
        } else {
            ""
        }
        return """{"model":"${TextoJsonDoChat.escapar(nome)}","messages":[$itens]$ferramentas,"stream":true,"usage":{"include":true}}"""
    }

    fun jsonDaMensagem(mensagem: MensagemDeChat): String {
        val papel = TextoJsonDoChat.escapar(mensagem.papel)
        if (mensagem.papel == "tool") {
            return """{"role":"tool","tool_call_id":"${TextoJsonDoChat.escapar(mensagem.idDaChamada)}","content":"${TextoJsonDoChat.escapar(mensagem.conteudo)}"}"""
        }
        if (mensagem.chamadas.isNotEmpty()) {
            val calls = mensagem.chamadas.joinToString(",") { chamada ->
                val args = chamada.argumentos.ifBlank { "{}" }
                """{"id":"${TextoJsonDoChat.escapar(chamada.id)}","type":"function","function":{"name":"${TextoJsonDoChat.escapar(chamada.nome)}","arguments":"${TextoJsonDoChat.escapar(args)}"}}"""
            }
            val content = if (mensagem.conteudo.isBlank()) "null" else "\"${TextoJsonDoChat.escapar(mensagem.conteudo)}\""
            return """{"role":"$papel","content":$content,"tool_calls":[$calls]}"""
        }
        if (mensagem.partes.isNotEmpty()) {
            val partes = buildList {
                if (mensagem.conteudo.isNotBlank()) add(jsonDeTexto(mensagem.conteudo))
                mensagem.partes.forEach { add(jsonDaParte(it)) }
            }.joinToString(",")
            return """{"role":"$papel","content":[$partes]}"""
        }
        return """{"role":"$papel","content":"${TextoJsonDoChat.escapar(mensagem.conteudo)}"}"""
    }

    private fun jsonDeTexto(texto: String): String {
        return """{"type":"text","text":"${TextoJsonDoChat.escapar(texto)}"}"""
    }

    private fun jsonDaParte(parte: ParteDeConteudoDoChat): String {
        return when (parte.tipo) {
            "imagem" -> """{"type":"image_url","image_url":{"url":"data:${parte.mime};base64,${parte.dados}"}}"""
            "arquivo" -> """{"type":"file","file":{"filename":"${TextoJsonDoChat.escapar(parte.nome)}","file_data":"data:${parte.mime};base64,${parte.dados}"}}"""
            else -> jsonDeTexto(parte.texto)
        }
    }

    fun cabecalhos(chave: String): Map<String, String> {
        return mapOf(
            "Authorization" to "Bearer ${chave.trim()}",
            "Content-Type" to "application/json",
            "X-Title" to "ChromeBrothers",
        )
    }
}
