package com.chromebrothers.navegador.regras

sealed class TrechoDaRespostaEmFluxo {
    data class Texto(val conteudo: String) : TrechoDaRespostaEmFluxo()
    data object Fim : TrechoDaRespostaEmFluxo()
    data class Falha(val mensagem: String) : TrechoDaRespostaEmFluxo()
}

data class LeituraDoFluxoDoChat(
    val texto: String? = null,
    val chamadas: List<PedacoDeChamadaDeFerramenta> = emptyList(),
    val fim: Boolean = false,
    val falha: String? = null,
    val uso: UsoDoChat? = null,
)

object TrechosDaRespostaEmFluxoDaOpenRouter {
    fun interpretarLinha(linha: String): TrechoDaRespostaEmFluxo? {
        val texto = linha.trim()
        if (texto.isEmpty() || texto.startsWith(":")) return null
        if (!texto.startsWith("data:")) return null
        return interpretarDados(texto.removePrefix("data:").trim())
    }

    fun interpretarDados(dados: String): TrechoDaRespostaEmFluxo? {
        if (dados == "[DONE]") return TrechoDaRespostaEmFluxo.Fim
        val temEscolha = dados.contains("\"choices\"")
        if (!temEscolha && dados.contains("\"error\"")) {
            val mensagem = TextoJsonDoChat.valorDaChave(dados, "message") ?: "A OpenRouter recusou o pedido."
            return TrechoDaRespostaEmFluxo.Falha(mensagem)
        }
        val conteudo = TextoJsonDoChat.valorDaChave(dados, "content") ?: return null
        return TrechoDaRespostaEmFluxo.Texto(conteudo)
    }

    fun lerLinha(linha: String): LeituraDoFluxoDoChat? {
        val texto = linha.trim()
        if (texto.isEmpty() || texto.startsWith(":")) return null
        if (!texto.startsWith("data:")) return null
        return lerDados(texto.removePrefix("data:").trim())
    }

    fun lerDados(dados: String): LeituraDoFluxoDoChat {
        if (dados == "[DONE]") return LeituraDoFluxoDoChat(fim = true)
        val temEscolha = dados.contains("\"choices\"")
        if (!temEscolha && dados.contains("\"error\"")) {
            val mensagem = TextoJsonDoChat.valorDaChave(dados, "message") ?: "A OpenRouter recusou o pedido."
            return LeituraDoFluxoDoChat(falha = mensagem)
        }
        val chamadas = pedacosDeChamada(dados)
        val conteudo = textoDeConteudo(dados)
        val uso = if (dados.contains("\"usage\"")) UsoDoChat.doJson(dados) else null
        return LeituraDoFluxoDoChat(texto = conteudo, chamadas = chamadas, uso = uso)
    }

    private fun textoDeConteudo(dados: String): String? {
        val marca = "\"content\""
        var indice = 0
        while (indice < dados.length) {
            val achou = dados.indexOf(marca, indice)
            if (achou < 0) return null
            var cursor = achou + marca.length
            while (cursor < dados.length && dados[cursor].isWhitespace()) cursor++
            if (cursor >= dados.length || dados[cursor] != ':') {
                indice = achou + 1
                continue
            }
            cursor++
            while (cursor < dados.length && dados[cursor].isWhitespace()) cursor++
            if (dados.startsWith("null", cursor)) {
                indice = achou + 1
                continue
            }
            if (cursor >= dados.length || dados[cursor] != '"') {
                indice = achou + 1
                continue
            }
            return TextoJsonDoChat.textoNaAspa(dados, cursor)?.first
        }
        return null
    }

    private fun pedacosDeChamada(dados: String): List<PedacoDeChamadaDeFerramenta> {
        val marca = "\"tool_calls\""
        val achou = dados.indexOf(marca)
        if (achou < 0) return emptyList()
        var cursor = achou + marca.length
        while (cursor < dados.length && dados[cursor].isWhitespace()) cursor++
        if (cursor >= dados.length || dados[cursor] != ':') return emptyList()
        cursor++
        while (cursor < dados.length && dados[cursor].isWhitespace()) cursor++
        if (cursor >= dados.length || dados[cursor] != '[') return emptyList()
        cursor++
        val pedacos = mutableListOf<PedacoDeChamadaDeFerramenta>()
        var profundidade = 0
        while (cursor < dados.length) {
            when (dados[cursor]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(dados, cursor) ?: return pedacos
                    cursor = lido.second
                }
                '{' -> {
                    if (profundidade == 0) {
                        val fim = fimDoObjeto(dados, cursor)
                        pedacos += pedacoDoObjeto(dados.substring(cursor, fim))
                        cursor = fim
                    } else {
                        profundidade++
                        cursor++
                    }
                }
                '[' -> {
                    profundidade++
                    cursor++
                }
                ']' -> {
                    if (profundidade == 0) return pedacos
                    profundidade--
                    cursor++
                }
                else -> cursor++
            }
        }
        return pedacos
    }

    private fun pedacoDoObjeto(objeto: String): PedacoDeChamadaDeFerramenta {
        return PedacoDeChamadaDeFerramenta(
            indice = numeroDaChave(objeto, "index"),
            id = TextoJsonDoChat.valorDaChave(objeto, "id").orEmpty(),
            nome = TextoJsonDoChat.valorDaChave(objeto, "name").orEmpty(),
            argumentos = TextoJsonDoChat.valorDaChave(objeto, "arguments").orEmpty(),
        )
    }

    private fun numeroDaChave(objeto: String, chave: String): Int {
        val marca = "\"$chave\""
        val achou = objeto.indexOf(marca)
        if (achou < 0) return 0
        var cursor = achou + marca.length
        while (cursor < objeto.length && (objeto[cursor].isWhitespace() || objeto[cursor] == ':')) cursor++
        val inicio = cursor
        if (cursor < objeto.length && objeto[cursor] == '-') cursor++
        while (cursor < objeto.length && objeto[cursor].isDigit()) cursor++
        return objeto.substring(inicio, cursor).toIntOrNull() ?: 0
    }

    private fun fimDoObjeto(json: String, inicio: Int): Int {
        var profundidade = 0
        var indice = inicio
        while (indice < json.length) {
            when (json[indice]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(json, indice) ?: return json.length
                    indice = lido.second
                }
                '{' -> {
                    profundidade++
                    indice++
                }
                '}' -> {
                    profundidade--
                    indice++
                    if (profundidade == 0) return indice
                }
                else -> indice++
            }
        }
        return json.length
    }
}
