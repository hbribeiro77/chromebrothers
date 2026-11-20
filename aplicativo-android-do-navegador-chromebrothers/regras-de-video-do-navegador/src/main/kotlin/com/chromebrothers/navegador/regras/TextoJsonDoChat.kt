package com.chromebrothers.navegador.regras

object TextoJsonDoChat {
    fun escapar(texto: String): String = buildString {
        texto.forEach { caractere ->
            when (caractere) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> if (caractere.code < 0x20) {
                    append("\\u%04x".format(caractere.code))
                } else {
                    append(caractere)
                }
            }
        }
    }

    fun valorDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        var indice = 0
        while (indice < json.length) {
            val achou = json.indexOf(marca, indice)
            if (achou < 0) return null
            var cursor = achou + marca.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length || json[cursor] != ':') {
                indice = achou + 1
                continue
            }
            cursor++
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (json.startsWith("null", cursor)) return null
            if (cursor >= json.length || json[cursor] != '"') {
                indice = achou + 1
                continue
            }
            return textoNaAspa(json, cursor)?.first
        }
        return null
    }

    fun inteiroDaChave(json: String, chave: String): Int? =
        textoDoNumeroDaChave(json, chave)?.toIntOrNull()

    fun numeroDaChave(json: String, chave: String): Double? =
        textoDoNumeroDaChave(json, chave)?.toDoubleOrNull()

    private fun textoDoNumeroDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        var indice = 0
        while (indice < json.length) {
            val achou = json.indexOf(marca, indice)
            if (achou < 0) return null
            var cursor = achou + marca.length
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length || json[cursor] != ':') {
                indice = achou + 1
                continue
            }
            cursor++
            while (cursor < json.length && json[cursor].isWhitespace()) cursor++
            if (cursor >= json.length) return null
            if (json.startsWith("null", cursor)) return null
            if (json[cursor] == '"') return textoNaAspa(json, cursor)?.first
            return tokenNumerico(json, cursor)
        }
        return null
    }

    private fun tokenNumerico(json: String, inicio: Int): String? {
        var cursor = inicio
        if (cursor < json.length && json[cursor] == '-') cursor++
        if (cursor >= json.length || !json[cursor].isDigit()) return null
        while (cursor < json.length && json[cursor].isDigit()) cursor++
        if (cursor < json.length && json[cursor] == '.') {
            val depoisDoPonto = cursor + 1
            if (depoisDoPonto < json.length && json[depoisDoPonto].isDigit()) {
                cursor = depoisDoPonto
                while (cursor < json.length && json[cursor].isDigit()) cursor++
            }
        }
        return json.substring(inicio, cursor)
    }

    fun textoNaAspa(json: String, indiceDaAspa: Int): Pair<String, Int>? {
        val saida = StringBuilder()
        var indice = indiceDaAspa + 1
        while (indice < json.length) {
            val caractere = json[indice]
            if (caractere == '"') return saida.toString() to (indice + 1)
            if (caractere == '\\' && indice + 1 < json.length) {
                when (json[indice + 1]) {
                    'n' -> saida.append('\n')
                    'r' -> saida.append('\r')
                    't' -> saida.append('\t')
                    '"' -> saida.append('"')
                    '\\' -> saida.append('\\')
                    'u' -> {
                        if (indice + 5 < json.length) {
                            val hex = json.substring(indice + 2, indice + 6)
                            saida.append(hex.toIntOrNull(16)?.toChar() ?: '?')
                            indice += 6
                            continue
                        }
                    }
                    else -> saida.append(json[indice + 1])
                }
                indice += 2
                continue
            }
            saida.append(caractere)
            indice++
        }
        return null
    }

    fun objetosDaChave(json: String, chave: String): List<String>? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '[') return null
        cursor++
        val objetos = mutableListOf<String>()
        while (cursor < json.length) {
            while (cursor < json.length && (json[cursor].isWhitespace() || json[cursor] == ',')) cursor++
            if (cursor >= json.length) return null
            if (json[cursor] == ']') return objetos
            if (json[cursor] != '{') return null
            val fim = fimDoObjeto(json, cursor) ?: return null
            objetos.add(json.substring(cursor, fim))
            cursor = fim
        }
        return null
    }

    fun objetoDaChave(json: String, chave: String): String? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '{') return null
        val fim = fimDoObjeto(json, cursor) ?: return null
        return json.substring(cursor, fim)
    }

    fun textosDaChave(json: String, chave: String): List<String>? {
        val marca = "\"$chave\""
        val achou = json.indexOf(marca)
        if (achou < 0) return null
        var cursor = achou + marca.length
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != ':') return null
        cursor++
        while (cursor < json.length && json[cursor].isWhitespace()) cursor++
        if (cursor >= json.length || json[cursor] != '[') return null
        cursor++
        val textos = mutableListOf<String>()
        while (cursor < json.length) {
            while (cursor < json.length && (json[cursor].isWhitespace() || json[cursor] == ',')) cursor++
            if (cursor >= json.length) return null
            if (json[cursor] == ']') return textos
            if (json[cursor] != '"') return null
            val lido = textoNaAspa(json, cursor) ?: return null
            textos.add(lido.first)
            cursor = lido.second
        }
        return null
    }

    private fun fimDoObjeto(json: String, inicio: Int): Int? {
        var profundidade = 0
        var indice = inicio
        while (indice < json.length) {
            val caractere = json[indice]
            if (caractere == '"') {
                indice = textoNaAspa(json, indice)?.second ?: return null
                continue
            }
            if (caractere == '{') profundidade++
            if (caractere == '}') {
                profundidade--
                if (profundidade == 0) return indice + 1
            }
            indice++
        }
        return null
    }
}
