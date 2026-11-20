package com.chromebrothers.navegador.regras

data class EntradaDoIndiceDaConversaDoChat(
    val id: String,
    val titulo: String,
    val criadaEm: Long,
    val atualizadaEm: Long,
    val bytesAproximados: Long,
    val resumida: Boolean = false,
)

object IndiceDasConversasDoChatDaOpenRouter {
    const val MAXIMO_DE_CONVERSAS = 40
    const val TETO_DE_BYTES_TOTAIS = 200L * 1024 * 1024
    const val TETO_DURO_POR_CONVERSA = 50L * 1024 * 1024

    fun escrever(entradas: List<EntradaDoIndiceDaConversaDoChat>): String {
        val itens = entradas.joinToString(",") { jsonDaEntrada(it) }
        return """{"conversas":[$itens]}"""
    }

    fun ler(bruto: String): List<EntradaDoIndiceDaConversaDoChat> {
        if (!delimitadoresBalanceados(bruto)) return emptyList()
        val objetos = TextoJsonDoChat.objetosDaChave(bruto, "conversas") ?: return emptyList()
        return objetos.map { lerEntrada(it) ?: return emptyList() }
    }

    private fun delimitadoresBalanceados(bruto: String): Boolean {
        val abertos = ArrayDeque<Char>()
        var dentroDeTexto = false
        var indice = 0
        while (indice < bruto.length) {
            val caractere = bruto[indice]
            if (dentroDeTexto) {
                when (caractere) {
                    '\\' -> indice += 2
                    '"' -> {
                        dentroDeTexto = false
                        indice++
                    }
                    else -> indice++
                }
                continue
            }
            when (caractere) {
                '"' -> dentroDeTexto = true
                '{', '[' -> abertos.addLast(caractere)
                '}' -> if (abertos.removeLastOrNull() != '{') return false
                ']' -> if (abertos.removeLastOrNull() != '[') return false
            }
            indice++
        }
        return !dentroDeTexto && abertos.isEmpty()
    }

    fun criar(
        entradas: List<EntradaDoIndiceDaConversaDoChat>,
        id: String,
        titulo: String,
        agoraMs: Long,
        bytes: Long,
    ): List<EntradaDoIndiceDaConversaDoChat> {
        val nova = EntradaDoIndiceDaConversaDoChat(
            id = id,
            titulo = titulo,
            criadaEm = agoraMs,
            atualizadaEm = agoraMs,
            bytesAproximados = bytes,
        )
        return listOf(nova) + entradas
    }

    fun atualizar(
        entradas: List<EntradaDoIndiceDaConversaDoChat>,
        id: String,
        titulo: String? = null,
        atualizadaEm: Long,
        bytes: Long,
        resumida: Boolean? = null,
    ): List<EntradaDoIndiceDaConversaDoChat> {
        if (entradas.none { it.id == id }) return entradas
        return entradas.map { entrada ->
            if (entrada.id != id) {
                entrada
            } else {
                entrada.copy(
                    titulo = titulo ?: entrada.titulo,
                    atualizadaEm = atualizadaEm,
                    bytesAproximados = bytes,
                    resumida = resumida ?: entrada.resumida,
                )
            }
        }
    }

    fun excluir(
        entradas: List<EntradaDoIndiceDaConversaDoChat>,
        id: String,
    ): List<EntradaDoIndiceDaConversaDoChat> = entradas.filter { it.id != id }

    fun bytesTotais(entradas: List<EntradaDoIndiceDaConversaDoChat>): Long =
        entradas.sumOf { it.bytesAproximados }

    fun ultrapassouQuantidade(entradas: List<EntradaDoIndiceDaConversaDoChat>): Boolean =
        entradas.size >= MAXIMO_DE_CONVERSAS

    fun ultrapassouBytesTotais(entradas: List<EntradaDoIndiceDaConversaDoChat>): Boolean =
        bytesTotais(entradas) >= TETO_DE_BYTES_TOTAIS

    fun ultrapassouTetoDuroDaConversa(bytes: Long): Boolean =
        bytes > TETO_DURO_POR_CONVERSA

    private fun jsonDaEntrada(entrada: EntradaDoIndiceDaConversaDoChat): String {
        val id = TextoJsonDoChat.escapar(entrada.id)
        val titulo = TextoJsonDoChat.escapar(entrada.titulo)
        val resumida = if (entrada.resumida) "true" else "false"
        return """{"id":"$id","titulo":"$titulo","criadaEm":${entrada.criadaEm},"atualizadaEm":${entrada.atualizadaEm},"bytesAproximados":${entrada.bytesAproximados},"resumida":$resumida}"""
    }

    private fun lerEntrada(objeto: String): EntradaDoIndiceDaConversaDoChat? {
        val id = TextoJsonDoChat.valorDaChave(objeto, "id") ?: return null
        if (id.isBlank()) return null
        val titulo = TextoJsonDoChat.valorDaChave(objeto, "titulo") ?: return null
        return EntradaDoIndiceDaConversaDoChat(
            id = id,
            titulo = titulo,
            criadaEm = longoDaChave(objeto, "criadaEm"),
            atualizadaEm = longoDaChave(objeto, "atualizadaEm"),
            bytesAproximados = longoDaChave(objeto, "bytesAproximados"),
            resumida = booleanoDaChave(objeto, "resumida") ?: false,
        )
    }

    private fun longoDaChave(json: String, chave: String): Long =
        TextoJsonDoChat.numeroDaChave(json, chave)?.toLong() ?: 0L

    private fun booleanoDaChave(json: String, chave: String): Boolean? {
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
            if (json.startsWith("true", cursor)) return true
            if (json.startsWith("false", cursor)) return false
            indice = achou + 1
        }
        return null
    }
}
