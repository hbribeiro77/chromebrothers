package com.chromebrothers.navegador.regras

data class ModeloListadoDaOpenRouter(
    val id: String,
    val nome: String,
    val familia: String,
)

data class PaginaDoCatalogoDeModelosDaOpenRouter(
    val modelos: List<ModeloListadoDaOpenRouter>,
    val quantidadeBruta: Int,
)

object CatalogoDeModelosPorFamiliaDaOpenRouter {
    const val LIMITE_DA_PAGINA = 1000
    val familias = listOf("DeepSeek", "GPT", "Claude", "Gemini", "Grok", "GLM", "Kimi")

    fun url(offset: Int): String {
        return "https://openrouter.ai/api/v1/models" +
            "?model_authors=deepseek,openai,anthropic,google,x-ai,z-ai,moonshotai" +
            "&output_modalities=text" +
            "&sort=newest" +
            "&limit=$LIMITE_DA_PAGINA" +
            "&offset=$offset"
    }

    fun pagina(json: String): PaginaDoCatalogoDeModelosDaOpenRouter {
        val objetos = objetosDoArray(json, "data")
        val modelos = objetos.mapNotNull { fatia ->
            val id = textoDaChaveNoPrimeiroNivel(fatia, "id") ?: return@mapNotNull null
            val familia = familiaDe(id) ?: return@mapNotNull null
            val nome = textoDaChaveNoPrimeiroNivel(fatia, "name")?.ifBlank { id } ?: id
            ModeloListadoDaOpenRouter(id, nome, familia)
        }
        return PaginaDoCatalogoDeModelosDaOpenRouter(modelos, objetos.size)
    }

    fun familiaDe(id: String): String? {
        if (id.startsWith("~") || id.contains(":batch")) return null
        return when {
            id.startsWith("deepseek/") -> "DeepSeek"
            id.startsWith("openai/gpt") -> "GPT"
            id.startsWith("z-ai/glm") -> "GLM"
            id.startsWith("moonshotai/kimi") -> "Kimi"
            id.startsWith("google/gemini") -> "Gemini"
            id.startsWith("anthropic/claude") -> "Claude"
            id.startsWith("x-ai/grok") -> "Grok"
            else -> null
        }
    }

    private fun objetosDoArray(json: String, nome: String): List<String> {
        val inicio = indiceDoArray(json, nome)
        if (inicio < 0) return emptyList()
        val saida = mutableListOf<String>()
        var indice = inicio
        var profundidade = 0
        while (indice < json.length) {
            when (json[indice]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(json, indice) ?: return saida
                    indice = lido.second
                }
                '{' -> {
                    if (profundidade == 0) {
                        val fim = fimDoAgrupado(json, indice)
                        saida += json.substring(indice, fim)
                        indice = fim
                    } else {
                        profundidade++
                        indice++
                    }
                }
                '[' -> {
                    profundidade++
                    indice++
                }
                ']' -> {
                    if (profundidade == 0) return saida
                    profundidade--
                    indice++
                }
                else -> indice++
            }
        }
        return saida
    }

    private fun indiceDoArray(json: String, nome: String): Int {
        var indice = 0
        var profundidade = 0
        while (indice < json.length) {
            when (json[indice]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(json, indice) ?: return -1
                    if (profundidade == 1 && lido.first == nome) {
                        var cursor = lido.second
                        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
                        if (cursor < json.length && json[cursor] == ':') {
                            cursor++
                            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
                            if (cursor < json.length && json[cursor] == '[') return cursor + 1
                        }
                    }
                    indice = lido.second
                }
                '{', '[' -> {
                    profundidade++
                    indice++
                }
                '}', ']' -> {
                    profundidade--
                    indice++
                }
                else -> indice++
            }
        }
        return -1
    }

    private fun fimDoAgrupado(json: String, inicio: Int): Int {
        val abre = json[inicio]
        val fecha = if (abre == '{') '}' else ']'
        var profundidade = 0
        var indice = inicio
        while (indice < json.length) {
            when (json[indice]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(json, indice) ?: return json.length
                    indice = lido.second
                }
                abre -> {
                    profundidade++
                    indice++
                }
                fecha -> {
                    profundidade--
                    indice++
                    if (profundidade == 0) return indice
                }
                else -> indice++
            }
        }
        return json.length
    }

    private fun textoDaChaveNoPrimeiroNivel(objeto: String, chave: String): String? {
        var indice = 0
        var profundidade = 0
        while (indice < objeto.length) {
            when (objeto[indice]) {
                '"' -> {
                    val lido = TextoJsonDoChat.textoNaAspa(objeto, indice) ?: return null
                    if (profundidade == 1 && lido.first == chave) {
                        var cursor = lido.second
                        while (cursor < objeto.length && objeto[cursor].isWhitespace()) cursor++
                        if (cursor < objeto.length && objeto[cursor] == ':') {
                            cursor++
                            while (cursor < objeto.length && objeto[cursor].isWhitespace()) cursor++
                            if (cursor < objeto.length && objeto[cursor] == '"') {
                                return TextoJsonDoChat.textoNaAspa(objeto, cursor)?.first
                            }
                        }
                    }
                    indice = lido.second
                }
                '{', '[' -> {
                    profundidade++
                    indice++
                }
                '}', ']' -> {
                    profundidade--
                    indice++
                }
                else -> indice++
            }
        }
        return null
    }
}
