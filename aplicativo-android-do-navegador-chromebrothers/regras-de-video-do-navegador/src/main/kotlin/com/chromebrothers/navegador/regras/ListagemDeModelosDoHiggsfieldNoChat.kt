package com.chromebrothers.navegador.regras

object ListagemDeModelosDoHiggsfieldNoChat {
    const val LIMITE_DO_FILTRO = 80
    const val LIMITE_DO_TEXTO = 12_000

    fun filtroDosArgumentos(argumentos: String): String {
        return TextoJsonDoChat.valorDaChave(argumentos, "filtro")?.trim().orEmpty()
    }

    fun mensagemSeFiltroInvalido(argumentos: String): String? {
        val filtro = filtroDosArgumentos(argumentos)
        return if (filtro.length > LIMITE_DO_FILTRO) "O filtro passou do limite." else null
    }

    fun itensParaSelect(bruto: String): List<Pair<String, String>> {
        return extrairItens(bruto).map { Pair(it.id, it.name) }
    }

    fun jsonDosItensParaPonte(bruto: String, idPreferido: String = ""): String {
        var pares = itensParaSelect(bruto)
        val pref = idPreferido.trim()
        if (pref.isNotEmpty()) {
            val comPref = pares.filter { it.first == pref }
            val resto = pares.filter { it.first != pref }
            pares = if (comPref.isNotEmpty()) {
                comPref + resto
            } else {
                listOf(pref to rotuloAmigavelDoId(pref)) + resto
            }
        }
        val itens = pares.joinToString(",") { (id, nome) ->
            """{"id":"${TextoJsonDoChat.escapar(id)}","nome":"${TextoJsonDoChat.escapar(nome)}"}"""
        }
        return """{"ok":true,"itens":[$itens]}"""
    }

    /** Cursor `after` da próxima página do models_explore, ou null se acabou. */
    fun cursorDaProximaPagina(bruto: String): String? {
        if (bruto.isBlank()) return null
        val hasMore = TextoJsonDoChat.valorDaChave(bruto, "has_more")
            ?.equals("true", ignoreCase = true) == true ||
            bruto.contains("\"has_more\":true") ||
            bruto.contains("\"has_more\": true")
        if (!hasMore) return null
        return TextoJsonDoChat.valorDaChave(bruto, "next_page_token")?.trim()?.takeIf { it.isNotEmpty() }
    }

    /** Une várias páginas do models_explore num único `{"items":[…]}` sem ids duplicados. */
    fun mesclarPaginasDeCatalogo(paginas: List<String>): String {
        val vistos = linkedSetOf<String>()
        val itens = mutableListOf<ItemModelo>()
        for (pagina in paginas) {
            for (item in extrairItens(pagina)) {
                if (vistos.add(item.id)) itens += item
            }
        }
        val corpo = itens.joinToString(",") { item ->
            val nome = TextoJsonDoChat.escapar(item.name)
            val desc = TextoJsonDoChat.escapar(item.description)
            val tipo = TextoJsonDoChat.escapar(item.outputType)
            """{"id":"${TextoJsonDoChat.escapar(item.id)}","name":"$nome","description":"$desc","output_type":"$tipo"}"""
        }
        return """{"items":[$corpo]}"""
    }

    private fun rotuloAmigavelDoId(id: String): String =
        when (id) {
            "wan3_0" -> "Wan 3.0"
            "wan3_0_prime" -> "Wan 3.0 Prime"
            "wan2_7" -> "Wan 2.7"
            else -> id
        }

    fun formatarCatalogo(bruto: String, filtro: String): String {
        val itens = extrairItens(bruto)
        if (itens.isNotEmpty()) {
            val chave = normalizar(filtro)
            val filtrados = if (
                chave.isEmpty() || chave == "image" || chave == "imagem"
            ) {
                itens.filter { it.outputType.isEmpty() || it.outputType == "image" }
            } else {
                itens.filter {
                    normalizar("${it.id} ${it.name} ${it.description} ${it.outputType}").contains(chave)
                }
            }
            if (filtrados.isEmpty()) {
                return "Nenhum modelo do Higgsfield bate com \"$filtro\"."
            }
            val cabeca = if (filtro.isBlank() || filtro.equals("image", true) || filtro.equals("imagem", true)) {
                "Modelos de imagem Higgsfield (${filtrados.size}):"
            } else {
                "Modelos Higgsfield com \"$filtro\" (${filtrados.size}):"
            }
            val linhas = filtrados.map { item ->
                buildString {
                    append(item.id)
                    if (item.name.isNotBlank()) append(" — ").append(item.name)
                    if (item.description.isNotBlank()) append(" — ").append(item.description.take(120))
                }
            }
            return truncar(cabeca + "\n" + linhas.joinToString("\n"))
        }
        val linhas = bruto.lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .toList()
        if (linhas.isEmpty()) {
            val limpo = bruto.trim()
            if (limpo.isEmpty()) return "Nenhum modelo devolvido pelo Higgsfield."
            return truncar(filtrarBloco(limpo, filtro))
        }
        val chave = normalizar(filtro)
        val escolhidas = if (chave.isEmpty() || chave == "image" || chave == "imagem") {
            linhas
        } else {
            linhas.filter { normalizar(it).contains(chave) }
        }
        if (escolhidas.isEmpty()) {
            return "Nenhum modelo do Higgsfield bate com \"$filtro\"."
        }
        val cabeca = if (filtro.isBlank()) {
            "Modelos Higgsfield (${escolhidas.size}):"
        } else {
            "Modelos Higgsfield com \"$filtro\" (${escolhidas.size}):"
        }
        return truncar(cabeca + "\n" + escolhidas.joinToString("\n"))
    }

    private data class ItemModelo(
        val id: String,
        val name: String,
        val description: String,
        val outputType: String,
    )

    /**
     * Extrai objetos do array `items` do models_explore sem depender de org.json
     * (módulo regras é JVM puro).
     */
    private fun extrairItens(bruto: String): List<ItemModelo> {
        val texto = bruto.replace("\\/", "/")
        val marcaItems = "\"items\""
        val posItems = texto.indexOf(marcaItems)
        if (posItems < 0) return emptyList()
        val colchete = texto.indexOf('[', posItems + marcaItems.length)
        if (colchete < 0) return emptyList()
        val fimArray = fecharArray(texto, colchete) ?: return emptyList()
        val corpo = texto.substring(colchete + 1, fimArray)
        val objetos = fatiarObjetosDoArray(corpo)
        return objetos.mapNotNull { obj ->
            val id = TextoJsonDoChat.valorDaChave(obj, "id")?.trim().orEmpty()
            if (id.isEmpty()) return@mapNotNull null
            ItemModelo(
                id = id,
                name = TextoJsonDoChat.valorDaChave(obj, "name")?.trim().orEmpty(),
                description = TextoJsonDoChat.valorDaChave(obj, "description")?.trim().orEmpty(),
                outputType = TextoJsonDoChat.valorDaChave(obj, "output_type")?.trim()?.lowercase().orEmpty(),
            )
        }
    }

    private fun fecharArray(texto: String, indiceAbre: Int): Int? {
        var nivel = 0
        var i = indiceAbre
        var emString = false
        var escapado = false
        while (i < texto.length) {
            val c = texto[i]
            if (emString) {
                when {
                    escapado -> escapado = false
                    c == '\\' -> escapado = true
                    c == '"' -> emString = false
                }
            } else {
                when (c) {
                    '"' -> emString = true
                    '[' -> nivel++
                    ']' -> {
                        nivel--
                        if (nivel == 0) return i
                    }
                }
            }
            i++
        }
        return null
    }

    private fun fatiarObjetosDoArray(corpo: String): List<String> {
        val out = mutableListOf<String>()
        var i = 0
        while (i < corpo.length) {
            while (i < corpo.length && (corpo[i].isWhitespace() || corpo[i] == ',')) i++
            if (i >= corpo.length) break
            if (corpo[i] != '{') {
                i++
                continue
            }
            val fim = fecharObjeto(corpo, i) ?: break
            out += corpo.substring(i, fim + 1)
            i = fim + 1
        }
        return out
    }

    private fun fecharObjeto(texto: String, indiceAbre: Int): Int? {
        var nivel = 0
        var i = indiceAbre
        var emString = false
        var escapado = false
        while (i < texto.length) {
            val c = texto[i]
            if (emString) {
                when {
                    escapado -> escapado = false
                    c == '\\' -> escapado = true
                    c == '"' -> emString = false
                }
            } else {
                when (c) {
                    '"' -> emString = true
                    '{' -> nivel++
                    '}' -> {
                        nivel--
                        if (nivel == 0) return i
                    }
                }
            }
            i++
        }
        return null
    }

    private fun filtrarBloco(texto: String, filtro: String): String {
        val chave = normalizar(filtro)
        if (chave.isEmpty() || chave == "image" || chave == "imagem") return texto
        if (normalizar(texto).contains(chave)) return texto
        return "Nenhum modelo do Higgsfield bate com \"$filtro\"."
    }

    private fun truncar(texto: String): String {
        if (texto.length <= LIMITE_DO_TEXTO) return texto
        return texto.take(LIMITE_DO_TEXTO) + "\n… truncado, total=${texto.length}"
    }

    fun normalizar(texto: String): String {
        return texto.lowercase()
            .replace('_', ' ')
            .replace('-', ' ')
            .replace(Regex("\\s+"), "")
    }
}
