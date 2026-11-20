package com.chromebrothers.navegador.regras

import java.net.URLEncoder

/**
 * Sugestões de pesquisa online (as linhas com lupa do Chrome). Os três motores respondem no
 * formato OpenSearch: ["texto", ["sugestão 1", "sugestão 2", ...], ...].
 */
object SugestoesDePesquisaDoMotorDeBusca {
    const val LIMITE = 5

    fun enderecoDaConsulta(motor: MotorDeBuscaDaBarraDeEndereco, texto: String): String {
        val q = URLEncoder.encode(texto.trim(), "UTF-8")
        return when (motor) {
            MotorDeBuscaDaBarraDeEndereco.GOOGLE ->
                "https://suggestqueries.google.com/complete/search?client=firefox&hl=pt-BR&ie=UTF-8&oe=UTF-8&q=$q"
            MotorDeBuscaDaBarraDeEndereco.DUCKDUCKGO ->
                "https://duckduckgo.com/ac/?type=list&kl=br-pt&q=$q"
            MotorDeBuscaDaBarraDeEndereco.BING ->
                "https://api.bing.com/osjson.aspx?market=pt-BR&query=$q"
        }
    }

    /** Vale a pena perguntar ao motor? (não para URLs nem para texto vazio). */
    fun deveConsultar(texto: String): Boolean {
        val limpo = texto.trim()
        if (limpo.isEmpty() || limpo.length > 200) return false
        if (limpo.startsWith("http://") || limpo.startsWith("https://")) return false
        return true
    }

    fun ler(resposta: String, digitado: String, limite: Int = LIMITE): List<String> {
        val raiz = runCatching { LeitorDeJsonSimples(resposta).ler() }.getOrNull() as? List<*> ?: return emptyList()
        val lista = raiz.getOrNull(1) as? List<*> ?: return emptyList()
        val proprio = digitado.trim().lowercase()
        return lista
            .mapNotNull { (it as? String)?.trim() }
            .filter { it.isNotEmpty() && it.lowercase() != proprio }
            .distinctBy { it.lowercase() }
            .take(limite)
    }
}

/** JSON mínimo (sem dependências): objetos viram Map, listas viram List, números viram Double. */
internal class LeitorDeJsonSimples(private val texto: String) {
    private var i = 0

    fun ler(): Any? {
        val valor = valor()
        espacos()
        return valor
    }

    private fun valor(): Any? {
        espacos()
        check(i < texto.length) { "JSON acabou" }
        return when (val c = texto[i]) {
            '[' -> lista()
            '{' -> objeto()
            '"' -> cadeia()
            't' -> literal("true", true)
            'f' -> literal("false", false)
            'n' -> literal("null", null)
            else -> if (c == '-' || c.isDigit()) numero() else error("JSON inválido em $i")
        }
    }

    private fun lista(): List<Any?> {
        i++
        val saida = mutableListOf<Any?>()
        espacos()
        if (texto.getOrNull(i) == ']') {
            i++
            return saida
        }
        while (true) {
            saida.add(valor())
            espacos()
            when (texto.getOrNull(i)) {
                ',' -> i++
                ']' -> {
                    i++
                    return saida
                }
                else -> error("JSON inválido em $i")
            }
        }
    }

    private fun objeto(): Map<String, Any?> {
        i++
        val saida = linkedMapOf<String, Any?>()
        espacos()
        if (texto.getOrNull(i) == '}') {
            i++
            return saida
        }
        while (true) {
            espacos()
            val chave = cadeia()
            espacos()
            check(texto.getOrNull(i) == ':') { "JSON inválido em $i" }
            i++
            saida[chave] = valor()
            espacos()
            when (texto.getOrNull(i)) {
                ',' -> i++
                '}' -> {
                    i++
                    return saida
                }
                else -> error("JSON inválido em $i")
            }
        }
    }

    private fun cadeia(): String {
        check(texto.getOrNull(i) == '"') { "JSON inválido em $i" }
        i++
        val saida = StringBuilder()
        while (i < texto.length) {
            val c = texto[i++]
            when (c) {
                '"' -> return saida.toString()
                '\\' -> {
                    val e = texto[i++]
                    when (e) {
                        'n' -> saida.append('\n')
                        't' -> saida.append('\t')
                        'r' -> saida.append('\r')
                        'b' -> saida.append('\b')
                        'f' -> saida.append('\u000C')
                        'u' -> {
                            saida.append(texto.substring(i, i + 4).toInt(16).toChar())
                            i += 4
                        }
                        else -> saida.append(e)
                    }
                }
                else -> saida.append(c)
            }
        }
        error("texto sem fim")
    }

    private fun numero(): Double {
        val inicio = i
        while (i < texto.length && (texto[i].isDigit() || texto[i] in "+-.eE")) i++
        return texto.substring(inicio, i).toDouble()
    }

    private fun literal(palavra: String, valor: Any?): Any? {
        check(texto.startsWith(palavra, i)) { "JSON inválido em $i" }
        i += palavra.length
        return valor
    }

    private fun espacos() {
        while (i < texto.length && texto[i].isWhitespace()) i++
    }
}
